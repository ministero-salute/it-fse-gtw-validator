package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.SystemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.*;
import jakarta.validation.ValidationException;

@Tag("ISOLATION")
class TerminologyUtilitiesIsolationTest {
    @Test void codeIdentitySupportsDeduplicationIndependentOfDisplayMetadata() {
        // These are valid partially populated code identities produced by CDA extraction.
        List<CodeDTO> identities = List.of(new CodeDTO(), new CodeDTO("A", null, null, null, null),
                new CodeDTO("B", null, null, null, null), new CodeDTO("A", "system", null, null, null),
                new CodeDTO("A", "other", null, null, null), new CodeDTO("A", "system", null, "1", null),
                new CodeDTO("A", "system", null, "2", null));
        for (int i = 0; i < identities.size(); i++) {
            CodeDTO first = identities.get(i);
            CodeDTO copy = new CodeDTO(first.getCode(), first.getCodeSystem(), "different system label", first.getVersion(), "different display");
            assertEquals(first, copy); assertEquals(first.hashCode(), copy.hashCode());
            assertFalse(first.equals(null)); assertFalse(first.equals("A"));
            for (int j = 0; j < identities.size(); j++) assertEquals(i == j, first.equals(identities.get(j)));
            assertEquals(1, new HashSet<>(List.of(first, copy)).size());
        }
        assertEquals("{\"code\":\"?\"}", new CodeDTO().toString());
        assertEquals("{\"code\":\"A\",\"display-name\":\"Alpha\"}", new CodeDTO("A", "system", null, null, "Alpha").toString());
    }

    @Test void versionIdentitySupportsGroupingWithAndWithoutVersion() {
        List<CodeSystemVersionDTO> identities = List.of(new CodeSystemVersionDTO(null, null),
                new CodeSystemVersionDTO("system", null), new CodeSystemVersionDTO("other", null),
                new CodeSystemVersionDTO("system", "1"), new CodeSystemVersionDTO("system", "2"));
        for (int i = 0; i < identities.size(); i++) {
            var first = identities.get(i);
            var copy = new CodeSystemVersionDTO(first.getCodeSystem(), first.getVersion());
            assertEquals(first, copy); assertEquals(first.hashCode(), copy.hashCode());
            assertFalse(first.equals(null)); assertFalse(first.equals("system"));
            for (int j = 0; j < identities.size(); j++) assertEquals(i == j, first.equals(identities.get(j)));
        }
        assertEquals("system", identities.get(1).toString());
        assertEquals("system v1", identities.get(3).toString());
    }

    private DictionaryETY dictionary(String version, Date created, Date released, boolean allowed) {
        return new DictionaryETY(null, "system", version, created, released, allowed);
    }

    @Test void snapshotSelectsNewestReleaseThenCreationThenLastInsertionAndIsReadOnly() {
        var released = new CodeSystemSnapshotDTO(List.of(dictionary("old", new Date(5), new Date(1), true),
                dictionary("new", new Date(1), new Date(2), false)));
        assertEquals("new", released.getCodeSystemMaxVersions().get("system"));
        assertEquals(List.of("system"), released.getAllowList());
        var created = new CodeSystemSnapshotDTO(List.of(dictionary("old", new Date(1), null, false), dictionary("new", new Date(2), null, false)));
        assertEquals("new", created.getCodeSystemMaxVersions().get("system"));
        var undated = new CodeSystemSnapshotDTO(List.of(dictionary("first", null, null, false), dictionary("last", null, null, false)));
        assertEquals("last", undated.getCodeSystemMaxVersions().get("system"));
        assertThrows(UnsupportedOperationException.class, () -> released.getAllowList().clear());
        assertThrows(UnsupportedOperationException.class, () -> released.getCodeSystems().clear());
        assertThrows(UnsupportedOperationException.class, () -> released.getCodeSystemVersions().clear());
        assertThrows(UnsupportedOperationException.class, () -> released.getCodeSystemMaxVersions().clear());
        assertNull(released.getMaxForCreationDate(List.of()));
        assertNull(released.getMaxForLast(new ArrayList<>()));
    }

