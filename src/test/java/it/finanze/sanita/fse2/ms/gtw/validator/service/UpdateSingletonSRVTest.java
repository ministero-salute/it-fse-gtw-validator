package it.finanze.sanita.fse2.ms.gtw.validator.service;

import it.finanze.sanita.fse2.ms.gtw.validator.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UpdateSingletonSRVTest extends AbstractTest {

    public static final String TEST_TEMPLATE_ROOT = "2.16.840.1.113883.2.9.10.1.1";

    @Autowired
    private IUpdateSingletonSRV service;

    @Autowired
    private ISchemaRepo schema;

    @Autowired
    private ISchematronRepo schematron;
    @Autowired
    private IDictionaryRepo dictionary;

    @BeforeAll
    void setup() {
        clearConfigurationItems();
        insertSchematron();
        insertSchema();
    }

    @Test
    void oneRunScheduleTest() {
        assertDoesNotThrow(() -> {
            String version = "1.3";
            SchemaETY ety = schema.findFatherXsd(version);

            if (ety == null) {
                throw new NoRecordFoundException(String.format("Schema with version %s not found on database.", version));
            }
            ety.setLastUpdateDate(new Date());
            SchemaValidatorSingleton.getInstance(false, ety , schema);

            SchematronETY ety0 = schematron.findByTemplateIdRoot(TEST_TEMPLATE_ROOT);

            if (ety0 == null) {
                throw new NoRecordFoundException(String.format("Schematron with template root %s not found on database.", TEST_TEMPLATE_ROOT));
            }
            ety0.setLastUpdateDate(new Date());
            SchematronValidatorSingleton.getInstance(false, ety0, dictionary);

            service.updateSingletonInstance();
        });
    }

}
