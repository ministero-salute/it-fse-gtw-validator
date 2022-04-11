package it.sanita.fse.validator.controller.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RestController;

import it.sanita.fse.validator.controller.IValidationCTL;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
import it.sanita.fse.validator.dto.response.RawValidationEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author CPIERASC
 *
 *	Validation controller.
 */
@RestController
@Slf4j
public class ValidationCTL extends AbstractCTL implements IValidationCTL {
	
	@Override
	public ValidationResDTO validation(ValidationReqDTO requestBody, HttpServletRequest request) {
		Validation.notNull(requestBody.getCda());
		//TODO: check sintattico
		//TODO: check semantico (sch + voc)
		return new ValidationResDTO(getLogTraceInfo(), RawValidationEnum.OK);
	}
	
}