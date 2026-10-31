package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.xml.sax.SAXParseException;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.SchemaSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(SchemaSRV.class)
class SchemaIsolationTest {
    @Autowired private SchemaSRV service;

    private Validator validator() throws Exception {
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(
                new StringReader("""
                    <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                      <xs:element name="value" type="xs:integer"/>
                    </xs:schema>
                    """))).newValidator();
    }

    @Test
    void validatesUsingRealXmlSchema() throws Exception {
        assertTrue(service.validateXsd(validator(), "<value>42</value>").isSuccess());
    }

    @Test
    void reportsSchemaViolationsWithoutThrowing() throws Exception {
        ValidationResult result = service.validateXsd(validator(), "<value>not-an-integer</value>");
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorsCount() > 0);
    }

    @Test
    void wrapsMalformedXmlFailure() throws Exception {
        Validator validator = validator();
        BusinessException failure = assertThrows(BusinessException.class,
                () -> service.validateXsd(validator, "<value>"));
        assertInstanceOf(SAXParseException.class, failure.getCause());
    }

    @Test
    void rejectsDoctypeBeforeResolvingExternalEntities() throws Exception {
        Validator validator = validator();
        BusinessException failure = assertThrows(BusinessException.class, () -> service.validateXsd(validator,
                "<!DOCTYPE value [<!ENTITY external SYSTEM 'file:///nonexistent-isolation-secret'>]><value>&external;</value>"));
        assertTrue(failure.getCause().getMessage().contains("DOCTYPE"));
    }

    @Test
    void accumulatesSeverityAndClearsDiagnostics() throws Exception {
        ValidationResult result = new ValidationResult();
        result.addError(null);
        result.addFatal("");
        result.addWarning("");
        assertTrue(result.isSuccess());
        SAXParseException diagnostic = new SAXParseException("diagnostic", null, null, 2, 3);
        result.warning(diagnostic);
        assertTrue(result.isSuccess());
        result.error(diagnostic);
        result.fatalError(diagnostic);
        assertFalse(result.isSuccess());
        assertEquals(1, result.getWarningsCount());
        assertEquals(1, result.getErrorsCount());
        assertEquals(1, result.getFatalsCount());
        assertEquals("2,3 diagnostic", result.getErrors().get(0));
        result.clear();
        assertTrue(result.isSuccess());
        assertEquals(0, result.getWarningsCount() + result.getErrorsCount() + result.getFatalsCount());
    }
}
