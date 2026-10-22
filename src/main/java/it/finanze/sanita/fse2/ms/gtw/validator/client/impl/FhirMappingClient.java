package it.finanze.sanita.fse2.ms.gtw.validator.client.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IFhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.validator.client.routes.FhirMappingClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.ServerResponseException;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of fhir-mapping-engine Client.
 */
@Slf4j
@Component
public class FhirMappingClient implements IFhirMappingClient {

	@Autowired
	private FhirMappingClientRoutes routes;

	@Autowired
	private RestTemplate client;

	@Override
	public TransformResDTO callConvertCdaInBundle(final FhirResourceDTO resource, final List<String> validations) {
		log.debug("Fhir Mapping Client - Calling Fhir Mapping to execute validations {}", validations);
		final String endpoint = routes.transform(validations);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<FhirResourceDTO> entity = new HttpEntity<>(resource, headers);

		try {
			return client.postForObject(endpoint, entity, TransformResDTO.class);
		} catch (HttpStatusCodeException clientException) {
			String msg = "Errore durante l'invocazione dell' API /v1/documents/transform. Il sistema ha restituito un " + clientException.getStatusCode();
			throw new ServerResponseException(routes.microservice(), "/v1/documents/transform", msg, clientException.getStatusCode(), clientException.getStatusCode().value(), clientException.getLocalizedMessage());
		}
	}

}
