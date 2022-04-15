package it.sanita.fse.validator.controller.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.sanita.fse.validator.cda.CDAHelper;
import it.sanita.fse.validator.controller.IValidationCTL;
import it.sanita.fse.validator.controller.Validation;
import it.sanita.fse.validator.dto.CDAValidationDTO;
import it.sanita.fse.validator.dto.SchematronInfoDTO;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.RawValidationEnum;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
import it.sanita.fse.validator.enums.CDAValidationStatusEnum;
import it.sanita.fse.validator.exceptions.NoRecordFoundException;
import it.sanita.fse.validator.repository.entity.SchematronETY;
import it.sanita.fse.validator.service.facade.IValidationFacadeSRV;
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
	public ValidationResDTO validation(ValidationReqDTO requestBody, HttpServletRequest request) {
		Validation.notNull(requestBody.getCda());
		
		RawValidationEnum outcome = RawValidationEnum.OK;
		
		
		SchematronInfoDTO schematronInfoDTO = CDAHelper.extractSchematronInfo(requestBody.getCda());
		
		SchematronETY schematronETY = validationSRV.findSchematron(schematronInfoDTO);
		if(schematronETY==null) {
			throw new NoRecordFoundException("Attention, no schematron found ");
		}
		
		CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(), schematronETY.getXsdSchemaVersion());
		if(CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
			outcome = RawValidationEnum.SYNTAX_ERROR;
		}	
		
		validationSRV.validateSemantic(requestBody.getCda(),schematronETY);
		
		if(validationSRV.validateVocabularies(requestBody.getCda())) {
			log.info("Validation completed successfully!");
		} else {
			outcome = RawValidationEnum.VOCABULARY_ERROR;
		}
		return new ValidationResDTO(getLogTraceInfo(), outcome);
	}
	 
	
}