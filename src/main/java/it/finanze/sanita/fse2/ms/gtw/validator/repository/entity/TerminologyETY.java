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
package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Model of terminology.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@Document(collection = "#{@terminologyBean}")
public class TerminologyETY {

	@Id
	private String id;
	
	@Field(name = Constants.App.SYSTEM_KEY)
	private String system;
	
	@Field(name = Constants.App.CODE_KEY)
	private String code;
	
	@Field(name = "description")
	private String description;
	
	@Field(name = "deleted")
	private Boolean deleted;
}
