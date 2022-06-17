package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SchematronRepoTest extends AbstractTest {

    public static final String TEST_TEMPLATE_ROOT = "2.16.840.1.113883.2.9.10.1.1";
    public static final String TEST_CDA_CODE_SYSTEM = "2.16.840.1.113883.6.1";
    public static final String TEST_TEMPLATE_IT_EXT = "1.3";
    public static final String TEST_TEMPLATE_IT_EXT__LOWER = "1.2";
    public static final String TEST_TEMPLATE_NAME = "schematronFSE.sch.xsl";
    public static final String TEST_TEMPLATE_CHILD_NAME = "2.16.840.1.113883.6.1.xml";
    public static final int TEST_TEMPLATE_CHILDREN = 1;

    @Autowired
    private ISchematronRepo repository;

    @BeforeAll
    void setup() {
        clearConfigurationItems();
        insertSchematron();
    }

    @Test
    void findByTemplateIdRootTest() {
        // Retrieve
        SchematronETY res = repository.findByTemplateIdRoot(TEST_TEMPLATE_ROOT);
        // Assertions
        assertEquals(TEST_TEMPLATE_ROOT, res.getTemplateIdRoot());
        assertEquals(TEST_TEMPLATE_IT_EXT, res.getTemplateIdExtension());
        assertEquals(TEST_TEMPLATE_NAME, res.getNameSchematron());
    }

    @Test
    void findChildrenBySystemTest() {
        // Retrieve
        List<SchematronETY> res = repository.findChildrenBySystem(TEST_CDA_CODE_SYSTEM);
        // Assertions
        assertEquals(TEST_TEMPLATE_CHILDREN, res.size());
        assertEquals(TEST_TEMPLATE_ROOT, res.get(0).getTemplateIdRoot());
        assertEquals(TEST_TEMPLATE_IT_EXT, res.get(0).getTemplateIdExtension());
        assertEquals(TEST_TEMPLATE_CHILD_NAME, res.get(0).getNameSchematron());
    }

    @Test
    void findBySystemAndVersionTest() {
        // Retrieve
        SchematronETY res = repository.findBySystemAndVersion(
            TEST_TEMPLATE_ROOT,
            TEST_TEMPLATE_IT_EXT__LOWER
        );
        // Assertions
        assertEquals(TEST_TEMPLATE_ROOT, res.getTemplateIdRoot());
        assertEquals(TEST_TEMPLATE_IT_EXT, res.getTemplateIdExtension());
        assertEquals(TEST_TEMPLATE_NAME, res.getNameSchematron());
    }

    @Test
    void findByNameTest() {
        // Retrieve
        SchematronETY res = repository.findByName(TEST_TEMPLATE_NAME);
        // Assertions
        assertEquals(TEST_TEMPLATE_ROOT, res.getTemplateIdRoot());
        assertEquals(TEST_TEMPLATE_IT_EXT, res.getTemplateIdExtension());
        assertEquals(TEST_TEMPLATE_NAME, res.getNameSchematron());
    }

}
