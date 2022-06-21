package it.finanze.sanita.fse2.ms.gtw.validator.controller;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class ValidationControllerTest {

    public static final Path TEST_FILE = Paths.get(
        "Files",
        "cda1.xml"
    );

    public static final Path TEST_FILE_ERR = Paths.get(
        "Files",
        "cda_ok",
        "Esempio CDA_002.xml"
    );

    @Autowired
    private IValidationCTL controller;

    @Test
    void validate() {
        ValidationResponseDTO res = controller.validation(new ValidationRequestDTO(
            new String(
                getFileFromInternalResources(TEST_FILE.toString()),
                StandardCharsets.UTF_8
            )
        ), new MockHttpServletRequest());
        assertEquals(res.getResult().getResult(), RawValidationEnum.SYNTAX_ERROR);

        res = controller.validation(new ValidationRequestDTO(
            new String(
                getFileFromInternalResources(TEST_FILE_ERR.toString()),
                StandardCharsets.UTF_8
            )
        ), new MockHttpServletRequest());

        assertEquals(res.getResult().getResult(), RawValidationEnum.VOCABULARY_ERROR);

    }

}
