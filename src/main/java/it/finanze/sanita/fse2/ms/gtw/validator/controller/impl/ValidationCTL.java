package it.finanze.sanita.fse2.ms.gtw.validator.controller.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
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
		Validation.notNull(requestBody.getCda());
		
		RawValidationEnum outcome = RawValidationEnum.OK;
		
		SchematronInfoDTO schematronInfoDTO = CDAHelper.extractSchematronInfo(requestBody.getCda());
		
		SchematronETY schematronETY = validationSRV.findSchematron(schematronInfoDTO);
		if(schematronETY==null) {
			throw new NoRecordFoundException("Attention, no schematron found for code : " + schematronInfoDTO.getCode()  + " system : " 
					+schematronInfoDTO.getCodeSystem()  + " template id extension : " + schematronInfoDTO.getTemplateIdExtension());
		}
		
		CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(), schematronETY.getXsdSchemaVersion());
		if(CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
			outcome = RawValidationEnum.SYNTAX_ERROR;
		}	
		
		if(RawValidationEnum.OK.equals(outcome)) {
			SchematronValidationResultDTO semanticValidation = validationSRV.validateSemantic(requestBody.getCda(),schematronETY);
			if(!semanticValidation.getValidXML()) {
				outcome = RawValidationEnum.SEMANTIC_ERROR;
			}

			if(RawValidationEnum.OK.equals(outcome)) {
				if(validationSRV.validateVocabularies(requestBody.getCda())) {
					log.info("Validation completed successfully!");
				} else {
					outcome = RawValidationEnum.VOCABULARY_ERROR;
				}
			}
		}
		
		return new ValidationResponseDTO(getLogTraceInfo(), outcome);
	}
	 
	
}