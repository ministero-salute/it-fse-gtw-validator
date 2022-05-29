package it.finanze.sanita.fse2.ms.gtw.validator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTest {
    
    @Autowired
    protected ServletWebServerApplicationContext context;

    @Autowired
	protected MongoTemplate mongoTemplate;

    protected void clearConfigurationItems() {
        mongoTemplate.dropCollection(SchemaETY.class);
        mongoTemplate.dropCollection(SchematronETY.class);
        mongoTemplate.dropCollection(TerminologyETY.class);
    }

    protected void insertSchema() {
        insertConfigurationItems("schema");
    }

    protected void insertSchematron() {
        insertConfigurationItems("schematron");
    }


    private void insertConfigurationItems(final String item) {
			
		try {
			final File folder = context.getResource("classpath:Files/" + item).getFile();

			for (File file : folder.listFiles()) {
				final String schemaJson = new String(Files.readAllBytes(Paths.get(file.getCanonicalPath())), StandardCharsets.UTF_8);
				final Document schema = Document.parse(schemaJson);
				mongoTemplate.insert(schema, item);

			}
		} catch(Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			throw new BusinessException(e);
		}
	}

}
