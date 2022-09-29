package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;

public class EnumsTest {
	
    @Test
    @DisplayName("ResultLogEnum test")
    void resultLogEnumTest() {
        String code = "OK";
        String description = "Operazione eseguita con successo";
        assertEquals(code, ResultLogEnum.OK.getCode());
        assertEquals(description, ResultLogEnum.OK.getDescription());
    } 
    

    @Test
    @DisplayName("Response DTO Test")
    void responseDtoTest() {
    	LogTraceInfoDTO logTraceInfo = new LogTraceInfoDTO("TEST_SPAN_ID", "TEST_TRACE_ID"); 
    	ResponseDTO responseDto = new ResponseDTO(logTraceInfo, 0, "testErrorMsg"); 
    	
    	assertEquals(responseDto.getClass(), ResponseDTO.class); 
    	assertEquals(responseDto.getTraceID().getClass(), String.class); 
    	assertEquals(responseDto.getSpanID().getClass(), String.class); 
    	
    	assertEquals(responseDto.getTraceID(), "TEST_TRACE_ID"); 
    	assertEquals(responseDto.getSpanID(), "TEST_SPAN_ID"); 

    } 
    
    
}
