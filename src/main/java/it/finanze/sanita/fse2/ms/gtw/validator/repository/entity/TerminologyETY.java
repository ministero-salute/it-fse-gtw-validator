/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Model of terminology.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Document(collection = "#{@terminologyBean}")
public class TerminologyETY {

	@Id
	private String id;
	
	@Field(name = "system")
	private String system;
	
	@Field(name = "code")
	private String code;
	
	@Field(name = "description")
	private String description;
	
	@Field(name = "deleted")
	private Boolean deleted;
}
