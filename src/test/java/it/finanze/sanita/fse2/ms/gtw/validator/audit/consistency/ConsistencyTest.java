package it.finanze.sanita.fse2.ms.gtw.validator.audit.consistency;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.ms.gtw.validator.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.OperationLogEnum.TERMINOLOGY_VALIDATION;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum.WARN;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.WarnLogEnum.INVALID_CODE;
import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(TEST)
public class ConsistencyTest {

    @Autowired
    private IValidationSRV service;

    @MockBean
    private IDictionaryRepo repository;

    @MockBean
    private LoggerHelper logger;

    @Test
    void checkInvalidCSLog() {
        // Mock repository to retrieve dictionaries
        doReturn(getDictionaries()).when(repository).getCodeSystems();
        // Run test
        VocabularyResultDTO res = service.validateVocabularies(
            getCDA(),
            "TEST_WIF"
        );
        // Check it has been called with the right arguments
        verify(logger, atLeastOnce()).warn(
            eq("TEST_WIF"),
            eq(getExpectedMessage()),
            eq(TERMINOLOGY_VALIDATION),
            eq(WARN),
            any(Date.class),
            eq(INVALID_CODE)
        );
        // Verify validation didn't pass
        assertFalse(res.getValid());
    }

    private String getCDA() {
        return new String(
            getFileFromInternalResources("Files/lab_inconsistency_cs.xml"),
            UTF_8
        );
    }

    private List<DictionaryETY> getDictionaries() {
        return Collections.singletonList(
            new DictionaryETY(
                new ObjectId().toHexString(),
                "2.16.840.1.113883.6.1",
                "1.0",
                new Date(),
                new Date(),
                false
            )
        );
    }

    private String getExpectedMessage() {
        return "Invalid Codes found during the validation: [{\"code\":\"2.16.840.1.113883.6.1\",\"display-name\":\"LOINC\",\"version\":\"1.0\",\"codes\":\"[{\"code\":\"11502-2\",\"display-name\":\"Referto di laboratorio\"}, {\"code\":\"18729-4\",\"display-name\":\"ESAMI DELLE URINE\"}, {\"code\":\"14957-5\",\"display-name\":\"Microalbumin Massa/Volume in Urine\"}, {\"code\":\"14957-5\",\"display-name\":\"Microalbumin Massa/Volume in Urine\"}, {\"code\":\"48767-8\",\"display-name\":\"Annotazioni e commenti\"}, {\"code\":\"14957-5\",\"display-name\":\"Microalbumin Massa/Volume in Urine\"}, {\"code\":\"30525-0\",\"display-name\":\"Età\"}]}]";
    }

}
