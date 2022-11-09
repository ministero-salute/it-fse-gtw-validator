/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class TerminologyValidationTest {


    @Autowired
    ITerminologyRepo vocabulariesMongoRepo;
    
    @MockBean
    private PropertiesCFG propsCFG;
    
    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    @DisplayName("Test the Terminology Validation")
    void validationTest() {

        final Map<String, List<String>> redisTerminology = generateRandomTerminology(10, 100);
        final Map<String, List<String>> mongoTerminology = generateRandomTerminology(10, 100);

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
 
        // All keys should exist in Mongo
        insertTerminologyOnMongo(redisTerminology);
        insertTerminologyOnMongo(mongoTerminology);

        final Map<String, List<String>> terminology = new HashMap<>();
        terminology.putAll(redisTerminology);
        terminology.putAll(mongoTerminology);


        // doThrow(new BusinessException("Test Error", null)).when(propsCFG).isRedisEnabled(); 
        
        // assertThrows(Exception.class, () -> vocabulariesSRV.vocabulariesExists(terminology)); 
        
    }

    
    @Nested
    @DirtiesContext
    class CodeSystemIndependent {

        @BeforeEach
        void setup() {
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
