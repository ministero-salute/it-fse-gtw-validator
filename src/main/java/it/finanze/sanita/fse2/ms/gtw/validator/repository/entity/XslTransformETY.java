package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import java.util.Date;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model to save xsl transform.
 */
@Document(collection = "#{@xslTransformBean}")
@Data
@NoArgsConstructor
public class XslTransformETY {

	@Id
	private String id;

	@Field(name = "template_id_root")
	private String templateIdRoot;
	
	@Field(name = "name_xsl_transform")
	private String nameXslTransform;
	
	@Field(name = "content_xsl_transform")
	private Binary contentXslTransform;
	
	@Field(name = "template_id_extension")
	private String templateIdExtension;
	
	@Field(name = "last_update_date")
    private Date lastUpdateDate;

}
