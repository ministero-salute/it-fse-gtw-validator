/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import lombok.Getter;

public enum ErrorLogEnum {

	GENERIC_WARNING("ERROR", "Generic error"),
	BLOCKLIST_ERROR("ERROR", "Blocklist error"),
	INVALID_VERSION("ERROR", "Invalid version");

	@Getter
	private String code;

	@Getter
	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

}
