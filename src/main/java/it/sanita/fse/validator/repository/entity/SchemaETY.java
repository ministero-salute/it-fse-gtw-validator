package it.sanita.fse.validator.repository.entity;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Model to save schema.
 */
@Document(collection = "schema")
@Data
@NoArgsConstructor
public class SchemaETY {

	@Id
	private String id;
	
	@Field(name = "cda_type")
	private String cdaType;
	
	@Field(name = "name_schema")
	private String nameSchema;
	
	@Field(name = "content_schema")
	private Binary contentSchema;
	
	@Field(name = "version")
	private String version;
	 
}