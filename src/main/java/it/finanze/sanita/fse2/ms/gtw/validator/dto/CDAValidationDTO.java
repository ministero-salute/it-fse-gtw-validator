/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CDAValidationDTO {
	
	private String message;
	
	private CDAValidationStatusEnum status;
	
	private Map<CDASeverityViolationEnum, List<String>> violations;
	
	public CDAValidationDTO(CDAValidationStatusEnum inStatus) {
		status = inStatus;
	}

	public CDAValidationDTO(ValidationResult result) {
		status = CDAValidationStatusEnum.NOT_VALID;
		violations = new HashMap<>();
		violations.put(CDASeverityViolationEnum.WARN, result.getWarnings());
		violations.put(CDASeverityViolationEnum.ERROR, result.getErrors());
		violations.put(CDASeverityViolationEnum.FATAL, result.getFatals());
	}
}