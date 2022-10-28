/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;

public final class TerminologyValidatorSingleton {

	private TerminologyValidatorSingleton() {}

	private static TerminologyValidatorSingleton instance;

	private List<CodeSystemVersionDTO> codeSystemVersions;
	private Map<String, String> codeSystemMaxVersions;
	private List<String> codeSystems;
	private List<String> whiteList;

	
	public static TerminologyValidatorSingleton getInstance() {
		if (instance == null) instance = new TerminologyValidatorSingleton();
		return instance;
	}
	
	// terminologies is an array containing all and only codeSystems and versions
	public void save(List<TerminologyETY> terminologies) {
		synchronized(TerminologyValidatorSingleton.class) {
			this.codeSystemVersions = getCodeSystemVersions(terminologies);
			this.codeSystemMaxVersions = getCodeSystemMaxVersions(codeSystemVersions); 
			this.codeSystems = getCodeSystems(codeSystemVersions);
			this.whiteList = getWhiteList(terminologies);
		}
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
	
	private List<CodeSystemVersionDTO> getCodeSystemVersions(List<TerminologyETY> terminologies) {
		return terminologies
				.stream()
				.map(ety -> new CodeSystemVersionDTO(ety.getSystem(), null))//csv.getVersion()))
				.collect(Collectors.toList());
	}
	
	private List<String> getCodeSystems(List<CodeSystemVersionDTO> codeSystemVersions) {
		return getCodeSystemVersions()
				.stream()
				.map(csv -> csv.getCodeSystem())
				.distinct()
				.collect(Collectors.toList());
	}
	
	private List<String> getWhiteList(List<TerminologyETY> terminologies) {
		return terminologies
				.stream()
				.filter(ety -> ety.getCode() != null && ety.getCode().equals("FSE_WHITELIST"))
				.map(ety -> ety.getSystem())
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
				.collect(Collectors.toMap(CodeSystemVersionDTO::getCodeSystem, CodeSystemVersionDTO::getCodeSystemVersion));
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