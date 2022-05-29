package it.finanze.sanita.fse2.ms.gtw.validator;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.ResetSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class SchemaTest extends AbstractTest {

	@Autowired
	IValidationSRV validationSRV;

    @BeforeEach
    void setup() {
		deleteSchema();
    }

    @Test
	@DisplayName("Schema Validator Singleton")
	void singletonTest() throws Exception {

		insertSchema();
        addSchemaVersion();

		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);

        validationSRV.validateSyntactic(cda, "1.3");
        validationSRV.validateSyntactic(cda, "1.3");

		Map<String,SchemaValidatorSingleton> mapInstance = SchemaValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());

        // load new version
        validationSRV.validateSyntactic(cda, "1.4");

        mapInstance = SchemaValidatorSingleton.getMapInstance();
        assertEquals(2, mapInstance.size());

        // update data ultimo aggiornamento
		updateSchemaLastUpdateDate();

        validationSRV.validateSyntactic(cda, "1.4");
        mapInstance = SchemaValidatorSingleton.getMapInstance();
        assertEquals(2, mapInstance.size());

	}


	@Test
	@DisplayName("Multithread Schema Validator Singleton")
	void multithreadSingletonTest() throws Exception {
    	
		insertSchema();
        addSchemaVersion();
		
		ResetSingleton.setPrivateField(SchemaValidatorSingleton.class, null,null, "mapInstance","instance");
		final int numberThreads = 4;

		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);
        String version = "1.3";

        try {
			List<SingletonThread> threads = new ArrayList<>();
			for (int i=0; i<numberThreads; i++) {
				SingletonThread thread = new SingletonThread(i, cda, version);
				threads.add(thread);
				
				thread.start();
			}
			
			// Waiting for threads to stop
			for (SingletonThread thread : threads) {
				thread.join();
			}
		} catch (Exception e) {
			log.error("Error while executing Jam Session", e);
		}

        Map<String,SchemaValidatorSingleton> mapInstance = SchemaValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());

        // ------ load new version

        version = "1.4";

        try {
			List<SingletonThread> threads = new ArrayList<>();
			for (int i=0; i<numberThreads; i++) {
				SingletonThread thread = new SingletonThread(i, cda, version);
				threads.add(thread);
				
				thread.start();
			}
			
			// Waiting for threads to stop
			for (SingletonThread thread : threads) {
				thread.join();
			}
		} catch (Exception e) {
			log.error("Error while executing Jam Session", e);
		}

        mapInstance = SchemaValidatorSingleton.getMapInstance();
        assertEquals(2, mapInstance.size());

        try {
     			List<SingletonThread> threads = new ArrayList<>();
     			for (int i=0; i<numberThreads; i++) {
     				SingletonThread thread = new SingletonThread(i, cda, version);
     				threads.add(thread);
     				
     				thread.start();
     			}
     			
     			// Waiting for threads to stop
     			for (SingletonThread thread : threads) {
     				thread.join();
     			}
     		} catch (Exception e) {
     			log.error("Error while executing Jam Session", e);
     		}

             mapInstance = SchemaValidatorSingleton.getMapInstance();
             assertEquals(2, mapInstance.size());
    }


    void addSchemaVersion(){

        List<SchemaETY> schemas = mongoTemplate.findAll(SchemaETY.class);

        for(SchemaETY schema : schemas){
            schema.setId(null);
            schema.setVersion("1.4");
        }

        mongoTemplate.insertAll(schemas);

    }

    void updateSchemaLastUpdateDate() throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date newDate = sdf.parse("01/01/2022");

		Query query = new Query();
        query.addCriteria(Criteria.where("version").is("1.4"));

        Update update = new Update();
        update.set("last_update_date", newDate);
        mongoTemplate.updateFirst(query, update, SchemaETY.class);

	}
    

    void deleteSchema() {
		mongoTemplate.remove(new Query(), SchemaETY.class);
    }

    class SingletonThread extends Thread {
		private Integer id;
		private String cda;
		private String version;
		
		public SingletonThread(Integer inID, String inCda, String inVersion) {
			id = inID;
			cda = inCda;
			version = inVersion;
		}
		
		@Override
		public void run() {
			log.info("[THREAD - " + id + "] STARTING ACTION");
			System.out.println("Version : " + version);
			validationSRV.validateSyntactic(cda, version);
			log.info("[THREAD - " + id + "] STOP ACTION");

		}
	}
}
