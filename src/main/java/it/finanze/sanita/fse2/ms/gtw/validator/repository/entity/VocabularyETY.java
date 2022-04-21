package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model of vocabulary.
 */
@Data
@NoArgsConstructor
@Document(collection = "vocabulary")
public class VocabularyETY {

	@Id
	private String id;
	
	@Field(name = "system")
	private String system;
	
	@Field(name = "code")
	private String code;
	
	@Field(name = "description")
	private String description;
	 
}
