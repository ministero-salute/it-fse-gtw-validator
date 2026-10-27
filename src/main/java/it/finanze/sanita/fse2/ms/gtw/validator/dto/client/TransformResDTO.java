package it.finanze.sanita.fse2.ms.gtw.validator.dto.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirValidationDTO.ValidationIssueDTO;
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

	private List<ValidationIssueDTO> normalizationIssues;

	private FhirValidationDTO validationResponse;

}
