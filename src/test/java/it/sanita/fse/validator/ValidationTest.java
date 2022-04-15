package it.sanita.fse.validator;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.vavr.collection.List;
import it.sanita.fse.validator.config.Constants;
import it.sanita.fse.validator.dto.SchematronValidationResultDTO;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
import it.sanita.fse.validator.repository.entity.SchematronETY;
import it.sanita.fse.validator.service.facade.IValidationFacadeSRV;
import it.sanita.fse.validator.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {Constants.ComponentScan.BASE})
@ActiveProfiles(Constants.Profile.TEST)
public class ValidationTest {
	 
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
	
	@Autowired
	private IValidationFacadeSRV validationSRV;

    @Test
    @DisplayName("No error and 200 OK")
    void noErrorAnd200OK() {
    	byte[] cda = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda1.xml");
		ObjectMapper objectMapper = new ObjectMapper(); 
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

	    ValidationReqDTO requestBody = new ValidationReqDTO();
	    requestBody.setCda(new String(cda));
	    
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

    @Autowired
    private MongoTemplate mongoTemplate;
    @Test
    void validateSchematron() {
    	try {
    		byte[] cda = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda1.xml");
    		SchematronETY schematron = getSchematron();
    		SchematronValidationResultDTO reuslt = validationSRV.validateSemantic(new String(cda), schematron);
    		reuslt.getFailedAssertions();
    	} catch(Exception ex) {
    		System.out.println(ex);
    	}
    }
   
    private SchematronETY getSchematron() {
    	Query query = new Query();
    	return mongoTemplate.find(query, SchematronETY.class).get(0);
    			
    }
}
