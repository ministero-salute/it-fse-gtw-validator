package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "#{@codeSystemBean}")
@Data
@NoArgsConstructor
public class CodeSystemVersionETY {

	@Id
	private String id;
	
	@Field(name = "code_system")
	private String codeSystem;
	
	@Field(name = "version")
	private String version; 
	
	@Field(name = "whitelisted")
	private boolean whiteListed; 
	 
}