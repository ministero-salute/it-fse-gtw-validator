package it.finanze.sanita.fse2.ms.gtw.validator.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

/**
 *  @author vincenzoingenito
 *  
 */
@Configuration
@Getter
public class ValidationCFG {

	@Value("${disable.validations}")
	private List<String> disableValidations; 
}
