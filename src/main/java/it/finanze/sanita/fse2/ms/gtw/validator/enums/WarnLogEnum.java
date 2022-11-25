/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import lombok.Getter;

public enum WarnLogEnum {

	GENERIC_WARN("WARNING", "Generic warning"),
	INVALID_CODE("WARNING", "Invalid codes"),
	UNKNOWN("WARNING", "Code Unknown"),
	ALLOWED("WARNING", "Code Allowed");

	@Getter
	private String code;

	@Getter
	private String description;

	private WarnLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
