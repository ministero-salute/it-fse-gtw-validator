
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ISchemaSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class SchemaSRVTest extends AbstractTest {

    @Autowired
    private ISchemaSRV service;
    
    @Autowired
    private ISchemaRepo repository;

    @MockBean
    private ConfigSRV config;

    @BeforeEach
    void setup() {
        clearConfigurationItems();
        insertSchema();
    }

    @Test
    void validationObjectTest() throws SAXException {
        when(config.isAuditEnable()).thenReturn(true);

        final String cda = new String(getFileFromInternalResources(
            "Files/cda_ok/Esempio CDA_002.xml"
        ), StandardCharsets.UTF_8);
        String version = "1.3";

        SchemaETY schema = repository.findFatherXsd(version);

        if (schema == null) {
            throw new NoRecordFoundException(String.format("Schema with version %s not found on database.", version));
        }

        SchemaValidatorSingleton instance = SchemaValidatorSingleton.getInstance(false, schema, repository, new Date());
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
