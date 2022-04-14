package it.sanita.fse.validator.xmlresolver;

import java.io.ByteArrayInputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.entity.SchemaETY;
import it.sanita.fse.validator.repository.mongo.ISchemaRepo;
import it.sanita.fse.validator.utility.StringUtility;
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
			output = new Input(publicId, schema.getNameSchema(), new ByteArrayInputStream(schema.getContentSchema().getData())); 
		} catch(Exception ex) {
			log.error("Error while resolve resource" , ex);
			throw new BusinessException("Error while resolve resource" , ex);	
		}
		return output;
	}
}


