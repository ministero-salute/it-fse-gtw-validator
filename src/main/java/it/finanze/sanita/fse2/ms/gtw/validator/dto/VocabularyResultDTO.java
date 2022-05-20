package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.NoArgsConstructor;

import lombok.Data;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class VocabularyResultDTO {

	private Boolean valid;
	
	private String message;
	
}
