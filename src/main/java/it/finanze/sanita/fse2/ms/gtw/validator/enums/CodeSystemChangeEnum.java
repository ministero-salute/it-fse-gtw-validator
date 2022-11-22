/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
	USO_DI_DROGHE("observation > code[code='74204-9']", "1.3.6.1.4.1.12009.10.1.1694"); //AnswerListOID
	
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
