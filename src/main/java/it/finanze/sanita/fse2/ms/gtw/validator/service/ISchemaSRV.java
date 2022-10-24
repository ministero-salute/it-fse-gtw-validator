/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

import javax.xml.validation.Validator;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;

/**
 *	@author vincenzoingenito
 *
 *	Schema interface service.
 */
public interface ISchemaSRV extends Serializable {

	 
	ValidationResult validateXsd(Validator validator, String objToValidate);
}
