package it.finanze.sanita.fse2.ms.gtw.validator.controller;

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
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;

/**
 * 
 * @author CPIERASC
 *
 *	Controller validation.
 */
@RequestMapping(path = "/v1.0.0")
@Tag(name = "Servizio validazione documenti")
public interface IValidationCTL {

	@PostMapping("/validate")
	@Operation(summary = "Validazione documenti", description = "Valida il CDA fornito in input.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationResponseDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Validazione eseguita", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
							@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
							@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)) })
	ValidationResponseDTO validation(@RequestBody ValidationRequestDTO requestBody, HttpServletRequest request);

}