    @Test void extractionRetainsDisplayNamesAndGroupsRepeatedSystems() {
        String cda = """
            <ClinicalDocument><typeId extension="v1"/><templateId root="root"/><templateId/>
              <code code="A" codeSystem="system" codeSystemVersion="1" codeSystemName="System" displayName="Alpha"/>
              <code code="B" codeSystem="system"/><code codeSystem=""/>
            </ClinicalDocument>
            """;
        var info = CDAHelper.extractInfo(cda, "ts");
        assertEquals(List.of("root"), info.getTemplateIdSchematron());
        assertEquals(SystemTypeEnum.TS, info.getSystem());
        assertEquals(List.of("A", "B"), CDAHelper.extractTerminology(cda).get("system"));
        var extraction = CDAHelper.extractAllCodeSystems(cda);
        assertEquals("Alpha", extraction.getCodes().get(0).getDisplayName());
        assertEquals("System", extraction.getCodes().get(0).getCodeSystemName());
        assertEquals("1", extraction.getCodes().get(0).getVersion());
        assertEquals(List.of(new CodeSystemVersionDTO("system", "1")),
                extraction.filterCodeSystemVersions(List.of(new CodeSystemVersionDTO("system", "1"))));
        assertThrows(BusinessException.class, () -> CDAHelper.extractInfo("<ClinicalDocument/>", null));
        assertTrue(new TerminologyExtractionDTO(null).getCodes().isEmpty());
    }

    @Test void normalizesObservationAnswerCodeSystemsAndMapsLoincAnswerList() {
        String result = CodeSystemUtility.sanitizeCda("<observation><code code='89261-2'/><value code='A' codeSystem='old'/></observation>");
        assertEquals("2.16.840.1.113883.2.9.10.1.4.3.4.5", CDAHelper.extractAllCodeSystems(result).getCodes().get(0).getCodeSystem());
        var loinc = new CodeSystemVersionDTO("2.16.840.1.113883.6.1", "1");
        assertEquals(new CodeSystemVersionDTO("2.16.840.1.113883.6.1.AL", "1"), CodeSystemUtility.getAnswerList(loinc));
        var other = new CodeSystemVersionDTO("other", "1"); assertSame(other, CodeSystemUtility.getAnswerList(other));
        assertTrue(CodeSystemUtility.requiresAnswerList("89261-2"));
        assertFalse(CodeSystemUtility.requiresAnswerList("unmanaged"));
        assertFalse(CodeSystemUtility.isBlocklisted("1.9990.1"));
        assertTrue(CodeSystemUtility.isBlocklisted("1.9999.1"));
    }

    @ParameterizedTest @NullAndEmptySource
    void absentSystemsAndAnswersAreNotSpecial(String input) {
        assertFalse(CodeSystemUtility.isLoinc(input));
        assertFalse(CodeSystemUtility.requiresAnswerList(input));
        assertFalse(CodeSystemUtility.isBlocklisted(input));
        assertThrows(ValidationException.class, () -> Validation.notNull(input));
    }

    @Test void validationChecksNullBlankAndBooleanConstraints() {
        Validation.notNull("value", 42); Validation.mustBeTrue(true, "denied");
        assertThrows(ValidationException.class, () -> Validation.notNull(" "));
        assertThrows(ValidationException.class, () -> Validation.notNull((Object) null));
        assertEquals("denied", assertThrows(ValidationException.class, () -> Validation.mustBeTrue(false, "denied")).getMessage());
        assertThrows(ValidationException.class, () -> Validation.mustBeTrue(null, "denied"));
    }

    @Test void readsClasspathResourceAndReturnsNullForMissingResource() {
        byte[] content = FileUtility.getFileFromInternalResources("application.properties");
        assertNotNull(content); assertTrue(new String(content, StandardCharsets.UTF_8).contains("spring.application.name"));
        assertNull(FileUtility.getFileFromInternalResources("nonexistent-isolation-resource"));
    }
}
