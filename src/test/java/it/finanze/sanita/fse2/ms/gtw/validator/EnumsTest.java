package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;

public class EnumsTest {
	
    @Test
    @DisplayName("AssettoOrgEnum test")
    void resultLogEnumTest() {
        String code = "OK";
        String description = "Operazione eseguita con successo";
        assertEquals(code, ResultLogEnum.OK.getCode());
        assertEquals(description, ResultLogEnum.OK.getDescription());
    }
}
