/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ValidationSRV; 



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles(Constants.Profile.TEST)
@DirtiesContext
@AutoConfigureMockMvc
class ValidationControllerTest extends AbstractTest {

    @BeforeEach
    void setup() {
        clearConfigurationItems();
        insertSchematron();
        insertSchema();
    }

    public static final Path TEST_FILE = Paths.get(
        "Files",
        "cda1.xml"
    );

    public static final Path TEST_FILE_ERR = Paths.get(
        "Files",
        "cda_ok",
        "Esempio CDA_002.xml"
    ); 
    
    
    @Autowired
    MockMvc mvc; 
    
    @MockBean
    private ValidationSRV validationSrv; 
    
    @SpyBean
    private MongoTemplate mongoTemplate; 
    
    
    @Test
    @DisplayName("Validation Controller - Test Success")
    void validationTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.VALID); 
    	ValidationRequestDTO validationRequest = new ValidationRequestDTO(); 
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	validationRequest.setCda(cda); 
    	validationRequest.setWorkflowInstanceId("wid");
    	SchematronValidationResultDTO schematronValidationResult = new SchematronValidationResultDTO(true, true, null, null); 
    	ObjectMapper objectMapper = new ObjectMapper(); 
    	
    	
    	when(validationSrv.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(validationSrv.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenReturn(schematronValidationResult); 
    	
    	when(validationSrv.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto); 
    	
	    
    	MockHttpServletRequestBuilder builder =
	            MockMvcRequestBuilders.post("http://localhost:8012/v1/validate").content(objectMapper.writeValueAsString(validationRequest)); 
	    
	    mvc.perform(builder
	            .contentType(MediaType.APPLICATION_JSON_VALUE))
	            .andExpect(status().is2xxSuccessful()); 
    	
    	
    } 
    
    @Test
    @DisplayName("Validation Controller - Invalid Syntactic Validation")
    void validationInvalidSyntacticTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID); 
    	Map<CDASeverityViolationEnum, List<String>> violations = new HashMap<CDASeverityViolationEnum, List<String>>(); 
    	ValidationResult vl = new ValidationResult(); 
    	vl.addWarning("testWarning"); 
    	violations.put(CDASeverityViolationEnum.WARN, vl.getWarnings()); 
    	validation.setViolations(violations); 
    	
    	ValidationRequestDTO validationRequest = new ValidationRequestDTO(); 
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	validationRequest.setCda(cda); 
    	SchematronValidationResultDTO schematronValidationResult = new SchematronValidationResultDTO(true, true, null, null); 
    	ObjectMapper objectMapper = new ObjectMapper(); 
    	
    	
    	when(validationSrv.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(validationSrv.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenReturn(schematronValidationResult); 
    	
    	when(validationSrv.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto); 
    	
	    
    	MockHttpServletRequestBuilder builder =
	            MockMvcRequestBuilders.post("http://localhost:8012/v1/validate").content(objectMapper.writeValueAsString(validationRequest)); 
	    
	    mvc.perform(builder
	            .contentType(MediaType.APPLICATION_JSON_VALUE))
	            .andExpect(status().is2xxSuccessful()); 
    	  	
    } 
    
    
    @Test
    @DisplayName("Validation Controller - Invalid Semantic Validation")
    void validationInvalidSemanticTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID); 
    	Map<CDASeverityViolationEnum, List<String>> violations = new HashMap<CDASeverityViolationEnum, List<String>>(); 
    	ValidationResult vl = new ValidationResult(); 
    	vl.addWarning("testWarning"); 
    	violations.put(CDASeverityViolationEnum.WARN, vl.getWarnings()); 
    	validation.setViolations(violations); 
    	
    	ValidationRequestDTO validationRequest = new ValidationRequestDTO(); 
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	validationRequest.setCda(cda); 
    	new SchematronValidationResultDTO(true, true, null, null); 
    	ObjectMapper objectMapper = new ObjectMapper(); 
    	
    	
    	when(validationSrv.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(validationSrv.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenThrow(new NoRecordFoundException("Error")); 
    	
    	when(validationSrv.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto); 
    	
	    
    	MockHttpServletRequestBuilder builder =
	            MockMvcRequestBuilders.post("http://localhost:8012/v1/validate").content(objectMapper.writeValueAsString(validationRequest)); 
	    
	    mvc.perform(builder
	            .contentType(MediaType.APPLICATION_JSON_VALUE))
	            .andExpect(status().is2xxSuccessful()); 
    	
    	
    } 
    
    
    void addSchemaVersion(){

        List<SchemaETY> schemas = mongoTemplate.findAll(SchemaETY.class);

        for(SchemaETY schema : schemas){
            schema.setId(null);
            schema.setTypeIdExtension("1.4");
        }

        mongoTemplate.insertAll(schemas);

    } 
    
    
    @Test
    @DisplayName("Inspect Singletons Test")
    void getSingletonsTest() throws Exception {
    	
    	MockHttpServletRequestBuilder builder =
	            MockMvcRequestBuilders.get("http://localhost:8012/v1/singletons"); 
	    
	    mvc.perform(builder
	            .contentType(MediaType.APPLICATION_JSON_VALUE))
	            .andExpect(status().is2xxSuccessful());     	
    	
    } 

    

}
