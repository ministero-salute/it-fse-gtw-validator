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
class LDOSchematronTest extends AbstractTest {

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
		map.put("2.16.840.1.113883.2.9.77.22.11.2", buildLoinc112());
		map.put("2.16.840.1.113883.1.11.12839", buildLoinc1112839());
		map.put("2.16.840.1.113883.5.112", buildLoinc5112());
		map.put("2.16.840.1.113883.5.112", buildLoinc5112());
		map.put("2.16.840.1.113883.5.1052", buildLoinc51052());
		map.put("2.16.840.1.113883.2.9.6.1.5", buildLoinc15());
		map.put("2.16.840.1.113883.6.73", buildLoinc673());
		
		deleteAndsaveTerminology(map); 
	}
	
	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("34105-7");
		out.add("46241-6");
		out.add("8646-2");
		out.add("47039-3");
		out.add("11329-0");
		out.add("75326-9");
		out.add("89261-2");
		out.add("LA28752-6");
		out.add("33999-4");
		out.add("LA16666-2");
		out.add("LA18821-1");
		out.add("LA18632-2");
		out.add("29545-1");
		out.add("42346-7");
		out.add("8648-8");
		out.add("55109-3");
		out.add("97835-3");
		out.add("11493-4");
		out.add("34104-0");
		out.add("34820-1");
		out.add("30954-2");
		out.add("24660-3");
		out.add("47519-4");
		out.add("2341-6");
		out.add("48765-2");
		out.add("52473-6");
		out.add("75321-0");
		out.add("48767-8");
		out.add("10160-0");
		out.add("11535-2");
		out.add("8651-2");
		out.add("10183-2");
		out.add("18776-5");
		
		return out;
	}
	
	private List<String> buildLoinc112(){
		List<String> out = new ArrayList<>();
		out.add("260152009"); 
		return out;
	}
	
	private List<String> buildLoinc1112839(){
		List<String> out = new ArrayList<>();
		out.add("h");
		out.add("mL");
		out.add("mL/h");
		out.add("mg");
		out.add("mg/h");
		return out;
	}
	
	private List<String> buildLoinc5112(){
		List<String> out = new ArrayList<>();
		out.add("IABDINJ");
		out.add("PO");
		return out;
	}
	
	private List<String> buildLoinc51052(){
		List<String> out = new ArrayList<>();
		out.add("LA");
		return out;
	}
	
	private List<String> buildLoinc15(){
		List<String> out = new ArrayList<>();
		out.add("035606033");
		out.add("043348022");
		return out;
	}
	
	private List<String> buildLoinc673(){
		List<String> out = new ArrayList<>();
		out.add("B01AX05");
		out.add("C08CA01");
		return out;
	}
	

	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "sch" + File.separator +"schematronFSE_LDO_V3.5.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V3.5.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
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

	@Test
	@DisplayName("CDA ERROR")
	void cdaError() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "sch" + File.separator +"schematronFSE_LDO_V3.5.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V3.5.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronLDO\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		}
	}

	@Test
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "xslt" + File.separator +"schematronFSE_LDO_V3.5.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V3.5.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronLDO\\OK");
		for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
			log.info("File analyzed :" + cdaOK.getKey());
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
			assertEquals(0, resultDTO.getFailedAssertions().size());
			assertEquals(true, resultDTO.getValidSchematron());
			assertEquals(true, resultDTO.getValidXML());
		}
	}
 
	@Test
	@DisplayName("CDA ERROR XSLT")
	void cdaErrorXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "xslt" + File.separator +"schematronFSE_LDO_V3.5.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_LDO_V3.5.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronLDO\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		 
		}
	}
 

}