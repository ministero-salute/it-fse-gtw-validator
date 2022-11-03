package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author vincenzoingenito
 * Model to save transform.
 */
@Document(collection = "#{@transformBean}")
@Data
@NoArgsConstructor
public class TransformETY {
 
	@Id
	private String id; 
	 
}