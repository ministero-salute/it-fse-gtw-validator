/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Description;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.config.properties.PropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class TerminologyValidationTest {

    @Autowired
    IVocabulariesSRV vocabulariesSRV;

    @Autowired
    IVocabulariesRedisRepo vocabulariesRedisRepo;

    @Autowired
    ITerminologyRepo vocabulariesMongoRepo;
    
    @MockBean
    private PropertiesCFG propsCFG;
    
    @Autowired
    MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        given(propsCFG.getValidationTTL()).willReturn(120L);
        given(propsCFG.isRedisEnabled()).willReturn(true);
    }

    @Test
    @DisplayName("Test the Terminology Validation")
    void validationTest() {

        final Map<String, List<String>> redisTerminology = generateRandomTerminology(10, 100);
        final Map<String, List<String>> mongoTerminology = generateRandomTerminology(10, 100);

        // A chunk of keys will exist only in Redis
        insertTerminologyOnRedis(redisTerminology);
        // Add one test key for the sake of coverage
        insertTerminologyOnRedis("test");

        // All keys should exist in Mongo
        insertTerminologyOnMongo(redisTerminology);
        insertTerminologyOnMongo(mongoTerminology);

        final Map<String, List<String>> terminology = new HashMap<>();
        terminology.putAll(redisTerminology);
        terminology.putAll(mongoTerminology);

        // VocabularyResultDTO existing = vocabulariesSRV.vocabulariesExists(terminology);

        // assertTrue(existing.getValid());
    } 
    
    @Test
    @DisplayName("Terminology Validation Exception Test")
    void validationExceptionTest() {

        final Map<String, List<String>> redisTerminology = generateRandomTerminology(10, 100);
        final Map<String, List<String>> mongoTerminology = generateRandomTerminology(10, 100);

        // A chunk of keys will exist only in Redis
        insertTerminologyOnRedis(redisTerminology);
        // Add one test key for the sake of coverage
        insertTerminologyOnRedis("test");

        // All keys should exist in Mongo
        insertTerminologyOnMongo(redisTerminology);
        insertTerminologyOnMongo(mongoTerminology);

        final Map<String, List<String>> terminology = new HashMap<>();
        terminology.putAll(redisTerminology);
        terminology.putAll(mongoTerminology);


        // doThrow(new BusinessException("Test Error", null)).when(propsCFG).isRedisEnabled(); 
        
        // assertThrows(Exception.class, () -> vocabulariesSRV.vocabulariesExists(terminology)); 
        
    }

    @Test
    @DisplayName("Testing with and without Redis")
    void redisOnOff() {

        given(propsCFG.isRedisEnabled()).willReturn(false);
        given(propsCFG.getValidationTTL()).willReturn(120L);
        
        final Map<String, List<String>> terminology = generateRandomTerminology(10, 1000);

        // A chunk of keys will exist only in Redis
        insertTerminologyOnRedis(terminology);
        insertTerminologyOnMongo(terminology);

        // Date start = new Date();
        // assertDoesNotThrow(() -> vocabulariesSRV.vocabulariesExists(terminology));
        // Date end = new Date();
        // log.info("Time without Redis: {}", end.getTime() - start.getTime());

        // given(propsCFG.isRedisEnabled()).willReturn(true);
        // start = new Date();
        // assertDoesNotThrow(() -> vocabulariesSRV.vocabulariesExists(terminology));
        // end = new Date();
        // log.info("Time with Redis: {}", end.getTime() - start.getTime());
    }

    @Nested
    class Performance {

        @BeforeEach
        void setup() {
            given(propsCFG.getValidationTTL()).willReturn(120L);
            given(propsCFG.isRedisEnabled()).willReturn(true);
        }

        @ParameterizedTest
        @DisplayName("No keys missed from Redis")
        @CsvSource({ "10, 100"})
        void t1(final int numSystems, final int numCodesEachSystem) {

            // Test validation on REDIS
            final Map<String, List<String>> terminology = generateRandomTerminology(numSystems, numCodesEachSystem);

            insertTerminologyOnRedis(terminology);
            Date start = new Date();
            
            log.info("Starting validating with Redis at: {}", start);
            validateWithRedis(terminology);

            Date end = new Date();
            log.info("Ending validating with Redis at: {}", end);

            final long redisMs = end.getTime() - start.getTime();
            log.info("REDIS - Time elapsed: {}", redisMs);

            // Test validation on MONGO
            insertTerminologyOnMongo(terminology);

            start = new Date();
            
            log.info("Starting validating with Mongo at: {}", start);
            validateWithMongo(terminology);

            end = new Date();
            log.info("Ending validating with Mongo at: {}", end);
            
            final long mongoMs = end.getTime() - start.getTime();
            log.info("MONGO - Time elapsed: {}", mongoMs);

            assertTrue(redisMs < mongoMs, "Redis solution should be faster than Mongo");
        }

        @ParameterizedTest
        @DisplayName("Massive collection on Mongo, all keys present on Redis")
        @CsvSource({ "10, 100"})
        void t2(final int numSystems, final int numCodesEachSystem) {

            // Test validation on REDIS
            final Map<String, List<String>> terminology = generateRandomTerminology(numSystems, numCodesEachSystem);

            insertTerminologyOnRedis(terminology);
            Date start = new Date();
            
            log.info("Starting validating with Redis at: {}", start);
            
            validateWithRedis(terminology);

            Date end = new Date();
            log.info("Ending validating with Redis at: {}", end);

            final long redisMs = end.getTime() - start.getTime();
            log.info("REDIS - Time elapsed: {}", redisMs);

            // Test validation on MONGO
            final Map<String, List<String>> noiseTerminology = generateRandomTerminology(numSystems*10, numCodesEachSystem*10);
            
            insertTerminologyOnMongo(terminology); // Inserting the same terminology on Mongo
            insertTerminologyOnMongo(noiseTerminology); // Inserting a noise terminology on Mongo

            start = new Date();
            
            log.info("Starting validating with Mongo at: {}", start);
            validateWithMongo(terminology);

            end = new Date();
            log.info("Ending validating with Mongo at: {}", end);
            
            final long mongoMs = end.getTime() - start.getTime();
            log.info("MONGO - Time elapsed: {}", mongoMs);

            assertTrue(redisMs < mongoMs, "Redis solution should be faster than Mongo");
        }

        @Disabled("This test evaluates the performance of the Redis solution")
        @RepeatedTest(value = 5, name = "Massive collection on Mongo, missing keys on Redis")
        void t3() {
            given(propsCFG.getValidationTTL()).willReturn(120L);

            final int numSystems = 10;
            final int numCodesEachSystem = 1000;

            final Map<String, List<String>> mongoTerminology = generateRandomTerminology(numSystems, numCodesEachSystem);
            final Map<String, List<String>> redisTerminology = new HashMap<>(mongoTerminology);
            redisTerminology.remove(redisTerminology.keySet().stream().findAny().get());

            // Test validation on REDIS
            insertTerminologyOnMongo(mongoTerminology); // All keys will be present in Mongo
            insertTerminologyOnRedis(redisTerminology); // No keys will be present in Redis
            
            Date start = new Date();
            log.info("Starting validating at: {}", start);
            // vocabulariesSRV.vocabulariesExists(mongoTerminology);
            // Date end = new Date();
            
            // log.info("Ending validating at: {}", end);
            // log.info("Time elapsed: {}", end.getTime() - start.getTime());
        }
    }

    @Nested
    @DirtiesContext
    class CodeSystemIndependent {

        @BeforeEach
        void setup() {
            given(propsCFG.getValidationTTL()).willReturn(120L);
            given(propsCFG.isRedisEnabled()).willReturn(false);
            given(propsCFG.isFindSpecificErrorVocabulary()).willReturn(false);
            given(propsCFG.isFindSystemAndCodesIndependence()).willReturn(true);
            mongoTemplate.dropCollection(TerminologyETY.class);
        }

        @ParameterizedTest
        @Description("Returns success if nothing found")
        @CsvSource({ "10, 100"})
        void findBySystemAndNotCodesSuccessTest(int numSystems, int numCodesEachSystem) {
            final Map<String, List<String>> terminology = generateRandomTerminology(numSystems, numCodesEachSystem);
            insertTerminologyOnMongo(terminology);
            // VocabularyResultDTO res = vocabulariesSRV.vocabulariesExists(terminology);
            // assertEquals(true, res.getValid());
        }

        @ParameterizedTest
        @Description("Returns false if system found and no codes found associated")
        @CsvSource({ "10, 100"})
        void findBySystemAndNotCodesNotFoundTest(int numSystems, int numCodesEachSystem) {
            final Map<String, List<String>> terminology = generateRandomTerminology(numSystems, numCodesEachSystem);
            Map<String, List<String>> terminology2 = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : terminology.entrySet()) {
                List<String> codes = new ArrayList<>();
                for (int j = 0; j < numCodesEachSystem; j++) {
                    codes.add("code_" + Math.random());
                }
                terminology2.put(entry.getKey(), codes);
            }

            // terminology2 will have same systems of terminology1, with different codes

            insertTerminologyOnMongo(terminology);
            // VocabularyResultDTO res = vocabulariesSRV.vocabulariesExists(terminology2);
            // assertEquals(false, res.getValid());
        }

        @ParameterizedTest
        @Description("Returns success if nothing found")
        @CsvSource({ "1, 5"})
        void findBySpecialSystemAndNotCodesSuccessTest(int numSystems, int numCodesEachSystem) {
            final Map<String, List<String>> terminology = generateRandomTerminology(numSystems, numCodesEachSystem);
            List<String> codes = new ArrayList<>();
            for (int j = 0; j < numCodesEachSystem; j++) {
                codes.add("code_" + Math.random());
            }
            terminology.put("2.5.999.6", codes);
            terminology.put("2.5.9999.6.8", codes);
            terminology.put("999.5.999.6", codes);
            terminology.put("9999.5.999.6", codes);
            terminology.put("4.5.4.6.999", codes);
            terminology.put("4.5.4.6.9999", codes);
            terminology.put("999.999.4.6.9999", codes);
            terminology.put("998.997.4.6.996", codes);  // not special char but will pass again
            insertTerminologyOnMongo(terminology);
            // VocabularyResultDTO res = vocabulariesSRV.vocabulariesExists(terminology);
            // assertEquals(true, res.getValid());
        }
    }

    boolean validateWithMongo(Map<String, List<String>> terminology) {
        boolean exists = true;
        for (String system : terminology.keySet()) {

            log.info("Checking existence of {} codes for system {}", terminology.size(), system);
            if (!vocabulariesMongoRepo.allCodesExists(system, terminology.get(system))) {
                log.info("Not all codes for system {} are present on Mongo", system);
                exists = false;
                break;
            }
        }
        return exists;
    }

    boolean validateWithRedis(Map<String, List<String>> terminology) {
        return vocabulariesRedisRepo.allKeysExists(terminology);
    }

    void insertTerminologyOnRedis(Map<String, List<String>> terminology) {
        vocabulariesRedisRepo.insertAll(terminology, propsCFG.getValidationTTL());
    }

    void insertTerminologyOnRedis(String key) {
        vocabulariesRedisRepo.insert(key, propsCFG.getValidationTTL());
    }

    void insertTerminologyOnMongo(Map<String, List<String>> terminology) {

        List<TerminologyETY> vocabularies = new ArrayList<>();
        for (String system : terminology.keySet()) {
            log.info("Inserting {} codes in system {}", terminology.get(system).size(), system);
            for (String code : terminology.get(system)) {
                TerminologyETY vocabularyETY = new TerminologyETY();
                vocabularyETY.setCode(code);
                vocabularyETY.setSystem(system);
                vocabularyETY.setDeleted(false); 
                vocabularies.add(vocabularyETY);
            }
        }
        mongoTemplate.insertAll(vocabularies);
    }

    private Map<String, List<String>> generateRandomTerminology(final int numSystems, final int numCodesEachSystem) {
        Map<String, List<String>> terminology = new HashMap<>();
        final String identifier = UUID.randomUUID().toString().substring(28);

        for (int i = 0; i < numSystems; i++) {
            String system = "system_" + identifier + "_" + i;
            List<String> codes = new ArrayList<>();
            for (int j = 0; j < numCodesEachSystem; j++) {
                codes.add("code_" + j);
            }
            terminology.put(system, codes);
        }
        return terminology;
    }
}
