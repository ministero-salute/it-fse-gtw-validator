package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ResourceResolver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public final class SchemaValidatorSingleton {

	private static Map<String,SchemaValidatorSingleton> mapInstance;
	
	private static SchemaValidatorSingleton instance;

	private String version;

	private Validator validator;

	private Date dataUltimoAggiornamento;
	
	

	private SchemaValidatorSingleton(String inVersion , Validator inValidator, Date inDataUltimoAggiornamento) {
		version = inVersion;
		validator = inValidator;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
	}

	public static SchemaValidatorSingleton getInstance(final boolean forceUpdate, final SchemaETY inSchema, final ISchemaRepo schemaRepo) {
		if(mapInstance!=null) {
			instance = mapInstance.get(inSchema.getVersion());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance==null || Boolean.TRUE.equals(forceUpdate);
		if(getInstanceCondition) {
			synchronized(SchematronValidatorSingleton.class) {
				if (getInstanceCondition) {
					try {
						ValidationResult result = new ValidationResult();
						SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
						factory.setResourceResolver(new ResourceResolver(inSchema.getVersion(), schemaRepo));
						Source schemaFile = new StreamSource(new ByteArrayInputStream(inSchema.getContentSchema().getData()));
						Schema schema = factory.newSchema(schemaFile);
						Validator validator = schema.newValidator();
						validator.setErrorHandler(result);
						instance = new SchemaValidatorSingleton(inSchema.getVersion(), validator, inSchema.getLastUpdateDate());
						mapInstance.put(instance.getVersion(), instance);
					} catch(Exception ex) {
						log.error("Error while retrieving and updating Singleton for Schema Validation", ex);
						throw new BusinessException("Error while retrieving and updating Singleton for Schema Validation", ex);
					}
				}
			}
		}

		return instance;
	}

	public String getVersion() {
		return version;
	}

	public Date getDataUltimoAggiornamento() {
		return dataUltimoAggiornamento;
	}

	public static Map<String,SchemaValidatorSingleton> getMapInstance() {
		return mapInstance;
	}

}
