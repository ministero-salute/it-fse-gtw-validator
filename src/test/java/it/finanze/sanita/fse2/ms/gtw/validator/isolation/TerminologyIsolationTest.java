package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.TerminologySRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(TerminologySRV.class)
class TerminologyIsolationTest {
    @Autowired private TerminologySRV service;
    @MockitoBean private IDictionaryRepo dictionaries;
    @MockitoBean private ITerminologyRepo codes;
    @MockitoBean private LoggerHelper logger;

    @BeforeEach
    void managedDictionary() {
        when(dictionaries.getCodeSystems()).thenReturn(List.of(dictionary("1.2.3", "v1", false)));
    }

    private DictionaryETY dictionary(String system, String version, boolean allow) {
        return new DictionaryETY(null, system, version, new Date(1), new Date(1), allow);
    }

    private TerminologyExtractionDTO extracted(String system, String version) {
        return new TerminologyExtractionDTO(new ArrayList<>(List.of(new CodeDTO("A", system, "System", version, "Alpha"))));
    }

    @Test
    void consumesKnownCodes() {
        when(codes.findAllCodesExistsForVersion("1.2.3", "v1", List.of("A"))).thenReturn(List.of("A"));
        TerminologyExtractionDTO input = extracted("1.2.3", "v1");
        assertTrue(service.validateTerminologies(input, "workflow").getValid());
        assertTrue(input.getCodes().isEmpty());
        verify(codes).findAllCodesExistsForVersion("1.2.3", "v1", List.of("A"));
    }

    @Test
    void returnsMissingCodesAndLogsDiagnostic() {
        VocabularyResultDTO result = service.validateTerminologies(extracted("1.2.3", "v1"), "workflow");
        assertFalse(result.getValid());
        assertTrue(result.getMessage().contains("A"));
        verify(logger).warn(eq("workflow"), contains("Invalid Codes"), any(), any(), any(), any());
    }

    @Test
    void suppliesLatestVersionForMissingVersion() {
        when(codes.findAllCodesExistsForVersion("1.2.3", "v1", List.of("A"))).thenReturn(List.of("A"));
        assertTrue(service.validateTerminologies(extracted("1.2.3", null), "workflow").getValid());
        verify(codes).findAllCodesExistsForVersion("1.2.3", "v1", List.of("A"));
    }

    @Test
    void rejectsUnmanagedVersionBeforeLookingUpCodes() {
        VocabularyResultDTO result = service.validateTerminologies(extracted("1.2.3", "v0"), "workflow");
        assertFalse(result.getValid());
        assertTrue(result.getMessage().contains("versione non gestita"));
        verifyNoInteractions(codes);
    }

    @Test
    void rejectsBlocklistedSystem() {
        VocabularyResultDTO result = service.validateTerminologies(extracted("1.999.3", "v1"), "workflow");
        assertFalse(result.getValid());
        assertTrue(result.getMessage().contains("Blocklist"));
        verifyNoInteractions(codes);
    }

    @Test
    void permitsUnknownSystemWithWarning() {
        assertTrue(service.validateTerminologies(extracted("1.2.4", "v1"), "workflow").getValid());
        verify(logger).warn(eq("workflow"), contains("Unknown CodeSystems"), any(), any(), any(), any());
        verifyNoInteractions(codes);
    }

    @Test
    void permitsAllowlistedSystemWithoutCodeLookup() {
        when(dictionaries.getCodeSystems()).thenReturn(List.of(dictionary("1.2.3", "v1", true)));
        assertTrue(service.validateTerminologies(extracted("1.2.3", "v0"), "workflow").getValid());
        verify(logger).warn(eq("workflow"), contains("Allowlisted"), any(), any(), any(), any());
        verifyNoInteractions(codes);
    }

    @Test
    void returnsFailureWhenNoDictionaryIsManaged() {
        when(dictionaries.getCodeSystems()).thenReturn(List.of());
        VocabularyResultDTO result = service.validateTerminologies(extracted("1.2.3", "v1"), "workflow");
        assertFalse(result.getValid());
        assertTrue(result.getMessage().contains("alcun dizionario"));
    }

    @Test
    void doesNotHideInfrastructureFailure() {
        RuntimeException failure = new RuntimeException("Mongo unavailable");
        when(dictionaries.getCodeSystems()).thenThrow(failure);
        assertSame(failure, assertThrows(RuntimeException.class,
                () -> service.validateTerminologies(extracted("1.2.3", "v1"), "workflow")));
    }
}
