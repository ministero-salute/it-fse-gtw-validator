package it.finanze.sanita.fse2.ms.gtw.validator.repository;

import it.finanze.sanita.fse2.ms.gtw.validator.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    }

    @Test
    void findChildrenXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findChildrenXsd(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE - 1, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
    }

    @Test
    void findFatherLastVersionXsdTest() {
        // Retrieve
        SchemaETY res = repository.findFatherLastVersionXsd();
        // Assertions
        assertTrue(res.getRootSchema());
        assertEquals(TEST_ROOT_NAME_FILE, res.getNameSchema());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.getTypeIdExtension());
    }

    @Test
    void findByVersionXsdTest() {
        // Retrieve
        List<SchemaETY> res = repository.findByVersion(TEST_TYPE_ID_EXTENSION);
        // Assertions
        assertFalse(res.isEmpty());
        assertEquals(TEST_FILES_SIZE, res.size());
        assertEquals(TEST_TYPE_ID_EXTENSION, res.get(0).getTypeIdExtension());
    }

}
