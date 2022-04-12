package it.sanita.fse.validator.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.sanita.fse.validator.cda.CDAHelper;
import it.sanita.fse.validator.controller.IValidationCTL;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.RawValidationEnum;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
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
	
	@Autowired
	private IValidationFacadeSRV validationSRV;

	@Override
	public ValidationResDTO validation(ValidationReqDTO requestBody, HttpServletRequest request) {
		Validation.notNull(requestBody.getCda());
		//TODO: check sintattico
		//TODO: check semantico (sch)
		
		validationSRV.validateVocabularies(CDAHelper.extractTerminology(requestBody.getCda()));
		return new ValidationResDTO(getLogTraceInfo(), RawValidationEnum.OK);
	}
	
}