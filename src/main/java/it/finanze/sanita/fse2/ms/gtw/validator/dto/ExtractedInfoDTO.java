package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtractedInfoDTO {
	
	private String templateIdSchematron;
	
	private String schemaVersion;
}