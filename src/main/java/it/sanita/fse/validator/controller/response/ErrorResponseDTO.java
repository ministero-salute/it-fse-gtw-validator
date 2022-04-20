package it.sanita.fse.validator.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.sanita.fse.validator.dto.AbstractDTO;
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
