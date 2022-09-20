package it.finanze.sanita.fse2.ms.gtw.validator.controller.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 *	Exceptions Handler.
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

	
	/**
	 * Tracker log.
	 */
	@Autowired
	private Tracer tracer;
   

	/**
	 * Management validation exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {NoRecordFoundException.class})
    protected ResponseEntity<ResponseDTO> handleValidationException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.BAD_REQUEST);
    }

	/**
	 * Management generic exception.
	 * 
	 * @param ex		exception
	 * @param request	request
	 * @return			
	 */
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ResponseDTO> handleGenericException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
 

	private ResponseEntity<ResponseDTO> handleException(final Exception ex, final HttpStatus status) {
		final ResponseDTO out = new ResponseDTO(getLogTraceInfo(), status.value(), ex.getMessage());
        return new ResponseEntity<>(out, new HttpHeaders(), status);
	}
	
	private LogTraceInfoDTO getLogTraceInfo() {
		return new LogTraceInfoDTO(
				tracer.currentSpan().context().spanIdString(), 
				tracer.currentSpan().context().traceIdString());
	}
	
}