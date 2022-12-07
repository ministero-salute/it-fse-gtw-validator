/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * The Class ErrorResponseDTO.
 *
 * 
 * 	Error response.
 */
@Data
public class ErrorResponseDTO {

	/**
	 * Codice.
	 */
	@Schema(description = "Codice di errore")
	private final Integer code;
	
	/**
	 * Messaggio.
	 */
	@Schema(description = "Messaggio di errore")
	private final String message;

}
