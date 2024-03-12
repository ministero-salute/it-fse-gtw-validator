/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.controller.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.ServerResponseException;

/**
 *	Exceptions Handler.
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

	
//	/**
//	 * Tracker log.
//	 */
//	@Autowired
//	private Tracer tracer;
   

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
				"","");
//				tracer.currentSpan().context().spanIdString(), 
//				tracer.currentSpan().context().traceIdString());
	}
	
	@ExceptionHandler(value = {ServerResponseException.class})
	protected ResponseEntity<ResponseDTO> handleServerResponseException(ServerResponseException ex) {
		return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}