/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility.isNullOrEmpty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants.App;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.TerminologyExtractionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CodeSystemChangeEnum;

public class CodeSystemUtility {
	
	private static final String LOINC_CODE_SYSTEM = "2.16.840.1.113883.6.1";
	private static final String ANSWER_LIST_CODE_SYSTEM = "2.16.840.1.113883.6.1.AL";
	
	private static final List<String> ANSWER_LIST_VALUES = Arrays.asList("89261-2", "33999-4", "75246-9");
	private static final List<String> INVALID_VALUES = Arrays.asList("999", "9999");
	
	public static List<CodeDTO> sanitizeMissingVersion(List<CodeDTO> codes, CodeSystemSnapshotDTO snapshot) {
		return codes
				.stream()
				.filter(code -> isNullOrEmpty(code.getVersion()))
				.peek(code -> sanitizeMissingVersion(code, snapshot))
				.collect(Collectors.toList());	
	}

	public static List<String> getBlockList(List<String> codeSystems) {
		return codeSystems
				.stream()
				.filter(CodeSystemUtility::isBlocklisted)
				.collect(Collectors.toList());
	}
	
	private static void sanitizeMissingVersion(CodeDTO code, CodeSystemSnapshotDTO snapshot) {
		if (!isNullOrEmpty(code.getVersion())) return;
		String codeSystem = code.getCodeSystem();
		String maxVersion = snapshot.getCodeSystemMaxVersions().get(codeSystem);	
		code.setVersion(maxVersion);
	}
	
    public static boolean isBlocklisted(String codeSystem) {
        if (isNullOrEmpty(codeSystem)) return false;
        return Arrays
            .stream(codeSystem.split("\\."))
            .anyMatch(INVALID_VALUES::contains);
    }

	public static boolean requiresAnswerList(String code) {
		if (isNullOrEmpty(code)) return false;
		return (ANSWER_LIST_VALUES.contains(code));
	}
	
    public static boolean isLoinc(String codeSystem) {
    	if (isNullOrEmpty(codeSystem)) return false;
    	return codeSystem.equals(LOINC_CODE_SYSTEM);
    }

	public static CodeSystemVersionDTO getAnswerList(CodeSystemVersionDTO codeSystemVersion) {
		if (!isLoinc(codeSystemVersion.getCodeSystem())) return codeSystemVersion;
		return new CodeSystemVersionDTO(ANSWER_LIST_CODE_SYSTEM, codeSystemVersion.getVersion());
	}

	public static String getGroupedMessage(TerminologyExtractionDTO terminologies) {
		String message = "Almeno uno dei seguenti vocaboli non è censito: ";
		Map<CodeSystemVersionDTO, List<CodeDTO>> groupedCodeSystems = getGroupedCodeSystems(terminologies);
		String groupedMessage = groupedCodeSystems
				.keySet()
				.stream()
				.map(key -> getCodeSystemMessage(key, groupedCodeSystems.get(key)))
				.collect(Collectors.joining(", "));
		return message + groupedMessage;
	}

	private static Map<CodeSystemVersionDTO, List<CodeDTO>> getGroupedCodeSystems(TerminologyExtractionDTO terminologies) {
		 return terminologies
				.getCodes()
				.stream()
				.collect(Collectors.groupingBy(CodeDTO::getCodeSystemVersion));
	}
	
	public static String getCodeSystemMessage(CodeSystemVersionDTO codeSystemVersion, List<CodeDTO> codes) {
		String codesString = codes
				.stream()
				.map(CodeDTO::getCode)
				.distinct()
				.collect(Collectors.joining(", ")); 
		return "[CodeSystem: " + codeSystemVersion + ", Codes: " + codesString + "]";
	}


    public static String sanitizeCda(String cda) {
    	Document document = Jsoup.parse(cda);
    	CodeSystemChangeEnum.getAll().forEach(change -> changeCodeSystem(document, change));		
    	return document.toString();
	}
	
    private static void changeCodeSystem(Document document, CodeSystemChangeEnum change) {
    	List<Elements> values = getValues(document, change.getSelector());
    	values.forEach(value -> value.attr(App.CODE_SYSTEM_KEY, change.getCodeSystem()));
    }

    private static List<Elements> getValues(Document document, String selector) {
    	return document
    			.select(selector)
    			.stream()
    			.map(Element::parent)
    			.filter(Objects::nonNull)
    			.map(elem -> elem.select(App.VALUE_KEY))
    			.collect(Collectors.toList());
    }
	
}
