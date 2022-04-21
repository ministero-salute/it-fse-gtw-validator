package it.finanze.sanita.fse2.ms.gtw.validator.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.AbstractDTO;
import lombok.Data;


/**
 * The Class ErrorResponseDTO.
 *
 * @author CPIERASC
 * 
 * 	Error response.
 */
@Data
public class ErrorResponseDTO extends AbstractDTO {

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
