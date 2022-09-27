package it.finanze.sanita.fse2.ms.gtw.validator.controller.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.ValidationCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronFailedAssertionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
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
	
	@Autowired
	private ValidationCFG validationCFG;
 
	private static final String syntaxValidation="SYNTAX";
	private static final String semanticValidation="SEMANTIC";
	private static final String terminologyValidation="TERMINOLOGY";
	
	@Override
	public ValidationResponseDTO validation(ValidationRequestDTO requestBody, HttpServletRequest request) {
		Long totalSum = 0L;
		List<String> messages = new ArrayList<>();
		Validation.notNull(requestBody.getCda());

		RawValidationEnum outcome = RawValidationEnum.OK;

		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(requestBody.getCda());

		boolean syntaxDisabled = validationCFG.getDisableValidations()!=null && !validationCFG.getDisableValidations().isEmpty() && validationCFG.getDisableValidations().contains(syntaxValidation);
		boolean semanticDisabled = validationCFG.getDisableValidations()!=null && !validationCFG.getDisableValidations().isEmpty() && validationCFG.getDisableValidations().contains(semanticValidation);
		boolean terminologyDisabled = validationCFG.getDisableValidations()!=null && !validationCFG.getDisableValidations().isEmpty() && validationCFG.getDisableValidations().contains(terminologyValidation);
		
		if(!syntaxDisabled) {
			Long startSyntatic = System.currentTimeMillis();
			CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(), infoDTO.getTypeIdExtension());
			Long endSyntatic = System.currentTimeMillis() - startSyntatic;
			totalSum += endSyntatic;
			log.info("VALIDAZIONE SINTATTICA : " + endSyntatic + " ms");
			if(CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
				if(StringUtility.isNullOrEmpty(validationResult.getNoRecordFound())){
					for(Entry<CDASeverityViolationEnum, List<String>> violations : validationResult.getViolations().entrySet()) {
						String severity = violations.getKey().toString();
						for(String violation : violations.getValue()) {
							messages.add(severity + ": " + violation);
						}
					}
				} else {
					messages.add(validationResult.getNoRecordFound());
				}
				outcome = RawValidationEnum.SYNTAX_ERROR;
			}	
		}

		if(RawValidationEnum.OK.equals(outcome)) {
			if(!semanticDisabled) {
				Long startSemantic = System.currentTimeMillis();
				SchematronValidationResultDTO semanticValidation = validationSRV.validateSemantic(requestBody.getCda(),infoDTO);
				Long endSemantic = System.currentTimeMillis() - startSemantic;
				totalSum += endSemantic;
				log.info("VALIDAZIONE SEMANTICA: " + endSemantic + " ms");
				if(semanticValidation.getFailedAssertions()!= null && !semanticValidation.getFailedAssertions().isEmpty()) {
					for(SchematronFailedAssertionDTO violation : semanticValidation.getFailedAssertions()) {
						messages.add(violation.getText());
						outcome = RawValidationEnum.SEMANTIC_WARNING;
					}
					
					if(Boolean.FALSE.equals(semanticValidation.getValidXML())){
						outcome = RawValidationEnum.SEMANTIC_ERROR;
					}
				}
			}

			if(RawValidationEnum.OK.equals(outcome)) {
				if(!terminologyDisabled) {
					Long startTerminology = System.currentTimeMillis();
					VocabularyResultDTO result =  validationSRV.validateVocabularies(requestBody.getCda());
					Long endDateTerminology = System.currentTimeMillis() - startTerminology;
					totalSum += endDateTerminology;
					log.info("VALIDAZIONE TERMINOLOGICA : " + endDateTerminology + " ms");
					if(Boolean.TRUE.equals(result.getValid())) {
						log.debug("Validation completed successfully!");
					} else {
						outcome = RawValidationEnum.VOCABULARY_ERROR;
						messages.add("Almeno uno dei seguenti vocaboli non sono censiti : " + result.getMessage());
					}
				}
			}
		}

		log.info("TOTAL SUM VALIDATION : " + totalSum + " ms");
		ValidationInfoDTO out = ValidationInfoDTO.builder().result(outcome).message(messages).build();
		return new ValidationResponseDTO(getLogTraceInfo(), out);
	}
	 
	
}