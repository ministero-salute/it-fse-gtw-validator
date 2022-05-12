package it.finanze.sanita.fse2.ms.gtw.validator.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronFailedAssertionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author CPIERASC
 *
 *	Validation controller.
 */
@Slf4j
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1705931913494895399L;
	
	@Autowired
	private IValidationFacadeSRV validationSRV;
 
	@Override
	public ValidationResponseDTO validation(ValidationRequestDTO requestBody, HttpServletRequest request) {
		
		List<String> messages = new ArrayList<>();
		Validation.notNull(requestBody.getCda());
		
		RawValidationEnum outcome = RawValidationEnum.OK;
		
		SchematronInfoDTO schematronInfoDTO = CDAHelper.extractSchematronInfo(requestBody.getCda());
		
		SchematronETY schematronETY = validationSRV.findSchematron(schematronInfoDTO);
		if(schematronETY==null) {
			outcome = RawValidationEnum.SCHEMATRON_NOT_FOUND;
			messages.add("Attention, no schematron found for code " + schematronInfoDTO.getCode()  + " system : " 
					+schematronInfoDTO.getCodeSystem()  + " template id extension : " + schematronInfoDTO.getTemplateIdExtension());
		}
		
		if(RawValidationEnum.OK.equals(outcome)) {
			CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(), schematronETY.getXsdSchemaVersion());
			if(CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
				for(Entry<CDASeverityViolationEnum, List<String>> violations : validationResult.getViolations().entrySet()) {
					String severity = violations.getKey().toString();
					for(String violation : violations.getValue()) {
						messages.add(severity + ": " + violation);
					}
				}
				outcome = RawValidationEnum.SYNTAX_ERROR;
			}	
			
			if(RawValidationEnum.OK.equals(outcome)) {
				SchematronValidationResultDTO semanticValidation = validationSRV.validateSemantic(requestBody.getCda(),schematronETY);
				if(!semanticValidation.getValidXML()) {
					for(SchematronFailedAssertionDTO violation : semanticValidation.getFailedAssertions()) {
						messages.add(violation.getText());
						
					}
					outcome = RawValidationEnum.SEMANTIC_ERROR;
				}
				
				if(RawValidationEnum.OK.equals(outcome)) {
					VocabularyResultDTO result =  validationSRV.validateVocabularies(requestBody.getCda());
					if(Boolean.TRUE.equals(result.getValid())) {
						log.info("Validation completed successfully!");
					} else {
						outcome = RawValidationEnum.VOCABULARY_ERROR;
						messages.add("Almeno uno dei seguenti vocaboli non sono censiti : : " + result.getMessage());
					}
				}
			}
		}
		
		ValidationInfoDTO out = ValidationInfoDTO.builder().result(outcome).message(messages).build();
		return new ValidationResponseDTO(getLogTraceInfo(), out);
	}
	 
	
}