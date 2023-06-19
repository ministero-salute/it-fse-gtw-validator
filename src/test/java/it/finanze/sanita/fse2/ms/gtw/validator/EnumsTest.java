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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.TerminologyExtractionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.WarnLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.VocabularyException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;

class EnumsTest {
	
    @Test
    @DisplayName("ResultLogEnum test")
    void resultLogEnumTest() {
        String code = "OK";
        String description = "Operation completed successfully";
        assertEquals(code, ResultLogEnum.OK.getCode());
        assertEquals(description, ResultLogEnum.OK.getDescription());
    } 
    
    @Test
    @DisplayName("ErrorLogEnum test")
    void errorLogEnumTest() {
        String code = "ERROR";
        String description = "Generic error";

        assertEquals(code, ErrorLogEnum.GENERIC_WARNING.getCode());
        assertEquals(description, ErrorLogEnum.GENERIC_WARNING.getDescription());

    } 
    
    @Test
    @DisplayName("OperationLogEnum test")
    void operationLogEnumTest() {
        String code = "VAL-TERMINOLOGY-CDA2";
        String description = "Validazione terminology CDA2";

        assertEquals(code, OperationLogEnum.TERMINOLOGY_VALIDATION.getCode());
        assertEquals(description, OperationLogEnum.TERMINOLOGY_VALIDATION.getDescription());

    } 
    
    @Test
    @DisplayName("WarnLogEnum test")
    void warnLogEnumTest() {
        String code = "WARNING";
        String description = "Code Allowed";

        assertEquals(code, WarnLogEnum.ALLOWED.getCode());
        assertEquals(description, WarnLogEnum.ALLOWED.getDescription());

    } 

    @Test
    @DisplayName("Response DTO Test")
    void responseDtoTest() {
    	LogTraceInfoDTO logTraceInfo = new LogTraceInfoDTO("TEST_SPAN_ID", "TEST_TRACE_ID"); 
    	ResponseDTO responseDto = new ResponseDTO(logTraceInfo, 0, "testErrorMsg"); 
    	
    	assertEquals(responseDto.getClass(), ResponseDTO.class); 
    	assertEquals(responseDto.getTraceID().getClass(), String.class); 
    	assertEquals(responseDto.getSpanID().getClass(), String.class); 
    	
    	assertEquals("TEST_TRACE_ID", responseDto.getTraceID());
    	assertEquals("TEST_SPAN_ID", responseDto.getSpanID());

    } 
    
    @Test
    @DisplayName("Code System Snapshot DTO Test")
    void codeSystemSnapshotDtoTest() {
    	List<DictionaryETY> dictList = new ArrayList<DictionaryETY>(); 
    	
    	DictionaryETY ety = new DictionaryETY(); 
    	DictionaryETY etySecond = new DictionaryETY(); 
    	
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
    	
    	CodeSystemSnapshotDTO dto = new CodeSystemSnapshotDTO(dictList); 
    	
    	assertNotNull(dto); 
    	
    	assertNotNull(dto.getAllowList()); 
    	assertNotNull(dto.getCodeSystemMaxVersions()); 
    	assertNotNull(dto.getMaxForCreationDate(dictList)); 
    	assertNotNull(dto.getMaxForLast(dictList)); 

    } 
    
    @Test
    @DisplayName("Code System DTO Test")
    void codeSystemDtoTest() {
    	CodeSystemVersionDTO dto = new CodeSystemVersionDTO("codeSystem", "version"); 
    	CodeSystemVersionDTO dto2 = new CodeSystemVersionDTO("codeSystem2", "version2"); 
    	CodeSystemVersionDTO dto3 = new CodeSystemVersionDTO("codeSystem3", "version2"); 
    	CodeSystemVersionDTO dtoNullFirst = new CodeSystemVersionDTO(null, "version"); 
    	CodeSystemVersionDTO dtoNullSecond = new CodeSystemVersionDTO("codeSystem", null); 
    	CodeSystemVersionDTO dtoNullFirstEq = new CodeSystemVersionDTO(null, "version"); 
    	CodeSystemVersionDTO dtoNullAll = new CodeSystemVersionDTO(null, null); 
    	CodeSystemVersionDTO dtoNullAllSecond = new CodeSystemVersionDTO(null, null); 

    	assertFalse(dto.equals(dto2)); 
    	assertFalse(dto2.equals(dto3)); 
    	assertFalse(dto.equals(dto3)); 
    	assertFalse(dtoNullFirst.equals(dtoNullSecond)); 
    	assertFalse(dtoNullFirst.equals(dtoNullSecond)); 
    	assertFalse(dtoNullFirst.equals(dtoNullAll)); 
    	assertFalse(dtoNullFirst.equals(dtoNullFirstEq)); 
     	assertFalse(dtoNullFirst.equals(null)); 

    	assertDoesNotThrow(() -> dto.toString()); 

    	assertDoesNotThrow(() -> dto.hashCode()); 
    	assertDoesNotThrow(() -> dtoNullAll.hashCode()); 

    } 
    
