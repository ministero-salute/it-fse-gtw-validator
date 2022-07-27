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
public class SingVaccSchematronTest extends AbstractTest {

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
		map.put("2.16.840.1.113883.5.112", buildLoinc51112Value());
		map.put("2.16.840.1.113883.5.1052", buildLoinc51052Value());
		map.put("2.16.840.1.113883.6.73", buildLoinc673Value());
		map.put("2.16.840.1.113883.2.9.6.1.5", buildLoinc15Value());

		deleteAndsaveTerminology(map); 
	}

	private List<String> buildLoinc51112Value(){
		List<String> out = new ArrayList<>();
		out.add("IABDINJ");
		return out;
	}

	private List<String> buildLoinc51052Value(){
		List<String> out = new ArrayList<>();
		out.add("LA");
		return out;
	}

	private List<String> buildLoinc15Value(){
		List<String> out = new ArrayList<>();
		out.add("035606033");
		return out;
	}

	private List<String> buildLoinc673Value(){
		List<String> out = new ArrayList<>();
		out.add("B01AX05");
		return out;
	}

	private List<String> buildLoincValue(){
		List<String> out = new ArrayList<>();
		out.add("87273-9");
		out.add("11369-6");
		out.add("30973-2");
		out.add("59778-1");
		out.add("30980-7");
		out.add("95715-9");
		out.add("59785-6");
		out.add("31044-1");
		out.add("75323-6");

		return out;
	}


	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronSinVACC" + File.separator + "sch" + File.separator +"schematron_singola_VACC v1.9.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_singola_VACC v1.9.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronSinVACC\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronSinVACC" + File.separator + "sch" + File.separator +"schematron_singola_VACC v1.9.sch");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_singola_VACC v1.9.sch",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);

		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronSinVACC\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {

			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);
		}
	}

	@Test
	@DisplayName("CDA OK XSLT")
	void cdaOKXslt() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronSinVACC" + File.separator + "xslt" + File.separator +"schematron_singola_VACC v1.9.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_singola_VACC v1.9.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronSinVACC\\OK");
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
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronSinVACC" + File.separator + "xslt" + File.separator +"schematron_singola_VACC v1.9.xslt");
		String schematronAsString = new String(schematron);
		String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", baseUrl);
		IReadableResource readableResource = new ReadableResourceInputStream("schematron_singola_VACC v1.9.xslt",new ByteArrayInputStream(schematronWithReplacesUrl.getBytes()));
		SchematronResourceXSLT schematronResource = new SchematronResourceXSLT(readableResource);

		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronSinVACC\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			boolean result = resultDTO.getFailedAssertions().size()>0;
			assertTrue(result);

		}
	}

}
