/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto.response;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.AbstractDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author CPIERASC
 *
 *	Base response.
 */
@Getter
@Setter
public class ResponseDTO extends AbstractDTO {

	/**
	 * Trace id log.
	 */
	private String traceID;
	
	/**
	 * Span id log.
	 */
	private String spanID;
	
	/**
	 * Error DTO that describes an error.
	 */
	private ErrorResponseDTO error;

	/**
	 * Instantiates a new response DTO.
	 *
	 * @param traceInfo the trace info
	 */
	public ResponseDTO(final LogTraceInfoDTO traceInfo) {
		traceID = traceInfo.getTraceID();
		spanID = traceInfo.getSpanID(); 
	}
	
	/**
	 * Instantiates a new response DTO.
	 *
	 * @param traceInfo the trace info
	 * @param errorCode the error code
	 * @param errorMsg the error msg
	 */
	public ResponseDTO(final LogTraceInfoDTO traceInfo, final Integer errorCode, final String errorMsg) {
		traceID = traceInfo.getTraceID();
		spanID = traceInfo.getSpanID();
		error = new ErrorResponseDTO(errorCode, errorMsg); 
	}
	
}
