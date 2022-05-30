package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helger.schematron.ISchematronResource;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationSRV implements IValidationSRV {

    @Autowired
    private IVocabulariesSRV vocabulariesRV;

    @Autowired
    private ISchematronRepo schematronRepo;
    
    @Autowired
    private ISchemaRepo schemaRepo;
    
    @Autowired
    private ISchemaSRV schemaSRV;
    
    @Autowired
    private IDictionaryRepo dictionaryRepo;
    
    @Override
    public VocabularyResultDTO validateVocabularies(final String cda) {
    	VocabularyResultDTO output = null;
        
        try {
            Map<String, List<String>> vocabularies = CDAHelper.extractTerminology(cda);
            log.info("Validating {} systems...", vocabularies.size());
            output = vocabulariesRV.vocabulariesExists(vocabularies);
        } catch (Exception e) {
            log.error("Error while executing validation on vocabularies", e);
            throw new BusinessException("Error while executing validation on vocabularies", e);
        }

        return output;
    }

    @Override
    public CDAValidationDTO validateSyntactic(final String cda, final String version) {
    	CDAValidationDTO out = new CDAValidationDTO(CDAValidationStatusEnum.VALID);
    	try {
    		Validator validator = null;
    		if(SchemaValidatorSingleton.getMapInstance()!=null && !SchemaValidatorSingleton.getMapInstance().isEmpty()) {
    			SchemaValidatorSingleton singleton = SchemaValidatorSingleton.getMapInstance().get(version);
    			if(singleton!=null) {
    				validator = singleton.getValidator();
    			}
    		}

    		if(validator==null) {
    			SchemaETY schema = schemaRepo.findFatherXsd(version);
    			if (schema == null) {
    				throw new NoRecordFoundException(String.format("Schema with version %s not found on database.", version));
    			}

    			SchemaValidatorSingleton instance = SchemaValidatorSingleton.getInstance(false, schema, schemaRepo);
    			validator = instance.getValidator();
    		} 	

    		ValidationResult validationResult = schemaSRV.validateXsd(validator, cda);
    		if(validationResult!=null && !validationResult.isSuccess()) {
    			out  = new CDAValidationDTO(validationResult);
    		}

    	} catch(NoRecordFoundException nEx) {
    		log.error(String.format("Schema with version %s not found on database.", version));
    		out.setNoRecordFound(String.format("Schema with version %s not found on database.", version));
    		out.setStatus(CDAValidationStatusEnum.NOT_VALID);
    	} catch(Exception ex) {
    		log.error("Error while executing validation on xsd schema", ex);
    		throw new BusinessException("Error while executing validation on xsd schema", ex);
    	}
    	return out;
    }
 
	@Override
	public SchematronValidationResultDTO validateSemantic(final String cdaToValidate,final ExtractedInfoDTO extractedInfoDTO) {
		SchematronValidationResultDTO output = null;
		try { 
			ISchematronResource schematronResource = null;
			
			if(SchematronValidatorSingleton.getMapInstance()!=null && !SchematronValidatorSingleton.getMapInstance().isEmpty()) {
				SchematronValidatorSingleton singleton = SchematronValidatorSingleton.getMapInstance().get(extractedInfoDTO.getTemplateIdSchematron());
				if(singleton!=null) {
					SchematronETY majorVersion = schematronRepo.findBySystemAndVersion(singleton.getTemplateIdRoot(), singleton.getTemplateIdExtension());
					if(majorVersion!=null) {
						singleton = SchematronValidatorSingleton.getInstance(true, majorVersion, dictionaryRepo);
					}
					schematronResource = singleton.getSchematronResource();
				}
			}
			
			if(schematronResource==null) {
				SchematronETY schematronETY = schematronRepo.findByTemplateIdRoot(extractedInfoDTO.getTemplateIdSchematron());
				SchematronValidatorSingleton schematron = SchematronValidatorSingleton.getInstance(false,schematronETY,dictionaryRepo);
				schematronResource = schematron.getSchematronResource();
			}
			
			output = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResource, cdaToValidate.getBytes());
		} catch(Exception ex) {
			log.error("Error while executing validation on schematron", ex);
			throw new BusinessException("Error while executing validation on schematron", ex);
		}
		return output;
	}
     
}
