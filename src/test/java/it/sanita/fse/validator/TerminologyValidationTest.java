package it.sanita.fse.validator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.sanita.fse.validator.config.Constants;
import it.sanita.fse.validator.repository.entity.VocabularyETY;
import it.sanita.fse.validator.service.IValidationSRV;
import it.sanita.fse.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "redis.vocabulary-ttl=300")
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class TerminologyValidationTest {

    @Autowired
    IValidationSRV validationSRV;

    @Autowired
    IVocabulariesSRV vocabulariesRedisSRV;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void performanceTest() {

        final Map<String, List<String>> terminology = generateRandomTerminology(10, 1000);

        insertTerminologyOnRedis(terminology);
        insertTerminologyOnMongo(terminology);

        boolean existing = validationSRV.validateVocabularies(terminology);

        assertTrue(existing);
    }

    void insertTerminologyOnRedis(Map<String, List<String>> terminology) {

        for (String system : terminology.keySet()) {
            for (String code : terminology.get(system)) {
                vocabulariesRedisSRV.cacheVocabulary(system, code);
            }
        }
    }

    void insertTerminologyOnMongo(Map<String, List<String>> terminology) {

        for (String system : terminology.keySet()) {

            for (String code : terminology.get(system)) {
                VocabularyETY vocabularyETY = new VocabularyETY();
                vocabularyETY.setCode(code);
                vocabularyETY.setSystem(system);
                mongoTemplate.save(vocabularyETY);
            }
        }
    }

    private Map<String, List<String>> generateRandomTerminology(final int numSystems, final int numCodesEachSystem) {
        Map<String, List<String>> terminology = new HashMap<>();

        for (int i = 0; i < numSystems; i++) {
            String system = "system_" + i;
            List<String> codes = new ArrayList<>();
            for (int j = 0; j < numCodesEachSystem; j++) {
                codes.add("code_" + j);
            }
            terminology.put(system, codes);
        }
        return terminology;
    }
}
