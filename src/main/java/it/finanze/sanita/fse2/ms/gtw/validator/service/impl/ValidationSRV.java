/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.Date;

import javax.xml.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helger.schematron.ISchematronResource;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.TerminologyExtractionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TransformETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITransformRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationSRV implements IValidationSRV {

    @Autowired
    private ITerminologySRV terminologySRV;

    @Autowired
    private ISchematronRepo schematronRepo;
    
    @Autowired
    private ISchemaRepo schemaRepo;
    
    @Autowired
    private ISchemaSRV schemaSRV;
	
	@Autowired
	private ITransformRepo structureMapRepo; 
      
    
    @Override
    public VocabularyResultDTO validateVocabularies(final String cda, final String workflowInstanceId) {
        try {
        	long startTime = new Date().getTime();
        	String sanitizedCda = CodeSystemUtility.sanitizeCda(cda);
            TerminologyExtractionDTO terminologies = CDAHelper.extractAllCodeSystems(sanitizedCda);
            log.info("Validating {} systems...", terminologies.getCodeSystems().size());
            VocabularyResultDTO validateTerminologies = terminologySRV.validateTerminologies(terminologies,workflowInstanceId);
            long endDate = new Date().getTime() - startTime;
            log.info("END DATE TERMINOLOGY QUERY TIME : " + endDate + " ms"); 
            return validateTerminologies;
        } catch (Exception e) {
            log.error("Error while executing validation on vocabularies", e);
            throw new BusinessException("Error while executing validation on vocabularies", e);
        }
    }

    @Override
    public CDAValidationDTO validateSyntactic(final String cda, final String typeIdExtension) {
    	CDAValidationDTO out = new CDAValidationDTO(CDAValidationStatusEnum.VALID);
    	try {
    		Validator validator = null;
    		if(SchemaValidatorSingleton.getMapInstance()!=null && !SchemaValidatorSingleton.getMapInstance().isEmpty()) {
    			SchemaValidatorSingleton singleton = SchemaValidatorSingleton.getMapInstance().get(typeIdExtension);
    			if(singleton!=null) {
    				validator = singleton.getValidator();
    			}
    		}

    		if(validator==null) {
    			SchemaETY schema = schemaRepo.findFatherXsd(typeIdExtension);
    			if (schema == null) {
    				throw new NoRecordFoundException(String.format("Schema with version %s not found on database.", typeIdExtension));
    			}
    			
    			SchemaETY lastUpdated = schemaRepo.findGtLastUpdate(typeIdExtension);

    			SchemaValidatorSingleton instance = SchemaValidatorSingleton.getInstance(false, schema, schemaRepo,lastUpdated.getLastUpdateDate());
    			validator = instance.getValidator();
    		} 	
    		ValidationResult validationResult = schemaSRV.validateXsd(validator, cda);
    		if(validationResult!=null && !validationResult.isSuccess()) {
    			out  = new CDAValidationDTO(validationResult);
    		}

    	} catch(NoRecordFoundException nEx) {
    		out.setMessage(nEx.getMessage());
    		out.setStatus(CDAValidationStatusEnum.NOT_VALID);
    	} catch(Exception ex) {
    		log.error("Error while executing validation on xsd schema", ex);
    		out.setMessage("Error while executing validation on xsd schema");
    		out.setStatus(CDAValidationStatusEnum.NOT_VALID);
    	}
    	return out;
    }
 
	@Override
	public SchematronValidationResultDTO validateSemantic(final String cdaToValidate,final ExtractedInfoDTO extractedInfoDTO) {
		SchematronValidationResultDTO output = new SchematronValidationResultDTO(false, false, null, null);
		try { 
			ISchematronResource schematronResource = null;
			
			if(SchematronValidatorSingleton.getMapInstance()!=null && !SchematronValidatorSingleton.getMapInstance().isEmpty()) {
				SchematronValidatorSingleton singleton = SchematronValidatorSingleton.getMapInstance().get(extractedInfoDTO.getTemplateIdSchematron());
				if(singleton!=null) {
					SchematronETY majorVersion = schematronRepo.findBySystemAndVersion(singleton.getTemplateIdRoot(), singleton.getVersion());
					if(majorVersion!=null) {
						singleton = SchematronValidatorSingleton.getInstance(true, majorVersion);
					}
					schematronResource = singleton.getSchematronResource();
				}
			}
			
			if(schematronResource==null) {
				SchematronETY schematronETY = schematronRepo.findByTemplateIdRoot(extractedInfoDTO.getTemplateIdSchematron());
				if (schematronETY == null) {
					throw new NoRecordFoundException(String.format("Schematron with template id root %s not found on database.", extractedInfoDTO.getTemplateIdSchematron()));
				}
				SchematronValidatorSingleton schematron = SchematronValidatorSingleton.getInstance(false,schematronETY);
				schematronResource = schematron.getSchematronResource();
			}
			
			output = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaToValidate.getBytes());
		} catch(NoRecordFoundException nEx) {
			output.setMessage(nEx.getMessage());
    	} catch(Exception ex) {
    		log.error("Error while executing validation on sch schematron", ex);
    		output.setMessage("Error while executing validation on sch schematron");
    	}
		return output;
	}
	
	@Override
	public String getStructureObjectID(final String templateId){
		String structureId = "";
		try{
			TransformETY structureMap = structureMapRepo.findMapByTemplateIdRoot(templateId);
			if(structureMap!=null) {
				structureId = structureMap.getId(); 
			}
		} catch(Exception ex){
			log.error("Error while getting structureId", ex);
			throw new BusinessException("Error while getting structureId", ex);
		}
		return structureId;
	}
     
}
