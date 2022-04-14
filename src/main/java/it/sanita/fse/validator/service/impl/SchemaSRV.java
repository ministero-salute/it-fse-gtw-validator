package it.sanita.fse.validator.service.impl;

import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import com.helger.commons.io.stream.StringInputStream;

import it.sanita.fse.validator.cda.ValidationResult;
import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.service.ISchemaSRV;
import lombok.extern.slf4j.Slf4j;

/**
 *	@author vincenzoingenito
 *
 *	Schema service.
 */
@Service
@Slf4j
public class SchemaSRV implements ISchemaSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 1491502156280529977L;
	
	@Override
	public ValidationResult validateXsd(final Validator validator, final String objToValidate) {
		ValidationResult result = new ValidationResult();
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder parser = builderFactory.newDocumentBuilder();
			
			// parse the XML into a document object
			Document document = parser.parse(new StringInputStream(objToValidate, Charset.defaultCharset()));
			
			validator.validate(new DOMSource(document));
		} catch(Exception ex) {
			log.error("Error while validating xsd " , ex);
			throw new BusinessException("Error while validating xsd " , ex);
		}
	    return result;
	}
 
}
