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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilityTest {

	@MockBean
	private ConfigSRV config;

	@Test
	@DisplayName("Code System Utility Test")
	void codeSystemUtilityTest() {
		when(config.isAuditEnable()).thenReturn(true);

    	List<DictionaryETY> dictList = new ArrayList<DictionaryETY>(); 
    	DictionaryETY ety = new DictionaryETY(); 
    	DictionaryETY etySecond = new DictionaryETY(); 
    	
    	List<CodeDTO> codeList = new ArrayList<CodeDTO>(); 
    	CodeDTO dto = new CodeDTO("code", "codeSystem", "codeSystemName", "version", "displayName"); 
    	CodeDTO dto2 = new CodeDTO("code2", "codeSystem2", "codeSystemName2", "version2", "displayName"); 
    	CodeDTO dto3 = new CodeDTO("code3", "codeSystem3", "codeSystemName3", "version2", "displayName"); 
    	codeList.add(dto); 
    	codeList.add(dto2); 
    	codeList.add(dto3); 

    

    	ety.setSystem("system"); 
    	ety.setVersion("version");
    	ety.setWhiteList(true);
    	ety.setReleaseDate(new Date()); 
    	etySecond.setSystem("system"); 
    	etySecond.setVersion("version");
    	etySecond.setWhiteList(true);
    	etySecond.setReleaseDate(new Date()); 
    	dictList.add(ety); 
    	dictList.add(etySecond); 
    	
    	List<String> codeSystems = new ArrayList<String>(); 
    	codeSystems.add("2.16.840.1.113883.6.1.AL"); 
    	codeSystems.add("2.16.840.1.113883.6.999.AL"); 
    	
    	CodeSystemSnapshotDTO dtoCodeSystem = new CodeSystemSnapshotDTO(dictList); 
    	
    	assertDoesNotThrow(() -> CodeSystemUtility.sanitizeMissingVersion(codeList, dtoCodeSystem)); 
    	assertDoesNotThrow(() -> CodeSystemUtility.getBlockList(codeSystems));  
    	assertDoesNotThrow(() -> CodeSystemUtility.getCodeSystemMessage(new CodeSystemVersionDTO("code", "system"), codeList));  
    	assertDoesNotThrow(() -> CodeSystemUtility.getAnswerList(new CodeSystemVersionDTO("code", "system")));  

    	assertTrue(CodeSystemUtility.isBlocklisted("2.16.840.1.113883.6.999.AL")); 
    	assertTrue(CodeSystemUtility.isLoinc("2.16.840.1.113883.6.1")); 
    	assertFalse(CodeSystemUtility.isLoinc("2.16.840.1.113883.6.2")); 
    	assertTrue(CodeSystemUtility.requiresAnswerList("89261-2")); 
    	assertFalse(CodeSystemUtility.requiresAnswerList("89263-2")); 

	}
}
