/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

//@Data
@AllArgsConstructor
public class TerminologyExtractionDTO {

	private List<CodeDTO> codes;
	
	public List<CodeDTO> getCodes() {
		if (codes == null) codes = new ArrayList<>();
		return codes;
	}
	
	public List<String> getCodeSystems() {
		return getCodes()
			.stream()
			.map(code -> code.getCodeSystem())
			.distinct()
			.collect(Collectors.toList());
	}
	
	public List<CodeSystemVersionDTO> getCodeSystemVersions() {
		return getCodes()
			.stream()
			.map(code -> new CodeSystemVersionDTO(code.getCodeSystem(), code.getVersion()))
			.distinct()
			.collect(Collectors.toList());
	}

	public List<String> filterCodeSystems(List<String> codeSystems) {
		return getCodeSystems()
			.stream()
			.filter(cs -> codeSystems.contains(cs))
			.collect(Collectors.toList());
	}

	public List<CodeSystemVersionDTO> filterCodeSystemVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		return getCodeSystemVersions()
			.stream()
			.filter(cs -> codeSystemVersions.contains(cs))
			.collect(Collectors.toList());
	}
	
	public List<String> rejectCodeSystems(List<String> codeSystems) {
		return getCodeSystems()
			.stream()
			.filter(cs -> !codeSystems.contains(cs))
			.collect(Collectors.toList());
	}
	
	public List<CodeSystemVersionDTO> rejectCodeSystemVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		return getCodeSystemVersions()
			.stream()
			.filter(cs -> !codeSystemVersions.contains(cs))
			.collect(Collectors.toList());
	}

	public void removeCodes(List<CodeDTO> codes) {
		getCodes().removeIf(code -> codes.contains(code));
	}
	
	public void removeCodeSystems(List<String> codeSystems) {
		getCodes().removeIf(code -> codeSystems.contains(code.getCodeSystem()));
	}

}
