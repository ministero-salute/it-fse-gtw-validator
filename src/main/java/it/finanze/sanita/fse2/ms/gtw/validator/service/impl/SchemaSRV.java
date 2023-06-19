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

import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.helger.commons.io.stream.StringInputStream;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import lombok.extern.slf4j.Slf4j;

/**
 *	Schema service.
 */
@Service
@Slf4j
public class SchemaSRV implements ISchemaSRV {


	@Override
	public ValidationResult validateXsd(final Validator validator, final String objToValidate) {
		ValidationResult result = new ValidationResult();
		Document document = null;
		try (StringInputStream si = new StringInputStream(objToValidate, StandardCharsets.UTF_8)){
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			builderFactory.setNamespaceAware(true);

			DocumentBuilder parser = builderFactory.newDocumentBuilder();
			

			// parse the XML into a document object
			document = parser.parse(si);

			synchronized(this) {
				validator.setErrorHandler(result);
				validator.validate(new DOMSource(document));
			}

		} catch(Exception ex) {
			log.error("Generic error while validating document.", ex);
			throw new BusinessException("Generic error while validating document.", ex);
		}
		return result;
	}
 
}
