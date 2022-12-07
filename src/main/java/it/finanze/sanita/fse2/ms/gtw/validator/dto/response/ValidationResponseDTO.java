/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto.response;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.ValidationInfoDTO;
import lombok.Getter;
import lombok.Setter;

/**
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class ValidationResponseDTO extends ResponseDTO {

	
	private final ValidationInfoDTO result;

	public ValidationResponseDTO(final LogTraceInfoDTO traceInfo, final ValidationInfoDTO inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
