/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import lombok.Getter;

public enum OperationLogEnum {

	TERMINOLOGY_VALIDATION("VAL-TERMINOLOGY-CDA2", "Validazione terminology CDA2");

	@Getter
	private String code;

	@Getter
	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
