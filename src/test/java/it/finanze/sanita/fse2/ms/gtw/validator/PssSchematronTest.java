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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class PssSchematronTest extends AbstractTest {

    @MockitoBean
    private ConfigSRV config;

    @Test
    @DisplayName("CDA OK")
    void cdaOK() throws Exception {
        when(config.isAuditEnable()).thenReturn(true);
        final String folder = "Files" + File.separator + "schematronPSS" + File.separator + "schV3" + File.separator;
        final String filename = "schematron_PSS_v2.7.sch";

        byte[] content = FileUtility.getFileFromInternalResources(folder + filename);

        try (ByteArrayInputStream bytes = new ByteArrayInputStream(content)) {
            IReadableResource resource = new ReadableResourceInputStream("schematron_PSS_v2.7.sch", bytes);
            SchematronResourceSCH sch = new SchematronResourceSCH(resource);
            Map<String, byte[]> cdas = getSchematronFiles(SchematronPath.PSS.OK());

            assumeFalse(CollectionUtils.isEmpty(cdas.values()));
            for (Entry<String, byte[]> cda : cdas.entrySet()) {
                SchematronValidationResultDTO result = CDAHelper.validateXMLViaSchematronFull(sch, cda.getValue());

                assertEquals(0, result.getFailedAssertions().size());
                assertTrue(result.getValidSchematron(), "Schematron should be valid");
                assertTrue(result.getValidXML(), "XML should be valid");
            }
        }

    }

}
