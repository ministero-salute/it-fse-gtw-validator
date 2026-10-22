package it.finanze.sanita.fse2.ms.gtw.validator.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of the fhir-mapping-engine convertCDAToBundle API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirResourceDTO {

	private String cda;

	private String objectId;

	private String engineId;

}
