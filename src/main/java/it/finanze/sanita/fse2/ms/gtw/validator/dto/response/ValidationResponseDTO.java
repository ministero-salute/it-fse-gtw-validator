package it.finanze.sanita.fse2.ms.gtw.validator.dto.response;

import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
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
