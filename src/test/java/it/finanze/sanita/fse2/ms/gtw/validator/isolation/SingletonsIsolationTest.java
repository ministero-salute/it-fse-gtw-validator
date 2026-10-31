package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.impl.SingletonInspectorCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.*;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.*;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.*;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.*;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.*;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.*;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ResourceResolver;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({UpdateSingletonSRV.class, ValidationSRV.class, SchemaSRV.class, SingletonInspectorCTL.class,
        SingletonsIsolationTest.Tracing.class})
class SingletonsIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @Configuration static class Tracing {
        @Bean Tracer tracer() { return OpenTelemetry.noop().getTracer("isolation"); }
    }
    @Autowired private UpdateSingletonSRV updater;
    @Autowired private ValidationSRV validation;
    @Autowired private SingletonInspectorCTL inspector;
    @MockitoBean private ISchemaRepo schemas;
    @MockitoBean private ISchematronRepo schematrons;
    @MockitoBean private IEngineRepo engines;
    @MockitoBean private it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV terminology;

    @BeforeEach @AfterEach
    void clearCaches() {
        if (SchemaValidatorSingleton.getMapInstance() != null) SchemaValidatorSingleton.getMapInstance().clear();
        if (SchematronValidatorSingleton.getMapInstance() != null) SchematronValidatorSingleton.getMapInstance().clear();
    }

    private SchemaETY schema(String content) {
        SchemaETY entity = new SchemaETY();
        entity.setTypeIdExtension("isolation-v1");
        entity.setNameSchema("root.xsd");
        entity.setLastUpdateDate(new Date(1));
        entity.setContentSchema(new Binary(content.getBytes(StandardCharsets.UTF_8)));
        return entity;
    }

    private SchemaETY simpleSchema() {
        return schema("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='value' type='xs:integer'/></xs:schema>");
    }

    private SchematronETY rules(String version, Date updated) {
        SchematronETY entity = new SchematronETY();
        entity.setTemplateIdRoot("isolation-root");
        entity.setVersion(version);
        entity.setSystem("TS");
        entity.setLastUpdateDate(updated);
        entity.setContentSchematron(new Binary("""
            <schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
              <pattern id="checks"><rule context="value">
                <assert test="number(.) &gt; 0">Value must be positive</assert>
                <report test="number(.) = 1">Value is one</report>
              </rule></pattern>
            </schema>
            """.getBytes(StandardCharsets.UTF_8)));
        return entity;
    }

    @Test
    void compilesSchemaResolvesIncludesCachesAndForcesRefresh() {
        SchemaETY parent = schema("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:include schemaLocation='child.xsd'/></xs:schema>");
        SchemaETY child = simpleSchema();
        child.setNameSchema("child.xsd");
        when(schemas.findByNameAndVersion("child.xsd", "isolation-v1")).thenReturn(child);
        SchemaValidatorSingleton first = SchemaValidatorSingleton.getInstance(false, parent, schemas, new Date(1));
        assertTrue(new SchemaSRV().validateXsd(first.getValidator(), "<value>3</value>").isSuccess());
        assertSame(first, SchemaValidatorSingleton.getInstance(false, parent, schemas, new Date(2)));
        SchemaValidatorSingleton refreshed = SchemaValidatorSingleton.getInstance(true, parent, schemas, new Date(2));
        assertNotSame(first, refreshed);
        assertEquals(new Date(2), refreshed.getDataUltimoAggiornamento());
        assertEquals("isolation-v1", refreshed.getTypeIdExtension());
    }

    @Test
    void missingIncludedSchemaAndInvalidSchemaHaveDistinctFailures() {
        SchemaETY parent = schema("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:include schemaLocation='absent.xsd'/></xs:schema>");
        assertThrows(NoRecordFoundException.class, () -> SchemaValidatorSingleton.getInstance(true, parent, schemas, new Date()));
        assertThrows(BusinessException.class, () -> SchemaValidatorSingleton.getInstance(true, schema("broken"), schemas, new Date()));
        RuntimeException failure = new RuntimeException("database offline");
        when(schemas.findByNameAndVersion("child.xsd", "v1")).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class,
                () -> new ResourceResolver("v1", schemas).resolveResource(null, null, null, "child.xsd", null)).getCause());
    }

    @Test
    void loadsRealSchemaFromRepositoryAndUsesCachedValidator() {
        SchemaETY schema = simpleSchema();
        when(schemas.findFatherXsd("isolation-v1")).thenReturn(schema);
        when(schemas.findGtLastUpdate("isolation-v1")).thenReturn(schema);
        assertEquals(CDAValidationStatusEnum.VALID, validation.validateSyntactic("<value>3</value>", "isolation-v1").getStatus());
        assertEquals(CDAValidationStatusEnum.NOT_VALID, validation.validateSyntactic("<value>bad</value>", "isolation-v1").getStatus());
        verify(schemas, times(1)).findFatherXsd("isolation-v1");
    }

    @Test
    void compilesAndExecutesSchematronAssertionsAndReports() throws Exception {
        SchematronETY entity = rules("1", new Date(1));
        SchematronValidatorSingleton first = SchematronValidatorSingleton.getInstance(false, entity);
        assertTrue(first.getSchematronResource().isValidSchematron());
        assertTrue(CDAHelper.validateXMLViaSchematronFull(first.getSchematronResource(), "<value>3</value>".getBytes()).getValidXML());
        var failed = CDAHelper.validateXMLViaSchematronFull(first.getSchematronResource(), "<value>-1</value>".getBytes());
        assertFalse(failed.getValidXML());
        assertTrue(failed.getFailedAssertions().get(0).getText().contains("positive"));
        var warning = CDAHelper.validateXMLViaSchematronFull(first.getSchematronResource(), "<value>1</value>".getBytes());
        assertTrue(warning.getValidXML());
        assertTrue(warning.getFailedAssertions().get(0).getText().contains("one"));
        assertSame(first, SchematronValidatorSingleton.getInstance(false, entity));
        assertEquals("isolation-root|TS", SchematronValidatorSingleton.identifier(entity));
        assertEquals("isolation-root", SchematronValidatorSingleton.identifier("isolation-root", null));
        assertNotSame(first, SchematronValidatorSingleton.getInstance(true, rules("2", new Date(2))));
    }

    @Test
    void semanticServiceLoadsAndRefreshesMajorVersion() {
        when(schematrons.findByRootAndSystem("isolation-root", "TS")).thenReturn(rules("1", new Date(1)));
        ExtractedInfoDTO info = new ExtractedInfoDTO(List.of("isolation-root"), "isolation-v1", SystemTypeEnum.TS);
        assertTrue(validation.validateSemantic("<value>3</value>", info).getValidXML());
        when(schematrons.findGreaterOne("isolation-root", "TS", "1")).thenReturn(rules("2", new Date(2)));
        assertFalse(validation.validateSemantic("<value>-1</value>", info).getValidXML());
        assertEquals("2", SchematronValidatorSingleton.getMapInstance().get("isolation-root|TS").getVersion());
    }

    @Test
    void rejectsMissingSchematronContent() {
        SchematronETY entity = rules("1", new Date());
        entity.setContentSchematron(null);
        assertThrows(BusinessException.class, () -> SchematronValidatorSingleton.getInstance(true, entity));
    }

    @Test
    void refreshesChangedCachesAndRemovesDeletedEntries() {
        SchemaETY schema = simpleSchema();
        SchemaValidatorSingleton initialSchema = SchemaValidatorSingleton.getInstance(true, schema, schemas, new Date(1));
        SchematronValidatorSingleton initialRules = SchematronValidatorSingleton.getInstance(true, rules("1", new Date(1)));
        when(schemas.findFatherXsd("isolation-v1")).thenReturn(schema);
        SchemaETY newer = simpleSchema();
        newer.setLastUpdateDate(new Date(2));
        when(schemas.findByExtensionAndLastUpdateDate("isolation-v1", new Date(1))).thenReturn(List.of(newer));
        when(schematrons.findByRootAndSystem("isolation-root", "TS")).thenReturn(rules("2", new Date(2)));
        updater.updateSingletonInstance();
        assertNotSame(initialSchema, SchemaValidatorSingleton.getMapInstance().get("isolation-v1"));
        assertNotSame(initialRules, SchematronValidatorSingleton.getMapInstance().get("isolation-root|TS"));
        assertEquals(new Date(2), SchemaValidatorSingleton.getMapInstance().get("isolation-v1").getDataUltimoAggiornamento());
        var response = inspector.getSingletons(new org.springframework.mock.web.MockHttpServletRequest());
        assertNotNull(response);
        when(schemas.findFatherXsd("isolation-v1")).thenReturn(null);
        when(schematrons.findByRootAndSystem("isolation-root", "TS")).thenReturn(null);
        updater.updateSingletonInstance();
        assertTrue(SchemaValidatorSingleton.getMapInstance().isEmpty());
        assertTrue(SchematronValidatorSingleton.getMapInstance().isEmpty());
        updater.updateSingletonInstance();
    }

    @Test
    void unchangedAndUndatedRulesKeepTheirInstances() {
        SchemaETY schema = simpleSchema();
        var cached = SchemaValidatorSingleton.getInstance(true, schema, schemas, new Date(1));
        var rules = SchematronValidatorSingleton.getInstance(true, rules("1", new Date(1)));
        when(schemas.findFatherXsd("isolation-v1")).thenReturn(schema);
        when(schematrons.findByRootAndSystem("isolation-root", "TS")).thenReturn(rules("1", new Date(1)));
        updater.updateSingletonInstance();
        assertSame(cached, SchemaValidatorSingleton.getMapInstance().get("isolation-v1"));
        assertSame(rules, SchematronValidatorSingleton.getMapInstance().get("isolation-root|TS"));
        when(schematrons.findByRootAndSystem("isolation-root", "TS")).thenReturn(rules("1", null));
        updater.updateSingletonInstance();
        assertSame(rules, SchematronValidatorSingleton.getMapInstance().get("isolation-root|TS"));
    }
}
