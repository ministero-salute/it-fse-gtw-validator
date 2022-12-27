/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *	Request body validazione.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequestDTO {

	@Schema(description = "cda")
    private String cda;
	
	@Schema(description = "workflow_instance_id")
    private String workflowInstanceId;

}
