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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.sanita.fse.validator.config.Constants;
import it.sanita.fse.validator.dto.request.ValidationReqDTO;
import it.sanita.fse.validator.dto.response.ValidationResDTO;
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

}
