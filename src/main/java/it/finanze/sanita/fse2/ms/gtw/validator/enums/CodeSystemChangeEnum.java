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
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum CodeSystemChangeEnum {
	CRONICITA_DEL_PROBLEMA("observation > code[code='89261-2']", "2.16.840.1.113883.2.9.10.1.4.3.4.5"), //ArtDecor
	STATO_CLINICO_PROBLEMA("observation > code[code='33999-4']", "2.16.840.1.113883.2.9.77.22.11.7"), //ArtDecor
	CAPACITA_MOTORIA("observation > code[code='75246-9']", "2.16.840.1.113883.2.9.77.22.11.15"), //ArtDecor
	FUMO("observation > code[code='72166-2']", "1.3.6.1.4.1.12009.10.1.1356"), //AnswerListOID
	USO_DI_DROGHE("observation > code[code='74204-9']", "1.3.6.1.4.1.12009.10.1.1694"), //AnswerListOID
	MARTIAL_STATUS("observation > code[code='45404-1']", "1.3.6.1.4.1.12009.10.1.3342"); //AnswerListOID
	
	private String selector;
	private String codeSystem;

	private CodeSystemChangeEnum(String selector, String codeSystem) {
		this.selector = selector;
		this.codeSystem = codeSystem;
	}
	
	public static List<CodeSystemChangeEnum> getAll() {
		return Arrays.asList(CodeSystemChangeEnum.values());
	}

}