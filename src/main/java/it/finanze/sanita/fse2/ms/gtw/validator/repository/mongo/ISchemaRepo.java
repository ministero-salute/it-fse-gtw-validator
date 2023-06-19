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
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;

/**
 * Schema interface repository.
 */
public interface ISchemaRepo {

	/**
	 * Return a Schema identified by last version.
	 * 
	 * @return Schema identified by last version.
	 */
	SchemaETY findFatherLastVersionXsd();
  
	/**
	 * Return a Schema identified by its {@code version}.
	 * 
	 * @return a Schema identified by its {@code version}.
	 */	
	SchemaETY findFatherXsd(String version);
	
	List<SchemaETY> findChildrenXsd(String version);
	
	SchemaETY findByNameAndVersion(String schemaName, String version);
	
	List<SchemaETY> findByVersion(String version);
	
	List<SchemaETY> findByExtensionAndLastUpdateDate(String typeIdExtension, Date lastUpdateDate);
	
	SchemaETY findGtLastUpdate(String typeIdExtension);
}
