package it.finanze.sanita.fse2.ms.gtw.validator.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.SingletonInspectorResponseDTO;

/**
 *
 *	Controller singleton inspector.
 */
@RequestMapping(path = "/v1")
@Tag(name = "Servizio ispezione singleton in memoria")
public interface ISingletonInspectorCTL {

    @GetMapping("/singletons")
	@Operation(summary = "Ritorna i singleton presenti in memoria", description = "Servizio che permette di ispezionare i singleton di Schema e Schematron attualmente caricati in memoria")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SingletonInspectorResponseDTO.class)))
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	SingletonInspectorResponseDTO getSingletons(HttpServletRequest request);
    
    
}
