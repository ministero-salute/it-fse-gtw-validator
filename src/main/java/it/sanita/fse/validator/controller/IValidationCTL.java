package it.sanita.fse.validator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.sanita.fse.validator.controller.request.ValidationRequestDTO;
import it.sanita.fse.validator.controller.response.ValidationResponseDTO;

/**
 * 
 * @author CPIERASC
 *
 *	Controller validation.
 */
@RequestMapping(path = "/v1")
@Tag(name = "Servizio validazione documenti")
public interface IValidationCTL {

	@PostMapping("/validate")
	@Operation(summary = "Validazione documenti", description = "Valida il CDA fornito in input.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationResponseDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Validazione eseguita"),
							@ApiResponse(responseCode = "400", description = "Bad Request"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	ValidationResponseDTO validation(@RequestBody ValidationRequestDTO requestBody, HttpServletRequest request);

}
