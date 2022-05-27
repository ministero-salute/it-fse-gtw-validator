package it.finanze.sanita.fse2.ms.gtw.validator.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.enums.CurrentApplicationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ILogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;
import net.logstash.logback.argument.StructuredArguments;

/** 
 * 
 * @author: Guido Rocco - IBM 
 */ 
@Service
public class ElasticLoggerHelper {
    
    
	Logger log = LoggerFactory.getLogger("elastic-logger"); 
	
	@Value("${spring.application.name}")
	private String loggingApplication; 
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
	
	
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String message, ILogEnum operation, 
			   ResultLogEnum result, Date startDateOperation, Date endDateOperation) {
		
		log.trace(message, StructuredArguments.kv("application", loggingApplication), 
				 StructuredArguments.kv("operation", operation.getCode()), 
				 StructuredArguments.kv("op-log-timestamp", dateFormat.format(new Date())),
				 StructuredArguments.kv("op-result", result.getCode()),
				 StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
				 StructuredArguments.kv("op-timestamp-end", dateFormat.format(endDateOperation))); 
	} 
	
	public void debug(String message,  ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation, Date endDateOperation) {
		
		log.debug(message, StructuredArguments.kv("application", loggingApplication), 
				 StructuredArguments.kv("operation", operation.getCode()), 
				 StructuredArguments.kv("op-log-timestamp", dateFormat.format(new Date())),
				 StructuredArguments.kv("op-result", result.getCode()),
				 StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
				 StructuredArguments.kv("op-timestamp-end", dateFormat.format(endDateOperation))); 
	} 
	 
	public void info(String message, ILogEnum operation,  
			ResultLogEnum result, Date startDateOperation, Date endDateOperation) {
		
		log.info(message, StructuredArguments.kv("application", loggingApplication), 
				 StructuredArguments.kv("operation", operation.getCode()), 
				 StructuredArguments.kv("op-log-timestamp", dateFormat.format(new Date())),
				 StructuredArguments.kv("op-result", result.getCode()),
				 StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
				 StructuredArguments.kv("op-timestamp-end", dateFormat.format(endDateOperation))); 
	} 
	
	public void warn(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation, Date endDateOperation) {
		
		log.warn(message, StructuredArguments.kv("application", loggingApplication), 
				 StructuredArguments.kv("operation", operation.getCode()), 
				 StructuredArguments.kv("op-log-timestamp", dateFormat.format(new Date())),
				 StructuredArguments.kv("op-result", result.getCode()),
				 StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
				 StructuredArguments.kv("op-timestamp-end", dateFormat.format(endDateOperation))); 
	} 
	
	public void error(String message, ILogEnum operation,  
			   ResultLogEnum result, Date startDateOperation, Date endDateOperation,
			   ILogEnum error) {
		
		log.error(message, StructuredArguments.kv("application", loggingApplication), 
				 StructuredArguments.kv("operation", operation.getCode()), 
				 StructuredArguments.kv("op-log-timestamp", dateFormat.format(new Date())),
				 StructuredArguments.kv("op-result", result.getCode()),
				 StructuredArguments.kv("op-timestamp-start", dateFormat.format(startDateOperation)),
				 StructuredArguments.kv("op-timestamp-end", dateFormat.format(endDateOperation)),
				 StructuredArguments.kv("op-error", error.getCode()),
				 StructuredArguments.kv("op-error-description", error.getDescription())); 
	}
    	
    
}