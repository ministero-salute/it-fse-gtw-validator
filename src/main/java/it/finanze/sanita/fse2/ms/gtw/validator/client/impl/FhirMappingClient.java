package it.finanze.sanita.fse2.ms.gtw.validator.client.impl;

import java.net.URI;
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
	public TransformResDTO callConvertCdaInBundle(final FhirResourceDTO resource, final List<String> validations, final boolean includeRemovalReport) {
		final URI uri = routes.transform(validations, includeRemovalReport);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<FhirResourceDTO> entity = new HttpEntity<>(resource, headers);

		try {
			return client.postForObject(uri, entity, TransformResDTO.class);
		} catch (HttpStatusCodeException clientException) {
			log.error("FHIR mapping engine request failed with HTTP status {}", clientException.getStatusCode());
			String msg = "FHIR mapping engine request to /v1/documents/transform failed with HTTP status " + clientException.getStatusCode();
			throw new ServerResponseException(routes.microservice(), "/v1/documents/transform", msg, clientException.getStatusCode(), clientException.getStatusCode().value(), clientException.getLocalizedMessage());
		}
	}

}
