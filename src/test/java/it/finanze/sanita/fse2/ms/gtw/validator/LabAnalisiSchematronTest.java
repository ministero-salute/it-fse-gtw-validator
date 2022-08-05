package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.helger.schematron.xslt.SchematronResourceXSLT;

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
class LabAnalisiSchematronTest extends AbstractTest {

	@Autowired
	ISchematronRepo schematronRepo;
	
	@Autowired
	IDictionaryRepo dictionaryRepo;

	@Autowired
	IValidationSRV validationSRV;
	
	@Autowired
	@Qualifier("baseUrl")
	private String baseUrl;
	  

	@BeforeEach
	void setup() { 
		dropTerminology();
		Map<String,List<String>> map = new HashMap<>();
		map.put("2.16.840.1.113883.6.1", buildLoincValue()); 
		deleteAndsaveTerminology(map); 
	}
	
	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("11502-2");
		out.add("18729-4");
		out.add("14957-5");
		out.add("48767-8");
		out.add("30525-0");
		return out;
	}
	
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronFSE" + File.separator + "sch" + File.separator +"schematronFSEv15.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematronWithReplacesUrl.getBytes())) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSEv15.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronFSE\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronFSE" + File.separator + "sch" + File.separator +"schematronFSEv15.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematronWithReplacesUrl.getBytes())) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSEv15.sch", bytes);
			SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
			
			Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronFSE\\ERROR");
			for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
				
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				boolean result = resultDTO.getFailedAssertions().size()>0;
				assertTrue(result);
			}
		}
	}
	
	@Test
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronFSE" + File.separator + "xslt" + File.separator +"schematronFSEv15.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematronWithReplacesUrl.getBytes())) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSEv15.xslt", bytes);
			SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
			Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronFSE\\OK");
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
	@DisplayName("CDA ERROR XSLT")
	void cdaErrorXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronFSE" + File.separator + "xslt" + File.separator +"schematronFSEv15.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		try (ByteArrayInputStream bytes = new ByteArrayInputStream(schematronWithReplacesUrl.getBytes())) {
			IReadableResource readableResource = new ReadableResourceInputStream("schematronFSEv15.xslt", bytes);
			SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
			
			Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronFSE\\ERROR");
			for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				boolean result = resultDTO.getFailedAssertions().size()>0;
				assertTrue(result);
			}
		}
	}
	 
}