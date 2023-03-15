/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import it.finanze.sanita.fse2.ms.gtw.validator.enums.SystemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtractedInfoDTO {
	
	private String templateIdSchematron; //Schematron
	
	private String typeIdExtension; //Schema

	private SystemTypeEnum system;
}
