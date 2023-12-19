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
package it.finanze.sanita.fse2.ms.gtw.validator.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.LogDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.WarnLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
@Service
@Slf4j
public class LoggerHelper {
    
	Logger kafkaLog = LoggerFactory.getLogger("kafka-logger"); 
	
    @Autowired
	private IConfigClient configClient;
	
	private String gatewayName;
	
	@Value("${log.kafka-log.enable}")
	private boolean kafkaLogEnable;

	@Value("${spring.application.name}")
	private String msName;
	
	/* 
	 * Specify here the format for the dates 
	 */
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS"); 
	
	/* 
	 * Implements structured logs, at all logging levels
	 */
	public void trace(String workflowInstanceId, String message, OperationLogEnum operation, ResultLogEnum result, Date startDateOperation) {
		if(configSRV.isControlLogPersistenceEnable()) {
			LogDTO logDTO = LogDTO.builder().
					message(message).
					operation(operation.getCode()).
					op_result(result.getCode()).
					op_timestamp_start(dateFormat.format(startDateOperation)).
					op_timestamp_end(dateFormat.format(new Date())).
					gateway_name(getGatewayName()).
					microservice_name(msName).
					workflow_instance_id(workflowInstanceId).
					build();

		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				workflow_instance_id(workflowInstanceId).
				build();

			if (Boolean.TRUE.equals(kafkaLogEnable)) {
				kafkaLog.trace(logMessage);
			}			
		}
	}

	public void debug(String workflowInstanceId, String message, OperationLogEnum operation, ResultLogEnum result, Date startDateOperation) {
		if(configSRV.isControlLogPersistenceEnable()) {
			LogDTO logDTO = LogDTO.builder().
					message(message).
					operation(operation.getCode()).
					op_result(result.getCode()).
					op_timestamp_start(dateFormat.format(startDateOperation)).
					op_timestamp_end(dateFormat.format(new Date())).
					gateway_name(getGatewayName()).
					microservice_name(msName).
					workflow_instance_id(workflowInstanceId).
					build();

		final String logMessage = asJsonDTO(logDTO);
		log.debug(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.debug(logMessage);
		}
	} 
	 
	public void info(String workflowInstanceId, String message, OperationLogEnum operation, ResultLogEnum result, Date startDateOperation) {
		if(configSRV.isControlLogPersistenceEnable()) {
			LogDTO logDTO = LogDTO.builder().
					message(message).
					operation(operation.getCode()).
					op_result(result.getCode()).
					op_timestamp_start(dateFormat.format(startDateOperation)).
					op_timestamp_end(dateFormat.format(new Date())).
					gateway_name(getGatewayName()).
					microservice_name(msName).
					workflow_instance_id(workflowInstanceId).
					build();

		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				workflow_instance_id(workflowInstanceId).
				build();

		final String logMessage = asJsonDTO(logDTO);
		log.info(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.info(logMessage);
			log.info("After send kafka message");
		}


	} 
	
	public void warn(String workflowInstanceId, String message, OperationLogEnum operation, ResultLogEnum result, Date startDateOperation, WarnLogEnum warning) {
		if(configSRV.isControlLogPersistenceEnable()) {
			LogDTO logDTO = LogDTO.builder().
					message(message).
					operation(operation.getCode()).
					op_result(result.getCode()).
					op_timestamp_start(dateFormat.format(startDateOperation)).
					op_timestamp_end(dateFormat.format(new Date())).
					op_warning(warning.getCode()).
					op_warning_description(warning.getDescription()).
					gateway_name(getGatewayName()).
					microservice_name(msName).
					workflow_instance_id(workflowInstanceId).
					build();

			final String logMessage = asJsonDTO(logDTO);
			log.warn(logMessage);
			if (Boolean.TRUE.equals(kafkaLogEnable)) {
				kafkaLog.warn(logMessage);
			}
		}


	} 
	
	public void error(String workflowInstanceId, String message, OperationLogEnum operation, ResultLogEnum result, Date startDateOperation, ErrorLogEnum error) {
		if(configSRV.isControlLogPersistenceEnable()) {
			LogDTO logDTO = LogDTO.builder().
					message(message).
					operation(operation.getCode()).
					op_result(result.getCode()).
					op_timestamp_start(dateFormat.format(startDateOperation)).
					op_timestamp_end(dateFormat.format(new Date())).
					op_error(error.getCode()).
					op_error_description(error.getDescription()).
					gateway_name(getGatewayName()).
					microservice_name(msName).
					workflow_instance_id(workflowInstanceId).
					build();

		LogDTO logDTO = LogDTO.builder().
				message(message).
				operation(operation.getCode()).
				op_result(result.getCode()).
				op_timestamp_start(dateFormat.format(startDateOperation)).
				op_timestamp_end(dateFormat.format(new Date())).
				op_error(error.getCode()).
				op_error_description(error.getDescription()).
				gateway_name(getGatewayName()).
				microservice_name(msName).
				workflow_instance_id(workflowInstanceId).
				build();
		
		final String logMessage = asJsonDTO(logDTO);
		log.error(logMessage);
		if (Boolean.TRUE.equals(kafkaLogEnable)) {
			kafkaLog.error(logMessage);
		}
		
	}

	/**
	 * Returns the gateway name.
	 * 
	 * @return The GatewayName of the ecosystem.
	 */
	private String getGatewayName() {
		if (gatewayName == null) {
			gatewayName = configClient.getGatewayName();
		}
		return gatewayName;
	}

	private String asJsonDTO(LogDTO dto) {
		String msg;
		try {
			msg = StringUtility.toJSON(dto);
		} catch (JsonProcessingException e) {
			msg = String.format("{\"error\": \"unable to deserialize due to %s\"}", e.getMessage());
		}
		return msg;
	}
	
}
