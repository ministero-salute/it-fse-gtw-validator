/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import lombok.Getter;

public enum ResultLogEnum {

	OK("OK", "Operation completed successfully"),
	WARN("WARN", "Operation completed with warnings"),
	KO("KO", "Error encountered while processing operation"); 

	@Getter
	private String code;

	@Getter
	private String description;

	private ResultLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
