package it.finanze.sanita.fse2.ms.gtw.validator.repository;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TerminologyRepoTest {

    public static final String TEST_SYSTEM_ID = "2.16.840.1.113883.5.111";
    public static final List<String> TEST_SYS_CODES = Collections.singletonList("MTH");
    @Autowired
    private ITerminologyRepo repository;

    @SpyBean
    private MongoTemplate mongo;

    @Test
    void findAllCodesExistsTest() {
        // Retrieve
        List<String> res = repository.findAllCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES);
        // Assertions
        assertFalse(res.isEmpty());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findAllCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES));
    }

    @Test
    void allCodesExistsTest() {
        // Retrieve & assert
        assertTrue(repository.allCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES));
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.allCodesExists(TEST_SYSTEM_ID, TEST_SYS_CODES));
    }

}
