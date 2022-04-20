package it.sanita.fse.validator.controller.response;

import it.sanita.fse.validator.enums.RawValidationEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author CPIERASC
 *
 *	DTO used to return validation result.
 */
@Getter
@Setter
public class ValidationResponseDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144353497297675698L;
	
	private final RawValidationEnum result;
	
	public ValidationResponseDTO() {
		super();
		result = null;
	}

	public ValidationResponseDTO(final LogTraceInfoDTO traceInfo, final RawValidationEnum inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
