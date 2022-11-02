package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.CodeSystemVersionETY;

public class CodeSystemSnapshotDTO {

	private List<CodeSystemVersionDTO> codeSystemVersions;
	private Map<String, String> codeSystemMaxVersions;
	private List<String> codeSystems;
	private List<String> whiteList;
	
	// terminologies is an array containing all and only codeSystems and versions
	public CodeSystemSnapshotDTO(List<CodeSystemVersionETY> codeSystemVersionsETY) {
		if (codeSystemVersions == null) codeSystemVersions = new ArrayList<>();
		this.whiteList = getWhiteList(codeSystemVersionsETY);
		this.codeSystemVersions = getCodeSystemVersions(codeSystemVersionsETY);
		this.codeSystemMaxVersions = getCodeSystemMaxVersions(codeSystemVersions); 
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
	
	private List<CodeSystemVersionDTO> getCodeSystemVersions(List<CodeSystemVersionETY> codeSystemVersionsETY) {
		return codeSystemVersionsETY
				.stream()
				.map(ety -> new CodeSystemVersionDTO(ety.getCodeSystem(), ety.getVersion()))
				.collect(Collectors.toList());
	}
	
	private List<String> getCodeSystems(List<CodeSystemVersionDTO> codeSystemVersions) {
		return getCodeSystemVersions()
				.stream()
				.map(csv -> csv.getCodeSystem())
				.distinct()
				.collect(Collectors.toList());
	}
	
	private List<String> getWhiteList(List<CodeSystemVersionETY> codeSystemVersions) {
		return codeSystemVersions
				.stream()
				.filter(ety -> ety.isWhiteListed())
				.map(ety -> ety.getCodeSystem())
				.distinct()
				.collect(Collectors.toList());
	}
	
	private Map<String, String> getCodeSystemMaxVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		return codeSystemVersions
				.stream()
				.collect(Collectors.groupingBy(dto -> dto.getCodeSystem()))
				.values()
				.stream()
				.map(this::getMaxCodeSystemVersion)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(CodeSystemVersionDTO::getCodeSystem, CodeSystemVersionDTO::getVersion));
	}

	private CodeSystemVersionDTO getMaxCodeSystemVersion(List<CodeSystemVersionDTO> codeSystemVersions) {
		if (codeSystemVersions.isEmpty()) return null;
		String codeSystem = codeSystemVersions.get(0).getCodeSystem();
		return new CodeSystemVersionDTO(codeSystem, getMax(codeSystemVersions));
	}

	private String getMax(List<CodeSystemVersionDTO> codeSystemVersions) {
		return null;
	}
}