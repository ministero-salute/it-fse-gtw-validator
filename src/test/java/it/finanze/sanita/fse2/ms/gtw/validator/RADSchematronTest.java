package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
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
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;
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
	 
	@BeforeEach
	void setup() {
		deleteDictionary();
		saveDictionaryFiles();
	}

	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "schematronFSE_RAD_v2.2.sch");
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.2.sch",new ByteArrayInputStream(schematron));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		schematronResource.setURIResolver(new ClasspathResourceURIResolver(dictionaryRepo));
		
		Map<String,byte[]> cdasOK = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\OK");
		for(Entry<String, byte[]> cdaOK : cdasOK.entrySet()) {
			log.info("File analyzed : " + cdaOK.getKey());
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaOK.getValue());
			assertEquals(0, resultDTO.getFailedAssertions().size());
			assertEquals(true, resultDTO.getValidSchematron());
			assertEquals(true, resultDTO.getValidXML());
		}
	}
	
	@Test
	@DisplayName("CDA KO")
	void cdaKO() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "schematronFSE_RAD_v2.2.sch");
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.2.sch",new ByteArrayInputStream(schematron));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		schematronResource.setURIResolver(new ClasspathResourceURIResolver(dictionaryRepo));
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\KO");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			log.info("File analyzed : " + cdaKO.getKey());
			
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			String failedAssertion = resultDTO.getFailedAssertions().stream().map(e->e.getText()).collect(Collectors.joining(",")).toUpperCase();
			
			String[] errors = cdaKO.getKey().split(" ")[2].split("\\.")[0].split("-");
			for(String error : errors) {
				String pattern = "ERRORE-"+error;
				boolean assertCont = failedAssertion.contains(pattern);
				if(!assertCont) {
					System.out.println("Stop");
				}
				assertTrue(assertCont);
			}
			assertEquals(errors.length, resultDTO.getFailedAssertions().size());
			assertEquals(true, resultDTO.getValidSchematron());
			assertEquals(false, resultDTO.getValidXML());
		}
	}
	
	@Test
	@DisplayName("CDA ERROR")
	void cdaError() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronRAD" + File.separator + "schematronFSE_RAD_v2.2.sch");
		IReadableResource readableResource = new ReadableResourceInputStream("schematronFSE_RAD_v2.2.sch",new ByteArrayInputStream(schematron));
		SchematronResourceSCH schematronResource = new SchematronResourceSCH(readableResource);
		schematronResource.setURIResolver(new ClasspathResourceURIResolver(dictionaryRepo));
		
		Map<String,byte[]> cdasKO = getSchematronFiles("src\\test\\resources\\Files\\schematronRAD\\ERROR");
		for(Entry<String, byte[]> cdaKO : cdasKO.entrySet()) {
			
			SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaSchematronFull(schematronResource, cdaKO.getValue());
			String failedAssertion = resultDTO.getFailedAssertions().stream().map(e->e.getText()).collect(Collectors.joining(",")).toUpperCase();
			
			log.info("File analyzed : " + cdaKO.getKey() + " Failed assertion : " + failedAssertion);
		 
		}
	}
 
	 
}