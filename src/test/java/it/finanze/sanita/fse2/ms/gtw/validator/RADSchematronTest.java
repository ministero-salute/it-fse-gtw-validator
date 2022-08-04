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
class RADSchematronTest extends AbstractTest {

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
		map.put("2.16.840.1.113883.2.9.77.22.11.13", buildLoinc1113());
		map.put("2.16.840.1.113883.5.111", buildLoinc5111());
		map.put("2.16.840.1.113883.2.9.6.1.5", buildLoinc15());
		map.put("2.16.840.1.113883.1.11.12839", buildLoinc1112839());
		map.put("2.16.840.1.113883.5.1052", buildLoinc51052());
		map.put("2.16.840.1.113883.6.73", buildLoinc673());
		map.put("2.16.840.1.113883.2.9.5.2.8", buildLoinc28());
		map.put("2.16.840.1.113883.5.112", buildLoinc5112());
		map.put("2.16.840.1.113883.2.9.77.22.11.17", buildLoinc1117());
		map.put("2.16.840.1.113883.5.1", buildLoinc51());
		map.put("2.16.840.1.113883.2.9.77.22.11.2", buildLoinc112());
		deleteAndsaveTerminology(map); 
	}
 
	
	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("60591-5");
		out.add("11369-6");
		out.add("10160-0");
		out.add("59781-5");
		out.add("30973-2");
		out.add("11450-4");
		out.add("29762-2");
		out.add("74013-4");
		out.add("10162-6");
		out.add("11614-5");
		out.add("8716-3");
		out.add("8480-6");
		out.add("8462-4");
		out.add("46264-8");
		out.add("18776-5");
		out.add("28564-3");
		out.add("68692-3");
		out.add("47519-4");
		out.add("46240-8");
		out.add("47420-5");
		out.add("75246-9");
		out.add("LA4270-0");
		out.add("30954-2");
		out.add("42348-3");
		out.add("57827-8");
		out.add("2161-8");
		out.add("68604-8");
		out.add("52797-8");
		out.add("35267-4");
		out.add("39016-1");
		out.add("48765-2");
		out.add("52473-6");
		out.add("75321-0");
		out.add("LA16666-2");
		out.add("48767-8");
		out.add("55114-3");
		out.add("36554-4");
		out.add("55111-9");
		out.add("18782-3");
		out.add("55110-1");
		out.add("55107-7");
		out.add("55109-3");
		out.add("18783-1");
		out.add("18785-6");
		out.add("29308-4");
		out.add("11329-0");
		out.add("75326-9");
		out.add("60975-0");
		out.add("89261-2");
		out.add("LA18821-1");
		out.add("33999-4");
		out.add("LA18632-2");
		out.add("10157-6");
		return out;
	}
	
	private List<String> buildLoinc51(){
		List<String> out = new ArrayList<>();
		out.add("M");
		out.add("F");
		return out;
	}
	
	private List<String> buildLoinc1113(){
		List<String> out = new ArrayList<>();
		out.add("MMG");
		return out;
	}
	
	private List<String> buildLoinc5111(){
		List<String> out = new ArrayList<>();
		out.add("FTH");
		out.add("MTH");
		return out;
	}
 
	private List<String> buildLoinc15(){
		List<String> out = new ArrayList<>();
		out.add("active");
		out.add("completed");
		out.add("035606033");
		out.add("035911015");
		return out;
	}
	
	private List<String> buildLoinc5112(){
		List<String> out = new ArrayList<>();
		out.add("SQ");
		out.add("IM");
		return out;
	}
	
	private List<String> buildLoinc1112839(){
		List<String> out = new ArrayList<>();
		out.add("a");
		out.add("mm[Hg]");
		out.add("mg/dL");
		
		return out;
	}
	
	private List<String> buildLoinc51052(){
		List<String> out = new ArrayList<>();
		out.add("LA");
		out.add("mm[Hg]");
		out.add("RA");
		return out;
	}
	
	private List<String> buildLoinc673(){
		List<String> out = new ArrayList<>();
		out.add("B01AX05");
		out.add("N01AX10");
		return out;
	}
	
	private List<String> buildLoinc28(){
		List<String> out = new ArrayList<>();
		out.add("PSSADI");
		out.add("PSSIT99");
		return out;
	}
	 
	private List<String> buildLoinc1117(){
		List<String> out = new ArrayList<>();
		out.add("Q13.1");
		return out;
	}
	
	private List<String> buildLoinc112(){
		List<String> out = new ArrayList<>();
		out.add("260152009");
		return out;
	}
	
	
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "sch" + File.separator +"schematronFSE_RAD_v2.5.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.5.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\OK");
		for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
			log.info("File analyzed :" + cdaOK.getKey());
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
			assertEquals(0, resultDTO.getFailedAssertions().size());
			assertTrue(resultDTO.getValidSchematron());
			assertTrue(resultDTO.getValidXML());
		}
	}
	
	@Test
	@DisplayName("CDA ERROR")
	void cdaError() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "sch" + File.separator +"schematronFSE_RAD_v2.5.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.5.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\KO");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		}
	}
	
	@Test
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "xslt" + File.separator +"schematronFSE_RAD_v2.5.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.5.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "xslt" + File.separator +"schematronFSE_RAD_v2.5.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.5.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		 
		}
	}
	 
	 
}