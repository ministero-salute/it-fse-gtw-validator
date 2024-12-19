
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
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.ValidationResult;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import lombok.Data;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
public class CDAValidationDTO {
	
	private String message;
	
	private CDAValidationStatusEnum status;
	
	private Map<CDASeverityViolationEnum, List<String>> violations;
	
	public CDAValidationDTO(CDAValidationStatusEnum inStatus) {
		status = inStatus;
	}

	public CDAValidationDTO(ValidationResult result) {
		status = CDAValidationStatusEnum.NOT_VALID;
		violations = new EnumMap<>(CDASeverityViolationEnum.class);
		violations.put(CDASeverityViolationEnum.WARN, result.getWarnings());
		violations.put(CDASeverityViolationEnum.ERROR, result.getErrors());
		violations.put(CDASeverityViolationEnum.FATAL, result.getFatals());
	}
}