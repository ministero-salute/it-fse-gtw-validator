/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.base;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public abstract class AbstractTest {
    
    @Autowired
    protected ServletWebServerApplicationContext context;
    @Autowired
	protected MongoTemplate mongo;
	@Autowired
	protected ProfileUtility profiles;

    protected void clearConfigurationItems() {
        mongo.dropCollection(SchemaETY.class);
        mongo.dropCollection(SchematronETY.class);
        mongo.dropCollection(TerminologyETY.class);
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
			final File[] lists = folder.listFiles();
			Objects.requireNonNull(lists);
			for (File file : lists) {
				final String schemaJson = new String(Files.readAllBytes(Paths.get(file.getCanonicalPath())), StandardCharsets.UTF_8);
				final Document schema = Document.parse(schemaJson);
				schema.put("deleted", false); 
				String targetCollection = item;
				if (profiles.isTestProfile()) {
					targetCollection = Constants.Profile.TEST_PREFIX + item;
				}
				mongo.insert(schema, targetCollection);
			}
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			throw new BusinessException(e);
		}
	}
    
	protected Map<String, byte[]> getSchematronFiles(final String path) {
    	Map<String, byte[]> map = new HashMap<>();
		File directory = new File(path);
		String[] actualFiles = directory.list();
		if (actualFiles != null) {
			for (String filename : actualFiles) {
				File file = new File(path + File.separator + filename);
				try {
					map.put(filename, Files.readAllBytes(file.toPath()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return map;
	}
}
