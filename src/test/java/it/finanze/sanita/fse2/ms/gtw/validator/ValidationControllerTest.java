/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ValidationSRV;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.validator.base.MockRequests.validate;
import static it.finanze.sanita.fse2.ms.gtw.validator.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(PER_CLASS)
@ActiveProfiles(TEST)
@AutoConfigureMockMvc
class ValidationControllerTest extends AbstractTest {

    @BeforeEach
    void setup() {
        clearConfigurationItems();
        insertSchematron();
        insertSchema();
    }
    
    @Autowired
    private MockMvc mvc;
    
    @MockBean
    private ValidationSRV service;
    
    @Test
    @DisplayName("Validation Controller - Test Success")
    void validationTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.VALID); 
    	ValidationRequestDTO req = new ValidationRequestDTO();
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	req.setCda(cda);
    	req.setWorkflowInstanceId("wid");
    	SchematronValidationResultDTO schematronValidationResult = new SchematronValidationResultDTO(true, true, null, null); 

    	
    	when(service.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(service.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenReturn(schematronValidationResult); 
    	
    	when(service.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto);

		when(service.getStructureObjectID(anyString())).thenReturn(Pair.of("test", "test"));

	    mvc.perform(validate(req)).andExpect(status().is2xxSuccessful());
    	
    } 
    
    @Test
    @DisplayName("Validation Controller - Invalid Syntactic Validation")
    void validationInvalidSyntacticTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID); 
    	Map<CDASeverityViolationEnum, List<String>> violations = new HashMap<>();
    	ValidationResult vl = new ValidationResult(); 
    	vl.addWarning("testWarning"); 
    	violations.put(CDASeverityViolationEnum.WARN, vl.getWarnings()); 
    	validation.setViolations(violations); 
    	
    	ValidationRequestDTO req = new ValidationRequestDTO();
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	req.setCda(cda);
    	SchematronValidationResultDTO schematronValidationResult = new SchematronValidationResultDTO(true, true, null, null); 

    	
    	when(service.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(service.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenReturn(schematronValidationResult); 
    	
    	when(service.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto); 

	    mvc.perform(validate(req)).andExpect(status().is2xxSuccessful());
    } 
    
    
    @Test
    @DisplayName("Validation Controller - Invalid Semantic Validation")
    void validationInvalidSemanticTest() throws Exception {
    	
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		
    	CDAValidationDTO validation = new CDAValidationDTO(CDAValidationStatusEnum.NOT_VALID); 
    	Map<CDASeverityViolationEnum, List<String>> violations = new HashMap<>();
    	ValidationResult vl = new ValidationResult(); 
    	vl.addWarning("testWarning"); 
    	violations.put(CDASeverityViolationEnum.WARN, vl.getWarnings()); 
    	validation.setViolations(violations); 
    	
    	ValidationRequestDTO req = new ValidationRequestDTO();
    	VocabularyResultDTO vocabularyResultDto = new VocabularyResultDTO(); 
    	vocabularyResultDto.setValid(true); 
    	req.setCda(cda);
    	new SchematronValidationResultDTO(true, true, null, null); 

    	
    	when(service.validateSyntactic(anyString(), anyString()))
    		.thenReturn(validation); 
    	
    	when(service.validateSemantic(anyString(), any(ExtractedInfoDTO.class)))
    		.thenThrow(new NoRecordFoundException("Error")); 
    	
    	when(service.validateVocabularies(anyString(),anyString()))
			.thenReturn(vocabularyResultDto);
	    
	    mvc.perform(validate(req)).andExpect(status().is2xxSuccessful());
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
