package it.finanze.sanita.fse2.ms.gtw.validator;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class SchemaSRVTest {

    @Autowired
    private ISchemaSRV service;
    @Autowired
    private ISchemaRepo repository;

    @Test
    void validationObjectTest() throws SAXException {

        final String cda = new String(getFileFromInternalResources(
            "Files\\cda_ok\\Esempio CDA_002.xml"
        ), StandardCharsets.UTF_8);
        String version = "1.3";

        SchemaETY schema = repository.findFatherXsd(version);

        if (schema == null) {
            throw new NoRecordFoundException(String.format("Schema with version %s not found on database.", version));
        }

        SchemaValidatorSingleton instance = SchemaValidatorSingleton.getInstance(false, schema, repository);
        ValidationResult res = service.validateXsd(instance.getValidator(), cda);

        assertTrue(res.isSuccess());
        assertEquals(0, res.getWarningsCount());
        assertEquals(0, res.getFatalsCount());
        assertEquals(0, res.getErrorsCount());

        res.addFatal("Fatal");
        res.addWarning("Warning");
        res.addError("Error");

        assertEquals(1, res.getWarningsCount());
        assertEquals(1, res.getFatalsCount());
        assertEquals(1, res.getErrorsCount());


        res.clear();

        assertEquals(0, res.getWarningsCount());
        assertEquals(0, res.getFatalsCount());
        assertEquals(0, res.getErrorsCount());

        res.warning(new SAXParseException("message", new LocatorImpl()));
        res.error(new SAXParseException("message", new LocatorImpl()));
        res.fatalError(new SAXParseException("message", new LocatorImpl()));

        assertEquals(1, res.getWarningsCount());
        assertEquals(1, res.getFatalsCount());
        assertEquals(1, res.getErrorsCount());

        res.clear();
    }

}
