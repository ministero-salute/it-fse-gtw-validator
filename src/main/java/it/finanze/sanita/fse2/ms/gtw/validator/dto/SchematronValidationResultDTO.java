/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SchematronValidationResultDTO {

	private Boolean validSchematron;
	private Boolean validXML;
	private List<SchematronFailedAssertionDTO> failedAssertions;
	
}