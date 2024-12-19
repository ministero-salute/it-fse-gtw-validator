
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

import com.helger.schematron.ISchematronResource;
import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.validator.base.AbstractTest;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IEngineRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.SchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.TerminologySRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidationTest extends AbstractTest {

	@Autowired
	IValidationFacadeSRV service;
	
	@SpyBean
	TerminologySRV terminologySRV; 
	
	@SpyBean
	private IDictionaryRepo codeSystemRepo; 
	
	@SpyBean
	private IEngineRepo engines;
	
	@MockBean
	private SchematronRepo schematronRepo;
	
	@Mock
	private ISchematronResource aResSCH;

	@MockBean
	private ConfigSRV config;
	
	
	@BeforeEach
	void setup() {
		when(config.isAuditEnable()).thenReturn(true);
		clearConfigurationItems();
		insertSchema();
		insertSchematron();
	}

	@Test
	void shouldReturnValidWhenCDAIsValid() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		String version = "1.3";
		
		log.info("Testing with version {}", version);
		CDAValidationDTO firstResult = service.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.VALID, firstResult.getStatus(), "The validation should have been completed correctly");
		
		CDAValidationDTO secondResult = service.validateSyntactic(cda, version);
		assertEquals(firstResult.getStatus(), secondResult.getStatus(), "Repeating validation should have not changed the result");

		
		log.info("Testing with version {}", version);
		
		CDAValidationDTO out = service.validateSyntactic(cda, "2.0.0");
		assertNotNull(out.getMessage()); 
	}

	@Test
	void shouldReturnNotValidWhenCDAIsInvalid() {

		final String cda = "<realmCode code=\"1\"/>";
		String version = "1.3";
		
		log.info("Testing with version {}", version);
		CDAValidationDTO firstResult = service.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, firstResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getMessage());
	
		CDAValidationDTO secondResult = service.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, secondResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getMessage());
	}

	@Test
	void shouldThrowBusinessExceptionWhenSchemaisNull() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		final String version = "3.0.0";
		
		CDAValidationDTO res = service.validateSyntactic(cda, version);
		assertEquals(String.format("Schema with version %s not found on database.", version), res.getMessage());
	}
 

	@Test
	void shouldReturnWhenCDAVocabularyIsInvalid() {
		final String cda = new String(getFileFromInternalResources(
			"Files/cda_ok/Esempio_CDA_003.xml"
		), StandardCharsets.UTF_8);

		VocabularyResultDTO res = service.validateVocabularies(cda, "wid");
		assertFalse(res.getValid(), "The vocabulary validation should be falsy");

		res = service.validateVocabularies("", "");
		assertFalse(res.getValid(), "Repeating vocabulary validation should be falsy");
	}
	
	@Test
	void validateSemanticValidSchematronTest() {
		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8); 
		
		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(cda, null);
		
		SchematronETY ety = new SchematronETY(); 
		ety.setId("TEST_ID"); 
		ety.setNameSchematron("schematronFSE_LDO_V3.5.sch");
		ety.setContentSchematron(new Binary("SGVsbG8gV29ybGQh".getBytes())); 
		ety.setTemplateIdRoot("2.16.840.1.113883.2.9.10.1.5");
		ety.setVersion("1.0"); 
		
		when(schematronRepo.findByRootAndSystem(anyString(), nullable(String.class))).thenReturn(ety);
		when(aResSCH.isValidSchematron()).thenReturn(true); 
		
		assertDoesNotThrow(() -> service.validateSemantic(cda, infoDTO));
		
	}
	@Test
	void validateSemanticInvalidSchematronTest() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		
		SchematronETY ety = new SchematronETY(); 
		ety.setId("TEST_ID"); 
		ety.setNameSchematron("TEST_NAME");
		ety.setContentSchematron(new Binary("Hello World!".getBytes())); 
		ety.setTemplateIdRoot("TEST_ROOT");
		ety.setVersion("1.2"); 
		
		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(cda, null);
		
		when(schematronRepo.findByRootAndSystem(anyString(), nullable(String.class))).thenReturn(ety);
		
		assertDoesNotThrow(() -> service.validateSemantic(cda, infoDTO));
		
		// Now Singleton is valorized  
		assertDoesNotThrow(() -> service.validateSemantic(cda, infoDTO));
		
	} 
	
	@Test
	void noSchematronFoundSemanticExceptionTest() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		String version = "1.3";
		
		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(cda, null);
		
		log.info("Testing with version {}", version);
		
		SchematronValidationResultDTO res = service.validateSemantic(cda, infoDTO);
		assertEquals("Schematron with template id root 2.16.840.1.113883.2.9.2.80.3.1.10.4 not found on database.", res.getMessage());
		assertEquals(false, res.getValidSchematron());
	} 
	
	@Test
	void validationTest() {
        final String cda = new String(getFileFromInternalResources(
                "Files/cda_ok/Esempio CDA_002.xml"
            ), StandardCharsets.UTF_8);

            DictionaryETY ety = new DictionaryETY(); 
            ety.setSystem("2.16.840.1.113883.6.1");
            ety.setVersion("1.3");
            ety.setReleaseDate(new Date()); 
            
            List<DictionaryETY> dictionaries = new ArrayList<>();
            dictionaries.add(ety);

            when(codeSystemRepo.getCodeSystems()).thenReturn(dictionaries);
            
            assertDoesNotThrow(() -> service.validateVocabularies(cda, "wid"));
            assertDoesNotThrow(() -> CDAHelper.extractTerminology(cda)); 
            
            // --------- Test - Throws Exception --------- 
            assertThrows(Exception.class, () -> service.validateVocabularies(null, "wid"));
            
            // --------- Test - Validation SRV ---------
			EngineMap map = new EngineMap();
			map.setOid("TEST-OID");
			map.setRoot("2.16.840.1.113883.6.1");
			map.setVersion("0.1");
            EngineETY engine = new EngineETY();
            engine.setRoots(map);
            
            when(engines.getLatestEngine()).thenReturn(engine);
            assertDoesNotThrow(() -> service.getStructureObjectID("2.16.840.1.113883.6.1"));

            when(engines.getLatestEngine()).thenThrow(new BusinessException("Error"));
            assertThrows(BusinessException.class, () -> service.getStructureObjectID("2.16.840.1.113883.6.1"));
	}

	@Test
	void engineRetrievalTest() {
		// Engine mock
		EngineMap map = new EngineMap();
		map.setRoot("2.16.840.1.113883.6.2"); // <= Erroneous
		EngineETY engine = new EngineETY();
		engine.setRoots(map);
		// Mock knowledge
		when(engines.getLatestEngine()).thenReturn(null);
		// Execute
		assertThrows(NoRecordFoundException.class, () -> service.getStructureObjectID("2.16.840.1.113883.6.1"));
		// Mock knowledge
		when(engines.getLatestEngine()).thenReturn(engine);
		// Execute
		assertThrows(NoRecordFoundException.class, () -> service.getStructureObjectID("2.16.840.1.113883.6.1"));
		// Reset
		engine.setId("engine-id");
		map.setOid("map-id");
		map.setRoot("2.16.840.1.113883.6.1");
		// Mock knowledge
		when(engines.getLatestEngine()).thenReturn(engine);
		// Execute
		assertDoesNotThrow(() -> {
			Pair<String, String> id = service.getStructureObjectID("2.16.840.1.113883.6.1");
			assertEquals(id.getKey(), engine.getId());
			assertEquals(id.getValue(), map.getOid());
		});
		// Mock knowledge
		when(engines.getLatestEngine()).thenThrow(new MongoException("Test error"));
		// Execute
		assertThrows(BusinessException.class, () -> service.getStructureObjectID("2.16.840.1.113883.6.1"));
	}

}
