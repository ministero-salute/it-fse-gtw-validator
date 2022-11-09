/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTest {
    
    @Autowired
    protected ServletWebServerApplicationContext context;

    @Autowired
	protected MongoTemplate mongoTemplate;

	@Autowired
	protected ProfileUtility profileUtility;
	
	@Autowired
    protected ServletWebServerApplicationContext server;

    protected void clearConfigurationItems() {
        mongoTemplate.dropCollection(SchemaETY.class);
        mongoTemplate.dropCollection(SchematronETY.class);
        mongoTemplate.dropCollection(TerminologyETY.class);
    }

    protected void insertSchema() {
        insertConfigurationItems("schema");
    }

	protected void removeSchema(String searchKey, String searchValue) {
		removeConfigurationItems(searchKey, searchValue, "schema", SchemaETY.class);
	}

    protected void insertSchematron() {
        insertConfigurationItems("schematron");
    }

	protected void removeSchematron(String searchKey, String searchValue) {
		removeConfigurationItems(searchKey, searchValue, "schematron", SchematronETY.class);
	}

    private void insertConfigurationItems(final String item) {

		try {
			final File folder = context.getResource("classpath:Files/" + item).getFile();

			for (File file : folder.listFiles()) {
				final String schemaJson = new String(Files.readAllBytes(Paths.get(file.getCanonicalPath())), StandardCharsets.UTF_8);
				final Document schema = Document.parse(schemaJson);
				schema.put("deleted", false); 
				String targetCollection = item;
				if (profileUtility.isTestProfile()) {
					targetCollection = Constants.Profile.TEST_PREFIX + item;
				}
				mongoTemplate.insert(schema, targetCollection);

			}
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			throw new BusinessException(e);
		}
	}

	private void removeConfigurationItems(final String searchKey, final String searchValue, final String collectionName, Class<?> destClass) {
		try {
			String targetCollection = collectionName;
			if (profileUtility.isTestProfile()) {
				targetCollection = Constants.Profile.TEST_PREFIX + collectionName;
			}
			mongoTemplate.remove(new Query().addCriteria(Criteria.where(searchKey).is(searchValue)), destClass, targetCollection);
		} catch(Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
			throw new BusinessException(e);
		}
	}
    
    protected Map<String, byte[]> getSchematronFiles(final String directoryPath) {
    	Map<String, byte[]> map = new HashMap<>();
		try {
			File directory = new File(directoryPath);
			
			//only first level files.
			String[] actualFiles = directory.list();
			
			if (actualFiles!=null && actualFiles.length>0) {
				for (String namefile : actualFiles) {
					File file = new File(directoryPath+ File.separator + namefile);
					map.put(namefile, Files.readAllBytes(file.toPath()));
				}
			}
		} catch(Exception ex) {
			log.error("Error while get schematron files : " + ex);
			throw new BusinessException("Error while get schematron files : " + ex);
		}
		return map;
	}

    protected void deleteDictionary() {
    	mongoTemplate.dropCollection(DictionaryETY.class);
    }
    
	protected String getTestCda() {
		return new String(getFileFromInternalResources("Files" + File.separator + "cda1.xml"), StandardCharsets.UTF_8);
	}
	
	protected void deleteAndsaveTerminology(Map<String,List<String>> map) {
		try {
			for(Entry<String, List<String>> el : map.entrySet()) {
				String system = el.getKey();

				Query query = new Query();
				query.addCriteria(Criteria.where(Constants.App.SYSTEM_KEY).is(system));
				mongoTemplate.remove(query, TerminologyETY.class);
				
				TerminologyETY terminology = null;
				for(String val : el.getValue()) {
					terminology = new TerminologyETY();
					terminology.setSystem(system);
					terminology.setCode(val);
					terminology.setDescription(val);
					mongoTemplate.save(terminology);
				}
			}
		} catch(Exception ex) {
			log.error("Error while save dictionary file : " + ex);
			throw new BusinessException("Error while save dictionary file : " + ex);
		}
	}
	
	protected void dropTerminology() {
		mongoTemplate.dropCollection(TerminologyETY.class);
	}
}
