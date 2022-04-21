package it.finanze.sanita.fse2.ms.gtw.validator.controller.response;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.AbstractDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LogTraceInfoDTO extends AbstractDTO {

	/**
	 * Span.
	 */
	private final String spanID;
	
	/**
	 * Trace.
	 */
	private final String traceID;

}
