package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.*;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.*;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.*;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ValidationSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(ValidationSRV.class)
class ValidationServiceIsolationTest {
    @Autowired private ValidationSRV service;
    @MockitoBean private ITerminologySRV terminology;
    @MockitoBean private ISchematronRepo schematrons;
    @MockitoBean private ISchemaRepo schemas;
    @MockitoBean private ISchemaSRV schemaService;
    @MockitoBean private IEngineRepo engines;

    @Test
    void extractsCodesAndPreservesTerminologyResult() {
        String cda = "<ClinicalDocument><code code='A' codeSystem='1.2.3' codeSystemVersion='v1'/></ClinicalDocument>";
        VocabularyResultDTO expected = new VocabularyResultDTO(true, null);
        when(terminology.validateTerminologies(any(), eq("workflow"))).thenReturn(expected);
        assertSame(expected, service.validateVocabularies(cda, "workflow"));
        verify(terminology).validateTerminologies(argThat(input -> input.getCodes().size() == 1
                && "A".equals(input.getCodes().get(0).getCode())
                && "1.2.3".equals(input.getCodes().get(0).getCodeSystem())), eq("workflow"));
    }

    @Test
    void wrapsVocabularyFailure() {
        RuntimeException failure = new RuntimeException("terminology offline");
        when(terminology.validateTerminologies(any(), anyString())).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class,
                () -> service.validateVocabularies("<ClinicalDocument/>", "workflow")).getCause());
    }

    @Test
    void returnsInvalidWhenSchemaMissingOrDatabaseFails() {
        CDAValidationDTO result = service.validateSyntactic("<ClinicalDocument/>", "missing-isolation-version");
        assertEquals(CDAValidationStatusEnum.NOT_VALID, result.getStatus());
        assertTrue(result.getMessage().contains("not found"));
        when(schemas.findFatherXsd("missing-isolation-version")).thenThrow(new RuntimeException("offline"));
        result = service.validateSyntactic("<ClinicalDocument/>", "missing-isolation-version");
        assertEquals(CDAValidationStatusEnum.NOT_VALID, result.getStatus());
        assertEquals("Error while executing validation on xsd schema", result.getMessage());
    }

    @Test
    void returnsSemanticDiagnosticWhenSchematronMissingOrDatabaseFails() {
        ExtractedInfoDTO info = new ExtractedInfoDTO(List.of("missing-isolation-root"), "v1", SystemTypeEnum.NONE);
        SchematronValidationResultDTO result = service.validateSemantic("<ClinicalDocument/>", info);
        assertFalse(result.getValidSchematron());
        assertTrue(result.getMessage().contains("not found"));
        when(schematrons.findByRootAndSystem("missing-isolation-root", null)).thenThrow(new RuntimeException("offline"));
        result = service.validateSemantic("<ClinicalDocument/>", info);
        assertEquals("Error while executing validation on sch schematron", result.getMessage());
    }

    @Test
    void selectsMatchingStructureMapFromLatestEngine() {
        EngineETY engine = new EngineETY();
        engine.setId("engine");
        engine.setRoots(new EngineMap("other", List.of("other-root"), "uri", "1"),
                new EngineMap("map", List.of("root"), "uri", "1"));
        when(engines.getLatestEngine()).thenReturn(engine);
        assertEquals(Pair.of("engine", "map"), service.getStructureObjectID(List.of("root")));
        assertThrows(NoRecordFoundException.class, () -> service.getStructureObjectID(List.of("unknown")));
    }

    @Test
    void distinguishesMissingEngineAndInfrastructureFailure() {
        assertThrows(NoRecordFoundException.class, () -> service.getStructureObjectID(List.of("root")));
        RuntimeException failure = new RuntimeException("offline");
        when(engines.getLatestEngine()).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class,
                () -> service.getStructureObjectID(List.of("root"))).getCause());
    }
}
