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
import org.junit.jupiter.api.Disabled;
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
class PssSchematronTest extends AbstractTest {

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
		map.put("2.16.840.1.113883.2.9.6.1.5", buildLoinc15());
		map.put("2.16.840.1.113883.1.11.12839", buildLoinc1112839());
		map.put("2.16.840.1.113883.2.9.5.2.8", buildLoincValue28());
		map.put("2.16.840.1.113883.5.1052", buildLoincValue51052());
		map.put("2.16.840.1.113883.6.73", buildLoincValue673());
		map.put("2.16.840.1.113883.2.9.77.22.11.17", buildLoincValue1117());
		map.put("2.16.840.1.113883.5.112", buildLoincValue5112());
		map.put("2.16.840.1.113883.5.111",buildLoincValue5111());
		map.put("2.16.840.1.113883.2.9.77.22.11.13", buildLoincValue1113());
		map.put("2.16.840.1.113883.5.1", buildLoincValue51());
		map.put("2.16.840.1.113883.2.9.77.22.11.2", buildLoincValue112());
		
		deleteAndsaveTerminology(map); 
	}
	
	private List<String> buildLoincValue112(){
		List<String> out = new ArrayList<>();
		out.add("260152009");
		return out;
	}
	
	private List<String> buildLoincValue51(){
		List<String> out = new ArrayList<>();
		out.add("F");
		out.add("M");
		return out;
	}
	
	private List<String> buildLoincValue5112(){
		List<String> out = new ArrayList<>();
		out.add("SQ");
		out.add("IM");
		return out;
	}
	
	private List<String> buildLoincValue673(){
		List<String> out = new ArrayList<>();
		out.add("B01AX05");
		out.add("N01AX10");
		return out;
	}
	
	private List<String> buildLoincValue1113(){
		List<String> out = new ArrayList<>();
		out.add("MMG");
		return out;
	}
	
	private List<String> buildLoincValue5111(){
		List<String> out = new ArrayList<>();
		out.add("FTH");
		return out;
	}
	
	private List<String> buildLoincValue1117(){
		List<String> out = new ArrayList<>();
		out.add("Q13.1");
		return out;
	}
	
	private List<String> buildLoincValue51052(){
		List<String> out = new ArrayList<>();
		out.add("LA");
		out.add("RA");
		return out;
	}



	private List<String> buildLoincValue28(){
		List<String> out = new ArrayList<>();
		out.add("PSSADI");
		out.add("PSSIT99");
		return out;
	}

	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("60591-5");
		out.add("10160-0");
		out.add("11369-6");
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
		out.add("2161-8");
		out.add("42348-3");
		out.add("57827-8");
		out.add("48765-2");
		out.add("52473-6");
		out.add("75321-0");
		out.add("33999-4");
		out.add("LA16666-2");
		out.add("48767-8");
		out.add("75326-9");
		out.add("LA18632-2");
		out.add("89261-2");
		out.add("LA18821-1");
		out.add("10157-6");
		out.add("M");
		out.add("52797-8");
		out.add("35267-4");
		out.add("39016-1");
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

	private List<String> buildLoinc1112839(){
		List<String> out = new ArrayList<>();
		out.add("a");
		out.add("mm[Hg]");
		out.add("mg/dL");
		return out;
	}

	@Test
	@Disabled
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		log.info("Analysing CDA OK with PSS Schematron");
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronPSS" + File.separator + "sch" + File.separator +"schematron_PSS_v2.4.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_PSS_v2.4.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronPSS\\OK");
		for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
			log.info("File analyzed :" + cdaOK.getKey());
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
			assertEquals(0, resultDTO.getFailedAssertions().size());
			assertTrue(resultDTO.getValidSchematron(), "Schematron should be valid");
			assertTrue(resultDTO.getValidXML(), "XML should be valid");
		}
	}

	@Test
	@Disabled
	@DisplayName("CDA ERROR")
	void cdaError() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronPSS" + File.separator + "sch" + File.separator +"schematron_PSS_v2.4.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_PSS_v2.4.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);

		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronPSS\\KO");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {

			try {
				SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
				assertTrue(resultDTO.getFailedAssertions().size() > 0, "At least one failed assertion must be present in KO CDA");
			} catch(Exception ex) {
				log.error("Error encountered while testing CDA Error", ex);
			}
		}
	}
	
	@Test
	@Disabled
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronPSS" + File.separator + "xslt" + File.separator +"schematron_PSS_v2.4.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_PSS_v2.4.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronPSS\\OK");
		for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
			log.info("File analyzed :" + cdaOK.getKey());
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
			assertEquals(0, resultDTO.getFailedAssertions().size());
			assertTrue(resultDTO.getValidSchematron());
			assertTrue(resultDTO.getValidXML());
		}
	}
 
	@Test
	@DisplayName("CDA ERROR XSLT")
	void cdaErrorXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronPSS" + File.separator + "xslt" + File.separator +"schematron_PSS_v2.4.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl.split(":")[0] + ":" + server.getWebServer().getPort());
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_PSS_v2.4.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronPSS\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		 
		}
	}

}