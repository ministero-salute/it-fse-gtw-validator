package it.finanze.sanita.fse2.ms.gtw.validator.utility;


import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StringUtilityTest {

    @Test
    void isNullOrEmpty() {
        assertTrue(StringUtility.isNullOrEmpty(null));
        assertTrue(StringUtility.isNullOrEmpty(""));
        assertFalse(StringUtility.isNullOrEmpty("123"));
    }

    @Test
    void getFilename() {
        assertEquals(StringUtility.getFilename(Paths.get("test/file.txt").toString()), "file.txt");
    }

}
