/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class LDOSchematronTest extends AbstractTest {

	@Autowired
	ISchematronRepo schematronRepo;

	@Autowired
	IDictionaryRepo dictionaryRepo;

	@Autowired
	IValidationSRV validationSRV;
 
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator +"schematronFSE_LDO_V4.8.sch");
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronLDO\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "schV3" + File.separator +"schematronFSE_LDO_V4.8.sch");
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematron)) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V4.8.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			
			Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronLDO\\ERROR");
			for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
				
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				boolean result = resultDTO.getFailedAssertions().size()>0;
				assertTrue(result);
			}
		}
	}
 
}