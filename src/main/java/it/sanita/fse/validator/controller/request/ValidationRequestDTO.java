package it.sanita.fse.validator.controller.request;

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
public class ValidationRequestDTO extends AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -3399427632028837223L;

	@Schema(description = "cda")
    private String cda;
     
     
}
