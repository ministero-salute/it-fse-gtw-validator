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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.CollectionUtils;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.xslt.SchematronResourceSCH;

import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.base.SchematronPath;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class LDOSchematronTest extends AbstractTest {

    @MockitoBean
    private ConfigSRV config;

    @Test
    @DisplayName("CDA OK")
    void cdaOK() throws Exception {
        when(config.isAuditEnable()).thenReturn(true);
        final String folder = "Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator;
        final String filename = "schematronFSE_LDO_V4.8.sch";

        byte[] content = FileUtility.getFileFromInternalResources(folder + filename);

        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content)) {

            IReadableResource resource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
            SchematronResourceSCH sch = new SchematronResourceSCH(resource);
            Map<String, byte[]> cdas = getSchematronFiles(SchematronPath.LDO.OK());

            assumeFalse(CollectionUtils.isEmpty(cdas.values()));
            for (Entry<String, byte[]> cda : cdas.entrySet()) {

                SchematronValidationResultDTO result = CDAHelper.validateXMLViaSchematronFull(sch, cda.getValue());
                assertEquals(0, result.getFailedAssertions().size());
                assertTrue(result.getValidSchematron());
                assertTrue(result.getValidXML());
            }
        }
    }

    @Test
    @DisplayName("CDA with warnings")
    void cdaWarning() throws Exception {
        when(config.isAuditEnable()).thenReturn(true);
        final String folder = "Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator;
        final String filename = "schematronFSE_LDO_V4.8.sch";

        byte[] content = FileUtility.getFileFromInternalResources(folder + filename);

        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content)) {

            IReadableResource resource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
            SchematronResourceSCH sch = new SchematronResourceSCH(resource);
            Map<String, byte[]> cdas = getSchematronFiles(SchematronPath.LDO.WARNING());

            assumeFalse(CollectionUtils.isEmpty(cdas.values()));
            for (Entry<String, byte[]> cdaOK : cdas.entrySet()) {

                SchematronValidationResultDTO result = CDAHelper.validateXMLViaSchematronFull(sch, cdaOK.getValue());
                assertTrue(result.getFailedAssertions().size() > 0);
                assertTrue(result.getValidSchematron());
                assertTrue(result.getValidXML());
            }
        }
    }

    @Test
    @DisplayName("CDA ERROR")
    void cdaError() throws Exception {
        when(config.isAuditEnable()).thenReturn(true);
        final String folder = "Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator;
        final String filename = "schematronFSE_LDO_V4.8.sch";

        byte[] content = FileUtility.getFileFromInternalResources(folder + filename);
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content)) {

            IReadableResource resource = new ReadableResourceInputStream(filename, bytes);
            SchematronResourceSCH sch = new SchematronResourceSCH(resource);
            Map<String, byte[]> cdas = getSchematronFiles(SchematronPath.LDO.KO());

            assumeFalse(CollectionUtils.isEmpty(cdas.values()));
            for (Entry<String, byte[]> cda : cdas.entrySet()) {

                SchematronValidationResultDTO result = CDAHelper.validateXMLViaSchematronFull(sch, cda.getValue());
                assertFalse(result.getValidXML());
            }
        }
    }

}
