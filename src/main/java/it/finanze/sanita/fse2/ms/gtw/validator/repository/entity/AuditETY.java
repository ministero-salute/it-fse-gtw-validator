/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Model to save ini and eds invocation info.
 */
@Document(collection = "#{@auditBean}")
@Data
@NoArgsConstructor
public class AuditETY {

	private String servizio;
	
	private Date start_time;
	
	private Date end_time;
	
	private Object request;
	
	private Object response;
}
