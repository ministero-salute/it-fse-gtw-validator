package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
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
		SchemaETY schema = null;
		
		try {
			if(StringUtility.isNullOrEmpty(version)) {
				schema = schemaRepo.findFatherLastVersionXsd();
			} else {
				schema = schemaRepo.findFatherXsd(version);
			}

			if (schema == null) {
				throw new BusinessException(String.format("Schema with version %s not found on database.", version));
			}
			
			SchemaValidatorSingleton instance = SchemaValidatorSingleton.getInstance(version, schema, schemaRepo);
			ValidationResult validationResult = schemaSRV.validateXsd(instance.getValidator(), cda);
			if(validationResult!=null && !validationResult.isSuccess()) {
				out  = new CDAValidationDTO(validationResult);
			}
		} catch(Exception ex) {

			if (schema == null) {
				log.error(String.format("Schema with version %s not found on database.", version), ex);
				throw new BusinessException(String.format("Schema with version %s not found on database.", version), ex);
			}
			log.error("Error while executing validation on xsd schema", ex);
			throw new BusinessException("Error while executing validation on xsd schema", ex);
		}
		return out;
	}

	@Override
	public SchematronValidationResultDTO validateSemantic(final String cdaToValidate,final SchematronETY schematronETY) {
		SchematronValidationResultDTO output = null;
		try { 
			SchematronValidatorSingleton schematron = SchematronValidatorSingleton.getInstance(schematronETY,schematronRepo);
			output = CDAHelper.validateXMLViaXSLTSchematronFull(schematron.getSchematronResource(), cdaToValidate.getBytes());
		} catch(Exception ex) {
			log.error("Error while executing validation on schematron", ex);
			throw new BusinessException("Error while executing validation on schematron", ex);
		}
		return output;
	}
    
	@Override
	public SchematronETY findSchematron(final SchematronInfoDTO schematronInfoDTO) {
		SchematronETY schematronETY = null;
		try {
			schematronETY = schematronRepo.findByCodeAndSystemAndExtension(schematronInfoDTO.getCode() , 
					schematronInfoDTO.getCodeSystem(), schematronInfoDTO.getTemplateIdExtension());
		} catch(Exception ex) {
			log.error("Error while executing find schematron ", ex);
			throw new BusinessException("Error while executing find schematron ", ex);
		}
		return schematronETY;
	}
}
