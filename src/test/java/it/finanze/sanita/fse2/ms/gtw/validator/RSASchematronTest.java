
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class RSASchematronTest extends AbstractTest {

	@MockBean
	private ConfigSRV config;

	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		when(config.isAuditEnable()).thenReturn(true);

		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "schV3" + File.separator +"schematron_RSA_v7.3.sch");
		
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v7.3.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			Map<String,byte[]> cdasOK = getSchematronFiles(SchematronPath.RSA.OK());
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

		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "schV3" + File.separator +"schematron_RSA_v7.3.sch");

		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v7.3.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			
			Map<String,byte[]> cdasKO = getSchematronFiles(SchematronPath.RSA.ERROR());
			for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
				
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				boolean result = resultDTO.getFailedAssertions().size()>0;
				assertTrue(result);
			}
		}
	}
 
	 
}