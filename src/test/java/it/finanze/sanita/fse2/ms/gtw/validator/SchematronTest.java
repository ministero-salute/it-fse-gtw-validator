//package it.finanze.sanita.fse2.ms.gtw.validator;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.nio.charset.StandardCharsets;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.test.context.ActiveProfiles;
//
//import com.helger.commons.io.resource.IReadableResource;
//import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
//import com.helger.schematron.xslt.SchematronResourceXSLT;
//
//import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
//import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
//import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
//import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
//import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
//import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
//import it.finanze.sanita.fse2.ms.gtw.validator.service.IValidationSRV;
//import it.finanze.sanita.fse2.ms.gtw.validator.singleton.ResetSingleton;
//import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
//import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
//import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;
//import lombok.extern.slf4j.Slf4j;
//
//
//
//@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
//@ActiveProfiles(Constants.Profile.TEST)
//public class SchematronTest {
//
//	@Autowired
//	private ISchematronRepo schematronRepo;
//
//	@Autowired
//	private IValidationSRV validationSRV;
//
//	@Autowired
//	private MongoTemplate mongoTemplate;
//	
//	@Test
//	@DisplayName("CDA OK")
//	void cdaOK() throws Exception {
//		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematron" + File.separator + "schematronFSE.sch.xsl");
//		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
//		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
//		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
//		
//		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
//		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
//		assertEquals(0, resultDTO.getFailedAssertions().size());
//		assertEquals(true, resultDTO.getValidSchematron());
//		assertEquals(true, resultDTO.getValidXML());
//	}
//
//	@Test
//	@DisplayName("CDA KO")
//	void cdaKO() throws Exception {
//		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematron" + File.separator + "schematronFSE.sch.xsl");
//		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
//		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
//		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
//		
//		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ko" + File.separator + "CDA2_Referto Medicina di Laboratorio Errore 6.xml");
//		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
//		assertEquals(true, resultDTO.getValidSchematron());
//		assertEquals(false, resultDTO.getValidXML());
//	}
//
//	@Test
//	@DisplayName("Schematron Validator Singleton")
//	void singletonTest() throws Exception {
//
//		dbSetup();
//
//		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);
//
//		ExtractedInfoDTO schematronInfoDTO = CDAHelper.extractSchematronInfo(cda);
////		SchematronETY schematronETY = validationSRV.findSchematron(schematronInfoDTO);
//
//		
//		validationSRV.validateSemantic(cda, schematronInfoDTO);
//		validationSRV.validateSemantic(cda, schematronInfoDTO);
//
//
//		Map<String,SchematronValidatorSingleton> mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(1, mapInstance.size());
////		assertNotNull(mapInstance.get(schematronInfoDTO.get));
//
//		// load a cda with different templateTd extension 
//
//		final String cda1 = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA_001.xml"), StandardCharsets.UTF_8);
//		ExtractedInfoDTO schematronInfoDTO1 = CDAHelper.extractSchematronInfo(cda1);
//		SchematronETY schematronETY1 = validationSRV.findSchematron(schematronInfoDTO1);
//		validationSRV.validateSemantic(cda1, schematronETY1);
//		mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(2, mapInstance.size());
//		assertNotNull(mapInstance.get(schematronInfoDTO1.getTemplateIdExtension()));
//
//		// update data ultimo aggiornamento
//
//		updateSchematronLastUpdateDate();
//
//		schematronETY1 = validationSRV.findSchematron(schematronInfoDTO1);
//		validationSRV.validateSemantic(cda1, schematronETY1);
//		mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(2, mapInstance.size());
//		assertNotNull(mapInstance.get(schematronInfoDTO1.getTemplateIdExtension()));
//
//		cleanDatabase();
//
//	}
//
//	@Test
//	@DisplayName("Multithread Schematron Validator Singleton")
//	void multithreadSingletonTest() throws Exception {
//		ResetSingleton.setPrivateField(SchematronValidatorSingleton.class, null,null, "mapInstance","instance");
//		dbSetup();
//		final int numberThreads = 4;
//
//		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml"), StandardCharsets.UTF_8);
//
//		ExtractedInfoDTO schematronInfoDTO = CDAHelper.extractSchematronInfo(cda);
//		SchematronETY schematronETY = validationSRV.findSchematron(schematronInfoDTO);
//		
//
//		try {
//			List<SingletonThread> threads = new ArrayList<>();
//			for (int i=0; i<numberThreads; i++) {
//				SingletonThread thread = new SingletonThread(i, cda, schematronETY);
//				threads.add(thread);
//				
//				thread.start();
//			}
//			
//			// Waiting for threads to stop
//			for (SingletonThread thread : threads) {
//				thread.join();
//			}
//		} catch (Exception e) {
//			log.error("Error while executing Jam Session", e);
//		}
//
//		Map<String,SchematronValidatorSingleton> mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(1, mapInstance.size());
//		assertNotNull(mapInstance.get(schematronInfoDTO.getTemplateIdExtension()));
//
//		// load a cda with different templateTd extension 
//
//		final String cda1 = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA_001.xml"), StandardCharsets.UTF_8);
//		ExtractedInfoDTO schematronInfoDTO1 = CDAHelper.extractSchematronInfo(cda1);
//		SchematronETY schematronETY1 = validationSRV.findSchematron(schematronInfoDTO1);
//
//		try {
//			List<SingletonThread> threads = new ArrayList<>();
//			for (int i=0; i<numberThreads; i++) {
//				SingletonThread thread = new SingletonThread(i, cda1, schematronETY1);
//				threads.add(thread);
//				
//				thread.start();
//			}
//			
//			// Waiting for threads to stop
//			for (SingletonThread thread : threads) {
//				thread.join();
//			}
//		} catch (Exception e) {
//			log.error("Error while executing Jam Session", e);
//		}
//
//
//		mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(2, mapInstance.size());
//		assertNotNull(mapInstance.get(schematronInfoDTO1.getTemplateIdExtension()));
//
//
//		// update data ultimo aggiornamento
//
//		updateSchematronLastUpdateDate();
//
//		schematronETY1 = validationSRV.findSchematron(schematronInfoDTO1);
//
//
//		try {
//			List<SingletonThread> threads = new ArrayList<>();
//			for (int i=0; i<numberThreads; i++) {
//				SingletonThread thread = new SingletonThread(i, cda1, schematronETY1);
//				threads.add(thread);
//				
//				thread.start();
//			}
//			
//			// Waiting for threads to stop
//			for (SingletonThread thread : threads) {
//				thread.join();
//			}
//		} catch (Exception e) {
//			log.error("Error while executing Jam Session", e);
//		}
//
//
//
//		mapInstance = SchematronValidatorSingleton.getMapInstance();
//		assertEquals(2, mapInstance.size());
//		assertNotNull(mapInstance.get(schematronInfoDTO1.getTemplateIdExtension()));
//
//		cleanDatabase();
//
//	}
//
//
//
//
//	void updateSchematronLastUpdateDate() throws ParseException {
//
//		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//        Date newDate = sdf.parse("01/01/2022");
//
//		Query query = new Query();
//        query.addCriteria(Criteria.where("template_id_extension").is("1.4"));
//
//        Update update = new Update();
//        update.set("data_ultimo_aggiornamento", newDate);
//        mongoTemplate.updateFirst(query, update, SchematronETY.class);
//
//	}
//
//
//	void dbSetup() {
//
//		Query query = new Query();
//        query.addCriteria(Criteria.where("template_id_extension").is("1.4"));
//		mongoTemplate.remove(query, SchematronETY.class);
//
//		query = new Query();
//		query.addCriteria(Criteria.where("name_schematron").is("2.16.840.1.113883.6.1.xml"));
//		SchematronETY ety = mongoTemplate.findOne(query, SchematronETY.class);
//
//		ety.setTemplateIdExtension("1.4");
//		ety.setId(null);
//		mongoTemplate.insert(ety);
//	}
//
//	
//    void cleanDatabase() {
//		Query query = new Query();
//        query.addCriteria(Criteria.where("template_id_extension").is("1.4"));
//		mongoTemplate.remove(query, SchematronETY.class);
//	}
//
//	class SingletonThread extends Thread {
//		private Integer id;
//		private String cda;
//		private SchematronETY schematronETY;
//		
//		public SingletonThread(Integer inID, String inCda, SchematronETY inSchematronETY) {
//			id = inID;
//			cda = inCda;
//			schematronETY = inSchematronETY;
//		}
//		
//		@Override
//		public void run() {
//			log.info("[THREAD - " + id + "] STARTING ACTION");
//			validationSRV.validateSemantic(cda, schematronETY);
//			log.info("[THREAD - " + id + "] STOP ACTION");
//		}
//	}
//
//
//	 
//}