    @Test
    @DisplayName("Code DTO Test")
    void codeDtoTest() {
    	CodeDTO dto = new CodeDTO("code", "codeSystem", "version"); 
    	CodeDTO dto2 = new CodeDTO("code2", "codeSystem2", "version2"); 
    	CodeDTO dto3 = new CodeDTO("code3", "codeSystem3", "version2"); 
    	CodeDTO dtoNullFirst = new CodeDTO(null, "codeSystem", "version"); 
    	CodeDTO dtoNullSecond = new CodeDTO("code", null, "version"); 
    	CodeDTO dtoNullFirstEq = new CodeDTO(null, "codeSystem", "version"); 
    	CodeDTO dtoNullAll = new CodeDTO(null, null, null); 
    	CodeDTO dtoNullAllSecond = new CodeDTO(null, null, null); 

    	assertFalse(dto.equals(dto2)); 
    	assertFalse(dto2.equals(dto3)); 
    	assertFalse(dto.equals(dto3)); 
    	assertFalse(dtoNullFirst.equals(dtoNullSecond)); 
    	assertFalse(dtoNullFirst.equals(dtoNullSecond)); 
    	assertFalse(dtoNullFirst.equals(dtoNullAll)); 
    	assertFalse(dtoNullFirst.equals(dtoNullFirstEq)); 
     	assertFalse(dtoNullFirst.equals(null)); 

    	assertDoesNotThrow(() -> dto.toString()); 

    	assertDoesNotThrow(() -> dto.getCodeSystemVersion()); 
    	assertDoesNotThrow(() -> dto.hashCode()); 
    	assertDoesNotThrow(() -> dtoNullAll.hashCode()); 

    } 
    
    @Test
    @DisplayName("Terminology Extraction DTO Test")
    void terminologyExtractionDtoTest() {
    	List<CodeDTO> dictList = new ArrayList<CodeDTO>(); 
    	List<String> dictListFilt = new ArrayList<String>(); 
    	dictList.add(new CodeDTO("code", "system", "version"));
    	dictList.add(new CodeDTO("code2", "system2", "version2")); 
    	dictList.add(new CodeDTO("code3", "system3", "version3")); 
    	dictListFilt.add("code"); 

    	TerminologyExtractionDTO dto = new TerminologyExtractionDTO(dictList); 
    	
    	
    	assertDoesNotThrow(() -> dto.getCodeSystemVersions()); 
    	assertDoesNotThrow(() -> dto.getCodeSystems()); 
    	assertDoesNotThrow(() -> dto.filterCodeSystems(dictListFilt)); 
    	assertDoesNotThrow(() -> dto.rejectCodeSystems(dictListFilt)); 
    	assertDoesNotThrow(() -> dto.filterCodeSystemVersions(new ArrayList<CodeSystemVersionDTO>())); 
    	assertDoesNotThrow(() -> dto.rejectCodeSystemVersions(new ArrayList<CodeSystemVersionDTO>())); 

    	assertDoesNotThrow(() -> dto.removeCodes(dictList)); 
    	assertDoesNotThrow(() -> dto.removeCodeSystems(dictListFilt)); 

    } 
    
    @Test
    @DisplayName("CDA Validation DTO Test")
    void cdaValidationDtoTest() {
    	ValidationResult result = new ValidationResult(); 
    	result.addError("test error"); 
    	
    	CDAValidationDTO dto = new CDAValidationDTO(result); 
    	
    	assertNotNull(dto); 

    } 
    
    @Test
    @DisplayName("Exception Tests")
    void exceptionTest() {
    	BusinessException exc = new BusinessException("Error msg"); 
    	BusinessException excWithoutMsg = new BusinessException(new RuntimeException()); 
    	VocabularyException vocExc = new VocabularyException(new RuntimeException()); 
    	VocabularyException vocExcWithMsg = new VocabularyException("Error msg", new RuntimeException()); 

    	assertNotNull(exc); 
    	assertNotNull(excWithoutMsg); 
    	assertNotNull(vocExc); 
    	assertNotNull(vocExcWithMsg); 
    	
    }
    
    
}
