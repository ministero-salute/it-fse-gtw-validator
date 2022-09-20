package it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver;

import java.io.ByteArrayInputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceResolver  implements LSResourceResolver {
	 
	private ISchemaRepo schemaRepo;

	private String version;
	
	public ResourceResolver(String inVersion, final ISchemaRepo inSchemaRepo) {
		version = inVersion;
		if(schemaRepo == null) {
			schemaRepo = inSchemaRepo;
		}
	}
	 
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		Input output = null;
		try {
			String nameFile = StringUtility.getFilename(systemId);
			SchemaETY schema = schemaRepo.findByNameAndVersion(nameFile, version);
			if (schema == null) {
				throw new NoRecordFoundException(String.format("Schema with name %s not found", nameFile));
			}
			try (ByteArrayInputStream bytes = new ByteArrayInputStream(schema.getContentSchema().getData())) {
				output = new Input(publicId, schema.getNameSchema(), bytes); 
			}
		} catch (NoRecordFoundException e) {
			throw e;
		} catch(Exception ex) {
			log.error("Error while resolve resource" , ex);
			throw new BusinessException("Error while resolve resource" , ex);	
		}
		return output;
	}
}


