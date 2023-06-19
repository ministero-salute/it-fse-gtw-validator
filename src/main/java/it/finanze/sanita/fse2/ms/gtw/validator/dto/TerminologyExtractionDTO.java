/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

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
