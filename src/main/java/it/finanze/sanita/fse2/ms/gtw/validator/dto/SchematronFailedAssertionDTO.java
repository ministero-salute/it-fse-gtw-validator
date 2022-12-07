/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchematronFailedAssertionDTO {
	
	private String location;
	
	private String text;
	
	private String test;
}