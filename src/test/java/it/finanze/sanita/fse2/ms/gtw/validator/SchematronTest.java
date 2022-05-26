package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.ResetSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class SchematronTest extends AbstractTest {

	@Autowired
	ISchematronRepo schematronRepo;

	@Autowired
	IValidationSRV validationSRV;
	
	@BeforeEach
	void setup() {
		mongoTemplate.remove(new Query(), SchematronETY.class);
	}

	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {

		insertSchematron();
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronData" + File.separator + "schematronFSE.sch.xsl");
		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
		
		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
		assertEquals(0, resultDTO.getFailedAssertions().size());
		assertEquals(true, resultDTO.getValidSchematron());
		assertEquals(true, resultDTO.getValidXML());
	}

	@Test
	@DisplayName("CDA KO")
	void cdaKO() throws Exception {

		insertSchematron();

		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronData" + File.separator + "schematronFSE.sch.xsl");
		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
		
		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ko" + File.separator + "CDA2_Referto Medicina di Laboratorio Errore 6.xml");
		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
		assertEquals(true, resultDTO.getValidSchematron());
		assertEquals(false, resultDTO.getValidXML());
	}

	@Test
	@Disabled("Executing this test in a pipeline will fail because mvn cannot read correctly a resource")
	@DisplayName("Schematron Validator Singleton")
	void singletonTest() throws Exception {

		insertSchematron();
		dbSetup();
		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);

		ExtractedInfoDTO schematronInfoDTO = CDAHelper.extractInfo(cda);
		
		validationSRV.validateSemantic(cda, schematronInfoDTO);
		validationSRV.validateSemantic(cda, schematronInfoDTO);

		Map<String,SchematronValidatorSingleton> mapInstance = SchematronValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());
		assertEquals("1.3", mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()).getTemplateIdExtension());
		assertNotNull(mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()));

		// load a cda with different templateTd extension 
		updateSchematronTemplateExtension("1.4");
		validationSRV.validateSemantic(cda, schematronInfoDTO);
		mapInstance = SchematronValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());
		assertEquals("1.4", mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()).getTemplateIdExtension());
		assertNotNull(mapInstance.get(schematronInfoDTO.getTemplateIdSchematron())); 
		cleanDatabase("1.4");
	}
	

	@Test
	@DisplayName("Multithread Schematron Validator Singleton")
	void multithreadSingletonTest() throws Exception {
		ResetSingleton.setPrivateField(SchematronValidatorSingleton.class, null,null, "mapInstance","instance");
		
		insertSchematron();
		dbSetup();
		final int numberThreads = 4;

		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);

		ExtractedInfoDTO schematronInfoDTO = CDAHelper.extractInfo(cda);

		try {
			List<SingletonThread> threads = new ArrayList<>();
			for (int i=0; i<numberThreads; i++) {
				SingletonThread thread = new SingletonThread(i, cda, schematronInfoDTO);
				threads.add(thread);
				thread.start();
			}
			
			// Waiting for threads to stop
			for (SingletonThread thread : threads) {
				thread.join();
			}
		} catch (Exception e) {
			log.error("Error while executing Jam Session", e);
		}

		Map<String,SchematronValidatorSingleton> mapInstance = SchematronValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());
		assertEquals("1.3", mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()).getTemplateIdExtension());
		assertNotNull(mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()));

		// load a cda with different templateTd extension 
		updateSchematronTemplateExtension("1.4");

		try {
			List<SingletonThread> threads = new ArrayList<>();
			for (int i=0; i<numberThreads; i++) {
				SingletonThread thread = new SingletonThread(i, cda, schematronInfoDTO);
				threads.add(thread);
				thread.start();
			}
			
			// Waiting for threads to stop
			for (SingletonThread thread : threads) {
				thread.join();
			}
		} catch (Exception e) {
			log.error("Error while executing Jam Session", e);
		}

		mapInstance = SchematronValidatorSingleton.getMapInstance();
		assertEquals(1, mapInstance.size());
		assertEquals("1.4", mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()).getTemplateIdExtension());
		assertNotNull(mapInstance.get(schematronInfoDTO.getTemplateIdSchematron()));
		cleanDatabase("1.4");

	}

	void updateSchematronTemplateExtension(final String extension) throws ParseException {
		
		Query query = new Query();
        List<SchematronETY> schematronList = mongoTemplate.find(query, SchematronETY.class);
        for(SchematronETY ety : schematronList) {
        	ety.setId(null);
        	ety.setTemplateIdExtension(extension);
        }
        
        mongoTemplate.insertAll(schematronList);
	}


	void dbSetup() {
		deleteSpecificExtensionSchematron("1.4");
	}
	
	private void deleteSpecificExtensionSchematron(String extension) {
		Query query = new Query();
        query.addCriteria(Criteria.where("template_id_extension").is(extension));
		mongoTemplate.remove(query, SchematronETY.class);
	}

	
    void cleanDatabase(String extension) {
    	deleteSpecificExtensionSchematron(extension);
	}

	class SingletonThread extends Thread {
		private Integer id;
		private String cda;
		private ExtractedInfoDTO extractedInfoDTO;
		
		public SingletonThread(Integer inID, String inCda, ExtractedInfoDTO inExtractedInfoDTO) {
			id = inID;
			cda = inCda;
			extractedInfoDTO = inExtractedInfoDTO;
		}
		
		@Override
		public void run() {
			log.info("[THREAD - " + id + "] STARTING ACTION");
			validationSRV.validateSemantic(cda, extractedInfoDTO);
			log.info("[THREAD - " + id + "] STOP ACTION");
		}
	}
	 
}
