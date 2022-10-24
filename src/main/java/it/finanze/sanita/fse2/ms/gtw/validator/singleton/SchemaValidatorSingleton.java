/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
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

import org.springframework.util.CollectionUtils;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
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

	private String typeIdExtension;

	private Validator validator;

	private Date dataUltimoAggiornamento;
	
	

	private SchemaValidatorSingleton(String inTypeIdExtension, Validator inValidator, Date inDataUltimoAggiornamento) {
		typeIdExtension = inTypeIdExtension;
		validator = inValidator;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
	}

	public static SchemaValidatorSingleton getInstance(final boolean forceUpdate, final SchemaETY inSchema, final ISchemaRepo schemaRepo,
			Date lastUpdatedDate) {
		if (mapInstance != null && !mapInstance.isEmpty()) {
			instance = mapInstance.get(inSchema.getTypeIdExtension());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance == null || CollectionUtils.isEmpty(mapInstance) || Boolean.TRUE.equals(forceUpdate);

		synchronized(SchemaValidatorSingleton.class) {
			if (getInstanceCondition) {
				try {
					ValidationResult result = new ValidationResult();
					SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					factory.setResourceResolver(new ResourceResolver(inSchema.getTypeIdExtension(), schemaRepo));
					
					try (ByteArrayInputStream schemaBytes = new ByteArrayInputStream(inSchema.getContentSchema().getData());) {
						Source schemaFile = new StreamSource(schemaBytes);
						Schema schema = factory.newSchema(schemaFile);
						Validator validator = schema.newValidator();
						validator.setErrorHandler(result);
						instance = new SchemaValidatorSingleton(inSchema.getTypeIdExtension(), validator, lastUpdatedDate/*inSchema.getLastUpdateDate()*/);
						mapInstance.put(instance.getTypeIdExtension(), instance);
					}
				} catch (NoRecordFoundException ne) {
					throw ne;
				} catch(Exception ex) {
					log.error("Error while retrieving and updating Singleton for Schema Validation", ex);
					throw new BusinessException("Error while retrieving and updating Singleton for Schema Validation", ex);
				}
			}
		}

		return instance;
	}

	public String getTypeIdExtension() {
		return typeIdExtension;
	}

	public Date getDataUltimoAggiornamento() {
		return dataUltimoAggiornamento;
	}

	public static Map<String,SchemaValidatorSingleton> getMapInstance() {
		return mapInstance;
	}

}
