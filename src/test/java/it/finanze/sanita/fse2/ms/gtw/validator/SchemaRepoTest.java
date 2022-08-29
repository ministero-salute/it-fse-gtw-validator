package it.finanze.sanita.fse2.ms.gtw.validator;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchemaRepoTest extends AbstractTest {

    public static final String TEST_TYPE_ID_EXTENSION = "1.3";
    public static final String TEST_ROOT_NAME_FILE = "CDA.xsd";
    public static final int TEST_FILES_SIZE = 10;

    @Autowired
    private ISchemaRepo repository;

    @SpyBean
    private MongoTemplate mongo;

    @BeforeAll
    void setup() {
        clearConfigurationItems();
        insertSchema();
    }

    @Test
    void findFatherXsdTest() {
        // Retrieve
        SchemaETY res = repository.findFatherXsd(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertTrue(res.getRootSchema());
        assertEquals(TEST_ROOT_NAME_FILE, res.getNameSchema());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findFatherXsd(TEST_TYPE_ID_EXTENSION));
    }

    @Test
    void findChildrenXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findChildrenXsd(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE - 1, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findChildrenXsd(TEST_TYPE_ID_EXTENSION));
    }

    @Test
    void findFatherLastVersionXsdTest() {
        // Retrieve
        SchemaETY res = repository.findFatherLastVersionXsd();
        // Assertions
        assertTrue(res.getRootSchema());
        assertEquals(TEST_ROOT_NAME_FILE, res.getNameSchema());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findFatherLastVersionXsd());
    }

    @Test
    void findByVersionXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findByVersion(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
        // Exceptions
        when(mongo).thenThrow(new MongoException("Test"));
        assertThrows(BusinessException.class, () -> repository.findByVersion(TEST_TYPE_ID_EXTENSION));
    }

}
