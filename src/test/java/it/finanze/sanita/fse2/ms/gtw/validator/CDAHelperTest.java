package it.finanze.sanita.fse2.ms.gtw.validator;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;

class CDAHelperTest {

    private static final String LOINC = "2.16.840.1.113883.6.1";
    private static final List<String> EXPECTED_CODES = List.of(
            "11502-2", "18729-4", "14957-5", "14957-5", "48767-8", "14957-5", "30525-0");

    @Test
    void extractsXmlCodesWithoutCreatingExtraOccurrences() {
        assertCodes(cda());
    }

    @Test
    void sanitizingPreservesXmlCodeOccurrences() {
        assertCodes(CodeSystemUtility.sanitizeCda(cda()));
    }

    @Test
    void retainsHtmlNormalizationAndSelfClosingElements() {
        String sanitized = CodeSystemUtility.sanitizeCda(
                "<ClinicalDocument><code codeSystem=\"LOINC\"/><value code=\"next\"/></ClinicalDocument>");
        Document document = Jsoup.parse(sanitized);

        assertEquals(Document.OutputSettings.Syntax.html, document.outputSettings().syntax());
        assertEquals("clinicaldocument", document.body().child(0).tagName());
        assertEquals("codesystem", document.selectFirst("code").attributes().iterator().next().getKey());
        assertEquals(0, document.selectFirst("code").childrenSize());
        assertEquals("value", document.selectFirst("code").nextElementSibling().tagName());
    }

    private void assertCodes(String cda) {
        assertEquals(EXPECTED_CODES, CDAHelper.extractTerminology(cda).get(LOINC));
        assertEquals(EXPECTED_CODES, CDAHelper.extractAllCodeSystems(cda).getCodes().stream()
                .filter(code -> LOINC.equals(code.getCodeSystem()))
                .map(CodeDTO::getCode)
                .toList());
    }

    private String cda() {
        return new String(getFileFromInternalResources(
                "Files/inconsistency_log_test/lab_inconsistency_cs.xml"), UTF_8);
    }
}
