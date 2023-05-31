package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UtilityTest {

	@Test
	@DisplayName("Code System Utility Test")
	void codeSystemUtilityTest() {
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
