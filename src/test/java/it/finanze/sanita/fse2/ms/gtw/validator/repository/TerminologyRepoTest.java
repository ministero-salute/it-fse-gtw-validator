package it.finanze.sanita.fse2.ms.gtw.validator.repository;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TerminologyRepoTest {

    public static final String TEST_SYSTEM_ID = "2.16.840.1.113883.5.111";
    public static final List<String> TEST_SYS_CODES = Collections.singletonList("MTH");
    @Autowired
    private ITerminologyRepo repository;

    @Test
    void findAllCodesExistsTest() {
        // Retrieve
        List<String> res = repository.findAllCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES);
        // Assertions
        assertFalse(res.isEmpty());
    }

    @Test
    void allCodesExistsTest() {
        // Retrieve & assert
        assertTrue(repository.allCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES));
    }

}
