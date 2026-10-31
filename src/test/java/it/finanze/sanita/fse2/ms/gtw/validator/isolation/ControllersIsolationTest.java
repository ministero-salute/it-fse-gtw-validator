package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import io.opentelemetry.api.trace.Tracer;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IFhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.impl.*;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.*;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirValidationDTO.*;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.*;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ValidationCTL.class, LivenessCheckCTL.class})
class ControllersIsolationTest {
    private static final String CDA = "<ClinicalDocument><typeId extension='v1'/><templateId root='root'/></ClinicalDocument>";
    @Autowired private ValidationCTL controller;
    @Autowired private LivenessCheckCTL liveness;
    @MockitoBean private IValidationFacadeSRV validation;
    @MockitoBean private IFhirMappingClient mapping;
    @MockitoBean private LoggerHelper logger;
    @MockitoBean private Tracer tracer;
    private MockHttpServletRequest request;

    @BeforeEach
    void validCda() {
        request = new MockHttpServletRequest();
        when(validation.validateSyntactic(eq(CDA), eq("v1"))).thenReturn(new CDAValidationDTO(CDAValidationStatusEnum.VALID));
        when(validation.validateSemantic(eq(CDA), any())).thenReturn(new SchematronValidationResultDTO(true, true, null, List.of()));
        when(validation.validateVocabularies(CDA, "workflow")).thenReturn(new VocabularyResultDTO(true, null));
        when(validation.getStructureObjectID(List.of("root"))).thenReturn(Pair.of("engine", "map"));
    }

    private ValidationInfoDTO call(List<String> validations, boolean removal) {
        return controller.validation(new ValidationRequestDTO(CDA, "workflow"), request, validations, removal).getResult();
    }

    private ValidationIssueDTO issue(String severity) {
        ValidationIssueDTO issue = new ValidationIssueDTO();
        issue.setSeverity(severity);
        issue.setCode("test-code");
        issue.setMessage("diagnostic");
        issue.setPaths(List.of("Bundle.entry[0]"));
        return issue;
    }

    @Test
    void returnsMappingIdentifiersWithoutCallingMappingEngineByDefault() {
        ValidationInfoDTO result = call(null, false);
        assertEquals(RawValidationEnum.OK, result.getResult());
        assertEquals("engine", result.getEngineID());
        assertEquals("map", result.getTransformID());
        assertTrue(result.getMessage().isEmpty());
        verifyNoInteractions(mapping);
        assertEquals("UP", liveness.health().getStatus().getCode());
    }

    @Test
    void rejectsBlankCdaBeforeUsingDependencies() {
        assertThrows(ValidationException.class, () -> controller.validation(
                new ValidationRequestDTO("  ", "workflow"), request, null, false));
        verifyNoInteractions(validation, mapping);
    }

