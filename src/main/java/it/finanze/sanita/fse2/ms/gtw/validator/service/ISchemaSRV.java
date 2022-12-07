/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service;

import javax.xml.validation.Validator;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;

/**
 *	Schema interface service.
 */
public interface ISchemaSRV {

	 
	ValidationResult validateXsd(Validator validator, String objToValidate);
}
