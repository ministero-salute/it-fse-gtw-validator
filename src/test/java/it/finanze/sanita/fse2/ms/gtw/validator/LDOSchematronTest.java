
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
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.base.SchematronPath;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.xslt.SchematronResourceSCH;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class LDOSchematronTest extends AbstractTest {

	@MockBean
	private ConfigSRV config;
 
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		when(config.isAuditEnable()).thenReturn(true);

		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator +"schematronFSE_LDO_V4.8.sch");
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			Map<String,byte[]> cdasOK = getSchematronFiles(SchematronPath.LDO.OK());
			for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
				log.info("File analyzed :" + cdaOK.getKey());
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
				assertEquals(0, resultDTO.getFailedAssertions().size());
				assertEquals(true, resultDTO.getValidSchematron());
				assertEquals(true, resultDTO.getValidXML());
			}
		}
	}

	@Test
	@DisplayName("CDA ERROR")
	void cdaError() throws Exception {
		when(config.isAuditEnable()).thenReturn(true);

		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator +"schematronFSE_LDO_V4.8.sch");
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			
			Map<String,byte[]> cdasKO = getSchematronFiles(SchematronPath.LDO.ERROR());
			for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
				
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				boolean result = resultDTO.getFailedAssertions().size()>0;
				assertTrue(result);
			}
		}
	}
 
}