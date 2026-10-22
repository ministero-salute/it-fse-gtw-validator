package it.finanze.sanita.fse2.ms.gtw.validator.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response of the fhir-mapping-engine convertCDAToBundle API.
 * The generated bundle (json) is intentionally ignored: only the validation outcome is needed.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformResDTO {

	private String errorMessage;

	private FhirValidationResponseDTO validationResponse;

}
