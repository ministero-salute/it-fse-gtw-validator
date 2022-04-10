package it.sanita.fse.validator.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchematronFailedAssertionDTO {
	private String location;
	private String text;
	private String test;
}