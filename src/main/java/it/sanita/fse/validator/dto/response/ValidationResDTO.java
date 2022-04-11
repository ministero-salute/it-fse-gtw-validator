package it.sanita.fse.validator.dto.response;

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
public class ValidationResDTO extends ResponseDTO {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144353497297675698L;
	
	private final RawValidationEnum result;
	
	public ValidationResDTO() {
		super();
		result = null;
	}

	public ValidationResDTO(final LogTraceInfoDTO traceInfo, final RawValidationEnum inResult) {
		super(traceInfo);
		result = inResult;
	}
	
}
