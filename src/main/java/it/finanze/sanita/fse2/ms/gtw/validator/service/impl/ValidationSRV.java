
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (C) 2023 Ministero della Salute
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import com.helger.schematron.ISchematronResource;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IEngineRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.validation.Validator;
import java.util.Date;
import java.util.Optional;

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
	private IEngineRepo engines;
      
    
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
	public SchematronValidationResultDTO validateSemantic(final String cdaToValidate,final ExtractedInfoDTO info) {
		SchematronValidationResultDTO output = new SchematronValidationResultDTO(false, false, null, null);
		String id = SchematronValidatorSingleton.identifier(info.getTemplateIdSchematron(), info.getSystem().value());
		try {
			ISchematronResource schematronResource = null;
			
			if(SchematronValidatorSingleton.getMapInstance()!=null && !SchematronValidatorSingleton.getMapInstance().isEmpty()) {
				SchematronValidatorSingleton ss = SchematronValidatorSingleton.getMapInstance().get(id);
				if(ss!=null) {
					SchematronETY majorVersion = schematronRepo.findGreaterOne(ss.getTemplateIdRoot(), ss.getSystem(), ss.getVersion());
					if(majorVersion!=null) {
						ss = SchematronValidatorSingleton.getInstance(true, majorVersion);
					}
					schematronResource = ss.getSchematronResource();
				}
			}
			
			if(schematronResource==null) {
				SchematronETY schematronETY = schematronRepo.findByRootAndSystem(info.getTemplateIdSchematron(), info.getSystem().value());
				if (schematronETY == null) {
					throw new NoRecordFoundException(String.format("Schematron with template id root %s not found on database.", id));
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
	public Pair<String, String> getStructureObjectID(final String templateId){

		Pair<String, String> p;
		EngineETY latest;

		try{
			 latest = engines.getLatestEngine();
		} catch(Exception ex){
			throw new BusinessException("Impossibile recuperare la structure-map nell'engine associato", ex);
		}

		if(latest == null) throw new NoRecordFoundException("Nessun engine disponibile");

		Optional<EngineMap> map = latest.getRoots().stream().filter(r -> r.getRoot().contains(templateId)).findFirst();

		if(!map.isPresent()) {
			throw new NoRecordFoundException(
				String.format("Nessuna mappa con id %s è stata trovata nell'engine %s", templateId, latest.getId())
			);
		}

		p = Pair.of(latest.getId(), map.get().getOid());

		return p;
	}
     
}
