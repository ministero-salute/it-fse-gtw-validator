package it.sanita.fse.validator.singleton;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import it.sanita.fse.validator.cda.ValidationResult;
import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.entity.SchemaETY;
import it.sanita.fse.validator.repository.mongo.ISchemaRepo;
import it.sanita.fse.validator.xmlresolver.ResourceResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public final class SchemaValidatorSingleton {

	private static SchemaValidatorSingleton instance;

	private String version;

	private Validator validator;

	private SchemaValidatorSingleton(String inVersion , Validator inValidator) {
		version = inVersion;
		validator = inValidator;
	}

	public static SchemaValidatorSingleton getInstance(final String inVersion , final SchemaETY inSchema, final ISchemaRepo schemaRepo) {
		if(instance==null || !instance.getVersion().equals(inVersion)) {
			try {
				ValidationResult result = new ValidationResult();
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				
				factory.setResourceResolver(new ResourceResolver(inVersion, schemaRepo));
				
				Source schemaFile = new StreamSource(new ByteArrayInputStream(inSchema.getContentSchema().getData()));
				Schema schema = factory.newSchema(schemaFile);
				
				Validator validator = schema.newValidator();
				validator.setErrorHandler(result);
				
				instance = new SchemaValidatorSingleton(inVersion, validator);
			} catch(Exception ex) {
				log.error("Error");
				throw new BusinessException("Ex");
			}
		}   

		return instance;
	}

}
