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
class RSASchematronTest extends AbstractTest {

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
		map.put("2.16.840.1.113883.5.1", buildLoinc51Value());
		map.put("2.16.840.1.113883.11.22.12", buildLoinc2212Value());
		map.put("2.16.840.1.113883.5.111", buildLoinc5111Value());
		map.put("2.16.840.1.113883.6.73", buildLoinc673Value());
		map.put("2.16.840.1.113883.1.11.12839", buildLoinc1112839Value());
		map.put("2.16.840.1.113883.5.112", buildLoinc5112Value());
		map.put("2.16.840.1.113883.2.9.6.1.5", buildLoinc15Value());
		map.put("2.16.840.1.113883.2.9.77.22.11.2", buildLoinc112Value());
		map.put("2.16.840.1.113883.2.9.77.22.11.4", buildLoinc114Value());
		deleteAndsaveTerminology(map); 
	}
	
	private List<String> buildLoinc112Value(){
		List<String> out = new ArrayList<>();
		out.add("111088007");
		return out;
	}
	
	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("11488-4");
		out.add("29299-5");
		out.add("29298-7");
		out.add("11329-0");
		out.add("75326-9");
		out.add("60975-0");
		out.add("89261-2");
		out.add("LA18821-1");
		out.add("33999-4");
		out.add("LA18632-2");
		out.add("10157-6");
		out.add("52797-8");
		out.add("35267-4");
		out.add("39016-1");
		out.add("48765-2");
		out.add("52473-6");
		out.add("75321-0");
		out.add("LA16666-2");
		out.add("30954-2");
		out.add("29545-1");
		out.add("62387-6");
		out.add("93126-1");
		out.add("47045-0");
		out.add("29548-5");
		out.add("29308-4");
		out.add("55110-1");
		out.add("62385-0");
		out.add("80615-8");
		out.add("2340-8");
		out.add("93341-6");
		out.add("48767-8");
		out.add("10160-0");
		return out;
	}
	
	private List<String> buildLoinc51Value(){
		List<String> out = new ArrayList<>();
		out.add("M");
		out.add("F");
		return out;
	}
	
	private List<String> buildLoinc2212Value(){
		List<String> out = new ArrayList<>();
		out.add("completed");
		out.add("active");
		return out;
	}
	
	private List<String> buildLoinc5111Value(){
		List<String> out = new ArrayList<>();
		out.add("MTH");
		return out;
	}
	
	private List<String> buildLoinc673Value(){
		List<String> out = new ArrayList<>();
		out.add("C02LA01");
		out.add("C03CA01");
		return out;
	}

	private List<String> buildLoinc1112839Value(){
		List<String> out = new ArrayList<>();
		out.add("mg");
		out.add("mg/h");
		out.add("a");
		out.add("h");
		return out;
	}
	
	private List<String> buildLoinc5112Value(){
		List<String> out = new ArrayList<>();
		out.add("PO");
		return out;
	}
	
	private List<String> buildLoinc15Value(){
		List<String> out = new ArrayList<>();
		out.add("023993013");
		return out;
	}
	
	private List<String> buildLoinc114Value(){
		List<String> out = new ArrayList<>();
		out.add("698.8");
		return out;
	}
	
	
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "sch" + File.separator +"schematron_RSA_v6.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v6.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronRSA\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "sch" + File.separator +"schematron_RSA_v6.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v6.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRSA\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		}
	}
	
	@Test
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "xslt" + File.separator +"schematron_RSA_v6.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v6.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronRSA\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRSA" + File.separator + "xslt" + File.separator +"schematron_RSA_v6.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_RSA_v6.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRSA\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		 
		}
	}
	 
	 
}