package it.sanita.fse.validator.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sanita.fse.validator.cda.ValidationResult;
import it.sanita.fse.validator.enums.CDASeverityViolationEnum;
import it.sanita.fse.validator.enums.CDAValidationStatusEnum;
import lombok.Getter;

@Getter
public class CDAValidationDTO {
	
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