/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto.response;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.SingletonInfoDTO;
import lombok.Getter;
import lombok.Setter;

/**
 *
 *	DTO used to return singleton inspection result.
 */
@Getter
@Setter
public class SingletonInspectorResponseDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144353495597675698L;
	
	private final SingletonInfoDTO result;

	public SingletonInspectorResponseDTO(final LogTraceInfoDTO traceInfo, final SingletonInfoDTO inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
