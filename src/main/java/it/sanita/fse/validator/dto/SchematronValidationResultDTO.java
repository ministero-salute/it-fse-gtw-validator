package it.sanita.fse.validator.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchematronValidationResultDTO {

	private Boolean validSchematron;
	private Boolean validXML;
	private List<SchematronFailedAssertionDTO> failedAssertions;
	
}