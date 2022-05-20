package it.finanze.sanita.fse2.ms.gtw.validator.dto;

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