package it.sanita.fse.validator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.sanita.fse.validator.cda.ValidationResult;
import it.sanita.fse.validator.config.Constants;
import it.sanita.fse.validator.dto.CDAValidationDTO;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
import it.sanita.fse.validator.repository.entity.SchematronETY;
import it.sanita.fse.validator.service.facade.IValidationFacadeSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class ValidationTest {
	
	private static String cdaError = "Pippo";
	private static String cda = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<ClinicalDocument xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:hl7-org:v3\">\r\n"
			+ "<realmCode code=\"IT\"/>\r\n"
			+ "<typeId extension=\"POCD_HD000040\" root=\"2.16.840.1.113883.1.3\"/>\r\n"
			+ "<templateId root=\"2.16.840.1.113883.2.9.2.80.3.1.10.4\" extension=\"2018.09\"/>\r\n"
			+ "<templateId root=\"2.16.840.1.113883.2.9.10.1.5\" extension=\"2018.09\"/>\r\n"
			+ "<id displayable=\"true\" extension=\"10580005322718615V1\" root=\"2.16.840.1.113883.2.9.2.80.3.1.4.4\"/>\r\n"
			+ "<code code=\"34105-7\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" displayName=\"Discharge summarization note\">\r\n"
			+ "<translation code=\"LED\" codeSystem=\"2.16.840.1.113883.2.9.2.80.3.1.6.2\" codeSystemName=\"Tipologie documento SOLE\" displayName=\"Lettera di Dimissione\">\r\n"
			+ "<qualifier> \r\n"
			+ "<name code=\"PR\" codeSystem=\"2.16.840.1.113883.2.9.2.80.3.1.6.1\" codeSystemName=\"SOLE\" displayName=\"Priorita' Referto\"/>\r\n"
			+ "<value code=\"PN\" codeSystem=\"2.16.840.1.113883.2.9.2.80.3.1.6.1\" codeSystemName=\"SOLE\" displayName=\"Priorita Normale\"/>\r\n"
			+ "</qualifier>\r\n"
			+ "</translation>\r\n"
			+ "</code><title>Lettera di Dimissione</title>\r\n"
			+ "<effectiveTime value=\"20220202144759\"/>\r\n"
			+ "<confidentialityCode code=\"N\" codeSystem=\"2.16.840.1.113883.5.25\" codeSystemName=\"HL7 Confidentiality\" displayName=\"Normal\">\r\n"
			+ "		<translation code=\"AN\" codeSystem=\"2.16.840.1.113883.2.9.2.80.3.1.6.1\" codeSystemName=\"SOLE\" displayName=\"Accesso Normale\"/>\r\n"
			+ "	</confidentialityCode>\r\n"
			+ "<languageCode code=\"it-IT\"/>\r\n"
			+ "<setId extension=\"10580005322718615V1\" root=\"2.16.840.1.113883.2.9.2.80.3.1.4.4\" assigningAuthorityName=\"Test SOLE\"/>\r\n"
			+ "<versionNumber value=\"1\"/>\r\n"
			+ "<recordTarget>\r\n"
			+ "		<patientRole>\r\n"
			+ "			<id assigningAuthorityName=\"Ministero Economia e Finanze\" displayable=\"true\" extension=\"SSSMNN75B01F257L\" root=\"2.16.840.1.113883.2.9.4.3.2\"/>\r\n"
			+ "			<addr use=\"H\">\r\n"
			+ "				<country>100</country>\r\n"
			+ "				<county>BO</county>\r\n"
			+ "				<city>BOLOGNA</city>\r\n"
			+ "				<postalCode>40026</postalCode>\r\n"
			+ "				<streetAddressLine>VIA DELIBERTIS 4</streetAddressLine>\r\n"
			+ "				<censusTract>037032</censusTract>\r\n"
			+ "			</addr>\r\n"
			+ "			<addr use=\"HP\">\r\n"
			+ "				<country>100</country>\r\n"
			+ "				<county>BO</county>\r\n"
			+ "				<city>IMOLA</city>\r\n"
			+ "				<postalCode>40026</postalCode>\r\n"
			+ "				<streetAddressLine>VIA DELIBERTIS 4</streetAddressLine>\r\n"
			+ "				<censusTract>037032</censusTract>\r\n"
			+ "			</addr>\r\n"
			+ "			<patient>\r\n"
			+ "				<name>\r\n"
			+ "					<family>ASSISTITOPROVA</family>\r\n"
			+ "					<given>MODENA UNO</given>\r\n"
			+ "				</name>\r\n"
			+ "				<administrativeGenderCode code=\"M\" codeSystem=\"2.16.840.1.113883.5.1\" codeSystemName=\"HL7 AdministrativeGender\" codeSystemVersion=\"1.0\" displayName=\"Maschio\"/>\r\n"
			+ "				<birthTime value=\"19750201\"/>\r\n"
			+ "				<birthplace>\r\n"
			+ "					<place>\r\n"
			+ "						<addr>\r\n"
			+ "							<country>100</country>\r\n"
			+ "							<city>BOLOGNA(BO)</city>\r\n"
			+ "							<censusTract>037006</censusTract>\r\n"
			+ "						</addr>\r\n"
			+ "					</place>\r\n"
			+ "				</birthplace>\r\n"
			+ "			</patient>\r\n"
			+ "		</patientRole>\r\n"
			+ "	</recordTarget>\r\n"
			+ "<author typeCode=\"AUT\">\r\n"
			+ "		<time value=\"20220202144759\"/>\r\n"
			+ "		<assignedAuthor>\r\n"
			+ "			<id assigningAuthorityName=\"Ministero Economia e Finanze\" displayable=\"true\" extension=\"MDCBGN54A01A944D\" root=\"2.16.840.1.113883.2.9.4.3.2\"/>\r\n"
			+ "			<assignedPerson>\r\n"
			+ "				<name>\r\n"
			+ "					<given>BOLOGNA UNO</given>\r\n"
			+ "					<family>MEDICOPROVA</family>\r\n"
			+ "				</name>\r\n"
			+ "			</assignedPerson>\r\n"
			+ "		</assignedAuthor>\r\n"
			+ "	</author>\r\n"
			+ "<custodian>\r\n"
			+ "		<assignedCustodian>\r\n"
			+ "			<representedCustodianOrganization>\r\n"
			+ "				<id assigningAuthorityName=\"AUSL DI BOLOGNA\" extension=\"080105\" root=\"2.16.840.1.113883.2.9.4.1.1\"/>\r\n"
			+ "				<name/>\r\n"
			+ "				<telecom/>\r\n"
			+ "				<addr>\r\n"
			+ "					<country/>\r\n"
			+ "					<county/>\r\n"
			+ "					<city/>\r\n"
			+ "					<postalCode/>\r\n"
			+ "					<streetName/>\r\n"
			+ "				</addr>\r\n"
			+ "			</representedCustodianOrganization>\r\n"
			+ "		</assignedCustodian>\r\n"
			+ "	</custodian>\r\n"
			+ "<legalAuthenticator>\r\n"
			+ "		<time value=\"20220202144759\"/>\r\n"
			+ "		<signatureCode code=\"S\"/>\r\n"
			+ "		<assignedEntity>\r\n"
			+ "			<id displayable=\"true\" extension=\"MDCBGN54A01A944D\" root=\"2.16.840.1.113883.2.9.4.3.2\"/>\r\n"
			+ "			<assignedPerson>\r\n"
			+ "				<name>\r\n"
			+ "					<prefix/>\r\n"
			+ "					<given>BOLOGNA UNO</given>\r\n"
			+ "					<family>MEDICOPROVA</family>\r\n"
			+ "				</name>\r\n"
			+ "			</assignedPerson>\r\n"
			+ "		</assignedEntity>\r\n"
			+ "	</legalAuthenticator>\r\n"
			+ "<inFulfillmentOf>\r\n"
			+ "		<order classCode=\"ACT\" moodCode=\"RQO\">\r\n"
			+ "			<id extension=\"1015961\" root=\"2.16.840.1.113883.2.9.3.21.100.100.2\"/>\r\n"
			+ "			<priorityCode code=\"R\" codeSystem=\"2.16.840.1.113883.5.7\" codeSystemName=\"HL7 ActPriority\" displayName=\"routine\"/>\r\n"
			+ "		</order>\r\n"
			+ "	</inFulfillmentOf>\r\n"
			+ "<componentOf>\r\n"
			+ "		<encompassingEncounter>\r\n"
			+ "<id root=\"2.16.840.1.113883.2.9.2.80105.4.6\" extension=\"718615\" assigningAuthorityName=\"Azienda\" displayable=\"true\"/>\r\n"
			+ "                   <effectiveTime>\r\n"
			+ "                       <low value=\"20220201144759+0100\"/>\r\n"
			+ "                       <high value=\"20220202144759+0100\"/>\r\n"
			+ "                   </effectiveTime>\r\n"
			+ "			<location>\r\n"
			+ "				<healthCareFacility>\r\n"
			+ "					<id root=\"2.16.840.1.113883.2.9.4.1.6\" extension=\"8000530110601\"/>\r\n"
			+ "					<location>\r\n"
			+ "						<name>Lettera di dimissione di test</name>\r\n"
			+ "					</location>\r\n"
			+ "					<serviceProviderOrganization>\r\n"
			+ "						<id root=\"2.16.840.1.113883.2.9.4.1.2\" extension=\"800053\" assigningAuthorityName=\"Ministero della Salute\"/>\r\n"
			+ "						<asOrganizationPartOf>\r\n"
			+ "							<id root=\"2.16.840.1.113883.2.9.4.1.1\" extension=\"080105\"/>\r\n"
			+ "						</asOrganizationPartOf>\r\n"
			+ "					</serviceProviderOrganization>\r\n"
			+ "				</healthCareFacility>\r\n"
			+ "			</location>\r\n"
			+ "		</encompassingEncounter>\r\n"
			+ "	</componentOf>\r\n"
			+ "	<component>\r\n"
			+ "		<structuredBody>\r\n"
			+ "<component>\r\n"
			+ "	<section>\r\n"
			+ "		<code code=\"46241-6\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" codeSystemVersion=\"2.19\" displayName=\" Diagnosi di Accettazione \"/>\r\n"
			+ "		<title>Motivo del ricovero</title>\r\n"
			+ "		<text>\r\n"
			+ "			<list>\r\n"
			+ "				<item>\r\n"
			+ "					<content ID=\"DIAG-1\">Disturbo di panico</content>\r\n"
			+ "				</item>\r\n"
			+ "				<item>\r\n"
			+ "					<content ID=\"DIAG-2\">Ipertiroidismo</content>\r\n"
			+ "				</item>\r\n"
			+ "			</list>\r\n"
			+ "		</text>\r\n"
			+ "	</section>\r\n"
			+ "</component><component>\r\n"
			+ "	<section>\r\n"
			+ "		<code code=\"8648-8\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" codeSystemVersion=\"2.19\" displayName=\"Decorso ospedaliero\"/>\r\n"
			+ "		<title> Decorso Ospedaliero </title>\r\n"
			+ "		<text>\r\n"
			+ "			<paragraph>\r\n"
			+ " Il paziente giungeva alla nostra attenzione sintomatico per\r\n"
			+ "scompenso cardiaco acuto. Durante il ricovero e' stato ottenuto un\r\n"
			+ "ripristino dello stato di compenso emodinamico mediante trattamento\r\n"
			+ "farmacologico intensivo.\r\n"
			+ "			</paragraph>\r\n"
			+ "		</text>\r\n"
			+ "	</section>\r\n"
			+ "</component>\r\n"
			+ "<component>\r\n"
			+ "	<section>\r\n"
			+ "		<code code=\"11535-2\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" codeSystemVersion=\"2.19\" displayName=\" Diagnosi di Dimissione \"/>\r\n"
			+ "		<title>Condizioni del paziente e diagnosi alla dimissione</title>\r\n"
			+ "		<text>\r\n"
			+ "Paziente in cattivo compenso emodinamico per insufficenza della\r\n"
			+ "Valvola Aortica di grado severo. Non in grado di deambulare\r\n"
			+ "correttamente, necessita di sedia a rotelle in ore serali. Si\r\n"
			+ "segnala inizio di sindrome paranoica e COPD.\r\n"
			+ "		</text>\r\n"
			+ "           <entry>\r\n"
			+ "		<observation classCode=\"OBS\" moodCode=\"EVN\">\r\n"
			+ "			<code code=\"8651-2\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" displayName=\"Diagnosi di Dimissione\" />\r\n"
			+ "			<value xsi:type=\"CD\" code=\"Cod_Diagn1\" codeSystem=\"2.16.840.1.113883.6.103\" codeSystemName=\"ICD9CM\" displayName=\"Diagnosi ext\">\r\n"
			+ "				<translation code=\"7654.321\" codeSystem= \"2.16.840.1.113883.6.99.99.99\" codeSystemName=\"Catalogo Universitario\" displayName=\"Diagnosi ext\"/>\r\n"
			+ "			</value>\r\n"
			+ "		</observation>\r\n"
			+ "           </entry>\r\n"
			+ "	</section>\r\n"
			+ "</component>		</structuredBody>\r\n"
			+ "	</component></ClinicalDocument>";
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Test
    @DisplayName("No error and 200 OK")
    void noErrorAnd200OK() {
    	
		ObjectMapper objectMapper = new ObjectMapper(); 
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

	    ValidationReqDTO requestBody = new ValidationReqDTO();
	    requestBody.setCda("cda");
	    
		String requestString = "";
		try {
			requestString = objectMapper.writeValueAsString(requestBody);
		} catch (JsonProcessingException e) {
			log.error("Errore durante la conversione in json : ",e);
		}

		String baseURL = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + webServerAppCtxt.getServletContext().getContextPath();

		HttpEntity<String> entity = new HttpEntity<String>(requestString, headers);
		ValidationResDTO result = null;

		Boolean bCheck = true;
		try {
			result = restTemplate.postForObject(baseURL + "/v1/validate", entity, ValidationResDTO.class);
		} catch (Exception e) {
			log.error("Errore in fase di invocazione", e);
			bCheck = false;
		}
		assertTrue(bCheck, "Non deve ritornare un codice diverso da 2XX");
		assertNull(result.getError(), "Non deve essere presente alcun errore.");
    }
     
//    @Test
//    void vi() throws Exception {
//    	String lastVersion = schemaRepo.findLastVersion();
//    	SchemaETY schemaFather = schemaRepo.findFatherXsd(lastVersion);
//    	cdaValidator.validateXsd(schemaFather.getContentSchema().getData(), cda);
//    } 
    
    @Autowired
    private IValidationFacadeSRV validationSRV;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Test
    void schematronValidation() {
    	String version = "1.0.0";
    	updateVersionSchematron(version);

    	String cdaClean = cda.replaceAll("\r\n", "");
    	System.out.println("Stop");
    	
    	validationSRV.validateSemantic(cda, version);
    	validationSRV.validateSemantic(cda, version);
    	version = "2.0.0";
    	updateVersionSchematron(version);
    	validationSRV.validateSemantic(cda, version);
    }
    
    private void updateVersionSchematron(String version) {
    	Query query = new Query();
    	Update update = new Update();
    	update.set("version", version);
    	mongoTemplate.updateFirst(query, update, SchematronETY.class);
    			
    }
    
    
    @Test
    void schemaValidation() {
    	try {
    		String version = "1.0.0";
    		String cdaClean = cda.replaceAll("\r\n", "");
    		CDAValidationDTO vResult = validationSRV.validateSyntactic(cdaClean, version);
    		System.out.println("Stop  ");
    	} catch(Exception ex) {
    		System.out.println("Stop");
    	}
    }

}
