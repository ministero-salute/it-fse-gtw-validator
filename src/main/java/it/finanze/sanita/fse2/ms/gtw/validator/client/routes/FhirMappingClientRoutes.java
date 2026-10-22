package it.finanze.sanita.fse2.ms.gtw.validator.client.routes;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.FhirMapping.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public final class FhirMappingClientRoutes {

    @Value("${ms.url.gtw-fhir-mapping-engine}")
    private String host;

    public UriComponentsBuilder base() {
        return UriComponentsBuilder.fromUriString(host);
    }

    public String identifier() {
        return IDENTIFIER;
    }

    public String microservice() {
        return IDENTIFIER_MS;
    }

    public String transform(List<String> validations, boolean includeRemovalReport) {
        UriComponentsBuilder builder = base().pathSegment(API_VERSION, API_DOCUMENTS, API_TRANSFORM);
        if (validations != null && !validations.isEmpty()) {
            builder.queryParam(QP_VALIDATIONS, validations);
        }
        if (includeRemovalReport) {
            builder.queryParam(QP_INCLUDE_REMOVAL_REPORT, true);
        }
        return builder.build().toUriString();
    }

}
