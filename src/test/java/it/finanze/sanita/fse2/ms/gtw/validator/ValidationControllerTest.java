package it.finanze.sanita.fse2.ms.gtw.validator;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants; 
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class ValidationControllerTest extends AbstractTest {

    @BeforeEach
    void setup() {
        clearConfigurationItems();
        insertSchematron();
        insertSchema();
    }

    public static final Path TEST_FILE = Paths.get(
        "Files",
        "cda1.xml"
    );

    public static final Path TEST_FILE_ERR = Paths.get(
        "Files",
        "cda_ok",
        "Esempio CDA_002.xml"
    );

}
