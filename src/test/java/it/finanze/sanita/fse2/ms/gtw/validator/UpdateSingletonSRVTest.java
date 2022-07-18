package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import org.junit.jupiter.api.*;
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
class UpdateSingletonSRVTest extends AbstractTest {

    public static final String TEST_TEMPLATE_ROOT = "2.16.840.1.113883.2.9.10.1.1";

    @Autowired
    private IUpdateSingletonSRV service;

    @Autowired
    private ISchemaRepo schema;

    @Autowired
    private ISchematronRepo schematron;
    @Autowired
    private IDictionaryRepo dictionary;

    @Autowired
    private IValidationSRV validationSRV;

    @BeforeAll
    void setup() {
        clearConfigurationItems();
        insertSchematron();
        insertSchema();
    }

    @Test
    @Disabled
    void oneRunScheduleTest() {
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
        assertDoesNotThrow(() -> service.updateSingletonInstance());
    }

    // autowired validationSRV -> chiamata a validate syntactic

    @Test
    void noSchemaRecordFoundTest() {
        String version = "1.3";
        SchemaETY ety = schema.findFatherXsd(version);
        ety.setLastUpdateDate(new Date());
        // Validate
        assertDoesNotThrow(() -> validationSRV.validateSyntactic(getTestCda(), version));    // getInstance call inside
        // Remove on DB
        removeSchema("type_id_extension", version);

        // Validate again -> OK still in memory
        assertDoesNotThrow(() -> validationSRV.validateSyntactic(getTestCda(), version));    // getInstance call inside

        // updateSingleton
        assertDoesNotThrow(() -> service.updateSingletonInstance());

        // Validate again -> error
        CDAValidationDTO expected = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID);
        expected.setNoRecordFound("Schema with version 1.3 not found on database.");
        CDAValidationDTO response = validationSRV.validateSyntactic(getTestCda(), version);
        Assertions.assertEquals(expected.getNoRecordFound(), response.getNoRecordFound());
        Assertions.assertEquals(expected.getStatus(), response.getStatus());
    }

    @Test
    void noSchematronRecordFoundTest() {
        String version = "1.3";

        ExtractedInfoDTO extractedInfoDTO = CDAHelper.extractInfo(getTestCda());

        SchematronETY ety = schematron.findByTemplateIdRoot(TEST_TEMPLATE_ROOT);
        ety.setLastUpdateDate(new Date());
        // Validate
        assertDoesNotThrow(() -> validationSRV.validateSemantic(getTestCda(), extractedInfoDTO));    // getInstance call inside
        // Remove on DB
        removeSchematron("template_id_root", TEST_TEMPLATE_ROOT);

        // Validate again -> OK still in memory
        assertDoesNotThrow(() -> validationSRV.validateSemantic(getTestCda(), extractedInfoDTO));    // getInstance call inside

        // updateSingleton
        assertDoesNotThrow(() -> service.updateSingletonInstance());

        // Validate again -> error
        Assertions.assertThrows(BusinessException.class, () -> validationSRV.validateSemantic(getTestCda(), extractedInfoDTO));
    }
}
