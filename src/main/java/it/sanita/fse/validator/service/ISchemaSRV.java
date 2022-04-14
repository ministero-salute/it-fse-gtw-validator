package it.sanita.fse.validator.service;

import java.io.Serializable;

import javax.xml.validation.Validator;

import it.sanita.fse.validator.cda.ValidationResult;

/**
 *	@author vincenzoingenito
 *
 *	Schema interface service.
 */
public interface ISchemaSRV extends Serializable {

	 
	ValidationResult validateXsd(Validator validator, String objToValidate);
}
