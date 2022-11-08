package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;

public class CodeSystemSnapshotDTO {

	private List<CodeSystemVersionDTO> codeSystemVersions;
	private Map<String, String> codeSystemMaxVersions;
	private List<String> codeSystems;
	private List<String> whiteList;
	
	// terminologies is an array containing all and only codeSystems and versions
	public CodeSystemSnapshotDTO(List<DictionaryETY> dictionaries) {
		if (codeSystemVersions == null) codeSystemVersions = new ArrayList<>();
		this.whiteList = getWhiteList(dictionaries);
		this.codeSystemVersions = getCodeSystemVersions(dictionaries);
		this.codeSystemMaxVersions = getCodeSystemMaxVersions(dictionaries); 
		this.codeSystems = getCodeSystems(codeSystemVersions);
	}

	public List<CodeSystemVersionDTO> getCodeSystemVersions() {
		if (codeSystemVersions == null) codeSystemVersions = new ArrayList<>();
		return Collections.unmodifiableList(codeSystemVersions);
	}

	public List<String> getCodeSystems() {
		if (codeSystems == null) codeSystems = new ArrayList<>();
		return Collections.unmodifiableList(codeSystems);
	}

	public List<String> getWhiteList() {
		if (whiteList == null) whiteList = new ArrayList<>();
		return Collections.unmodifiableList(whiteList);
	}

	public Map<String, String> getCodeSystemMaxVersions() {
		if (codeSystemMaxVersions == null) codeSystemMaxVersions = new HashMap<>();
		return Collections.unmodifiableMap(codeSystemMaxVersions);
	}
	
	private List<CodeSystemVersionDTO> getCodeSystemVersions(List<DictionaryETY> dictionaries) {
		return dictionaries
				.stream()
				.map(ety -> new CodeSystemVersionDTO(ety.getSystem(), ety.getVersion()))
				.collect(Collectors.toList());
	}
	
	private List<String> getCodeSystems(List<CodeSystemVersionDTO> codeSystemVersions) {
		return getCodeSystemVersions()
				.stream()
				.map(csv -> csv.getCodeSystem())
				.distinct()
				.collect(Collectors.toList());
	}
	
	private List<String> getWhiteList(List<DictionaryETY> dictionaries) {
		return dictionaries
				.stream()
				.filter(ety -> ety.isWhiteList())
				.map(ety -> ety.getSystem())
				.distinct()
				.collect(Collectors.toList());
	}
	
	private Map<String, String> getCodeSystemMaxVersions(List<DictionaryETY> dictionaries) {
		return dictionaries
				.stream()
				.collect(Collectors.groupingBy(dto -> dto.getSystem()))
				.values()
				.stream()
				.map(this::getMaxCodeSystemVersion)
				.filter(Objects::nonNull)
		        .collect(HashMap::new, (m,v)->m.put(v.getCodeSystem(), v.getVersion()), HashMap::putAll);
	}

	private CodeSystemVersionDTO getMaxCodeSystemVersion(List<DictionaryETY> dictionaries) {
		if (dictionaries.isEmpty()) return null;
		String codeSystem = dictionaries.get(0).getSystem();
		return new CodeSystemVersionDTO(codeSystem, getMax(dictionaries));
	}

	private String getMax(List<DictionaryETY> dictionaries) {
		if (dictionaries.isEmpty()) return null;
		DictionaryETY firstDictionary = dictionaries.get(0);
		if (firstDictionary.getReleaseDate() != null) return getMaxForReleaseDate(dictionaries);
		if (firstDictionary.getCreationDate() != null) return getMaxForCreationDate(dictionaries);
		return getMaxForLast(dictionaries);
	}

	private String getMaxForReleaseDate(List<DictionaryETY> dictionaries) {
		return dictionaries
				.stream()
				.sorted(Comparator.comparing(DictionaryETY::getReleaseDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.findFirst()
				.map(DictionaryETY::getVersion)				
				.orElse(null);
	}

	private String getMaxForCreationDate(List<DictionaryETY> dictionaries) {
		return dictionaries
				.stream()
				.sorted(Comparator.comparing(DictionaryETY::getCreationDate, Comparator.nullsLast(Comparator.reverseOrder())))
				.findFirst()
				.map(DictionaryETY::getVersion)				
				.orElse(null);
	}

	private String getMaxForLast(List<DictionaryETY> dictionaries) {
		Collections.reverse(dictionaries);
		return dictionaries
				.stream()
				.findFirst()
				.map(DictionaryETY::getVersion)
				.orElse(null);
	}
}