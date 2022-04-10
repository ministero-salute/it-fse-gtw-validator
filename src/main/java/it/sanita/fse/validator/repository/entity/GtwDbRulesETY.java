package it.sanita.fse.validator.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model to save gtw rules.
 */
@Getter
@Setter
@Document(collection = "gtw-db-rules")
@Data 
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class GtwDbRulesETY {
 
	/**
	 * Pk document.
	 */
	@Id
	private String id;
 
}
