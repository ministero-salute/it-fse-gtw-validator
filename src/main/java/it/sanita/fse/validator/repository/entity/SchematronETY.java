package it.sanita.fse.validator.repository.entity;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save schematron.
 */
@Document(collection = "schematron")
@Data
@NoArgsConstructor
public class SchematronETY {

	@Id
	private String id;
	
	@Field(name = "cda_type")
	private String cdaType;
	
	@Field(name = "name_schematron")
	private String nameSchematron;
	
	@Field(name = "content_schematron")
	private Binary contentSchematron;
	
	@Field(name = "version")
	private String version;
	 
}