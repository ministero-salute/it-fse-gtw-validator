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
	
	@Field(name = "content_schematron")
	private Binary contentSchematron;

	@Field(name = "name_schematron")
	private String nameSchematron;

	@Field(name = "cda_code")
	private String cdaCode;

	@Field(name = "cda_code_system")
	private String cdaCodeSystem;

	@Field(name = "template_id_extension")
	private String templateIdExtension;
	
	@Field(name = "xsd_schema_version")
	private String xsdSchemaVersion;
	 
}