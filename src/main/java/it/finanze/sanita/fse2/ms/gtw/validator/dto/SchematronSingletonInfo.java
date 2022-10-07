package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchematronSingletonInfo {

	private String templateIdRoot;
	
    private String version;

    private Date dataUltimoAggiornamento;
    
}
