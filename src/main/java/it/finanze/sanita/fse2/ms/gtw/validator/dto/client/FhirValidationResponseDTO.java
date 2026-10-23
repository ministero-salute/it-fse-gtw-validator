package it.finanze.sanita.fse2.ms.gtw.validator.dto.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ordered validation results returned by the fhir-mapping-engine.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FhirValidationResponseDTO {

	private List<ValidationResultDTO> results;

	/**
	 * Result of one requested validation.
	 */
	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ValidationResultDTO {

		private String type;

		private List<ValidationIssueDTO> issues;

		/**
		 * Matches the mapping engine: only ERROR or FATAL issues invalidate a result.
		 */
		public boolean isValid() {
			if (issues == null) {
				return true;
			}
			return issues.stream().noneMatch(i -> "ERROR".equalsIgnoreCase(i.getSeverity())
					|| "FATAL".equalsIgnoreCase(i.getSeverity()));
		}

	}

	/**
	 * A diagnostic produced by a validation.
	 */
	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ValidationIssueDTO {

		private String severity;

		private String code;

		private List<String> paths;

		private String message;

	}

}