    @Test
    void syntaxErrorsShortCircuitOtherStages() {
        CDAValidationDTO invalid = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID);
        invalid.setViolations(Map.of(CDASeverityViolationEnum.ERROR, List.of("invalid value")));
        when(validation.validateSyntactic(CDA, "v1")).thenReturn(invalid);
        ValidationInfoDTO result = call(null, false);
        assertEquals(RawValidationEnum.SYNTAX_ERROR, result.getResult());
        assertTrue(result.getMessage().get(0).contains("invalid value"));
        verify(validation, never()).validateSemantic(anyString(), any());
        verifyNoInteractions(mapping);
    }

    @Test
    void reportsSyntaxMessage() {
        CDAValidationDTO invalid = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID);
        invalid.setMessage("schema unavailable");
        when(validation.validateSyntactic(CDA, "v1")).thenReturn(invalid);
        assertEquals(List.of("schema unavailable"), call(null, false).getMessage());
    }

    @Test
    void reportsSemanticFailures() {
        when(validation.validateSemantic(eq(CDA), any())).thenReturn(new SchematronValidationResultDTO(false, false, null, null));
        assertEquals(RawValidationEnum.SEMANTIC_ERROR, call(null, false).getResult());
        when(validation.validateSemantic(eq(CDA), any())).thenReturn(new SchematronValidationResultDTO(false, false, "missing schematron", null));
        assertEquals(List.of("missing schematron"), call(null, false).getMessage());
        verify(validation, never()).validateVocabularies(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void distinguishesSemanticWarningsFromErrors(boolean validXml) {
        when(validation.validateSemantic(eq(CDA), any())).thenReturn(new SchematronValidationResultDTO(true, validXml, null,
                List.of(SchematronFailedAssertionDTO.builder().text("semantic diagnostic").build())));
        ValidationInfoDTO result = call(null, false);
        assertEquals(validXml ? RawValidationEnum.SEMANTIC_WARNING : RawValidationEnum.SEMANTIC_ERROR, result.getResult());
        assertEquals(List.of("semantic diagnostic"), result.getMessage());
    }

    @Test
    void vocabularyFailureOverridesWarningsAndTsSkipsVocabulary() {
        when(validation.validateVocabularies(CDA, "workflow")).thenReturn(new VocabularyResultDTO(false, "unknown code"));
        assertEquals(RawValidationEnum.VOCABULARY_ERROR, call(null, false).getResult());
        clearInvocations(validation);
        request.addHeader("X-System-Type", "TS");
        assertEquals(RawValidationEnum.OK, call(null, false).getResult());
        verify(validation, never()).validateVocabularies(anyString(), anyString());
    }

    @Test
    void removalReportOnlyIncludesSyntheticMetadataAndNormalizationWarning() {
        TransformResDTO response = new TransformResDTO();
        response.setNormalizationIssues(List.of(issue("WARNING")));
        when(mapping.callConvertCdaInBundle(any(), isNull(), eq(true))).thenReturn(response);
        ValidationInfoDTO result = call(null, true);
        assertEquals(RawValidationEnum.PRUNING_WARNING, result.getResult());
        assertEquals(List.of("NORMALIZATION - WARNING: diagnostic"), result.getMessage());
        verify(mapping).callConvertCdaInBundle(argThat(dto -> dto.getDocumentReferenceDTO() != null), isNull(), eq(true));
        verify(logger).warn(eq("workflow"), contains("Bundle.entry[0]"), any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NORMATIVE_R4", "TRAVERSABILITY", "FUTURE_CHECK"})
    void fhirErrorsOverrideNormalizationWarnings(String type) {
        TransformResDTO response = new TransformResDTO();
        response.setNormalizationIssues(List.of(issue("WARNING")));
        FhirValidationDTO fhir = new FhirValidationDTO();
        ValidationResultDTO check = new ValidationResultDTO();
        check.setType(type);
        check.setIssues(List.of(issue("FATAL")));
        fhir.setResults(List.of(check));
        response.setValidationResponse(fhir);
        when(mapping.callConvertCdaInBundle(any(), eq(List.of(type)), eq(false))).thenReturn(response);
        ValidationInfoDTO result = call(List.of(type), false);
        assertEquals(RawValidationEnum.FHIR_VALIDATION_ERROR, result.getResult());
        assertEquals(2, result.getMessage().size());
        verify(logger).error(eq("workflow"), contains("errors: 1"), any(), any(), any(), any());
    }

    @Test
    void informationOnlyIssuesDoNotEmitStructuredLogs() {
        TransformResDTO response = new TransformResDTO();
        FhirValidationDTO fhir = new FhirValidationDTO();
        ValidationResultDTO check = new ValidationResultDTO();
        check.setType("NORMATIVE_R4");
        check.setIssues(List.of(issue("INFORMATION")));
        fhir.setResults(List.of(check));
        response.setValidationResponse(fhir);
        when(mapping.callConvertCdaInBundle(any(), anyList(), eq(false))).thenReturn(response);
        assertEquals(RawValidationEnum.OK, call(List.of("NORMATIVE_R4"), false).getResult());
        verifyNoInteractions(logger);
    }

    @Test
    void rejectsEmptyFailedAndIncompleteMappingResponses() {
        assertThrows(BusinessException.class, () -> call(null, true));
        TransformResDTO response = new TransformResDTO();
        response.setErrorMessage("conversion failed");
        when(mapping.callConvertCdaInBundle(any(), any(), anyBoolean())).thenReturn(response);
        assertTrue(assertThrows(BusinessException.class, () -> call(null, true)).getMessage().contains("conversion failed"));
        response.setErrorMessage(null);
        assertThrows(BusinessException.class, () -> call(List.of("NORMATIVE_R4"), false));
        response.setValidationResponse(new FhirValidationDTO());
        assertThrows(BusinessException.class, () -> call(List.of("NORMATIVE_R4"), false));
    }
}
