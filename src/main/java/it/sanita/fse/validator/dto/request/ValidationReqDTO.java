package it.sanita.fse.validator.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import it.sanita.fse.validator.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author CPIERASC
 *
 *	Request body validazione.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReqDTO extends AbstractDTO {

    @Schema(description = "CDA")
    private String cda;

}
