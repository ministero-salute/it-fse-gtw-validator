/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;

/**
 * @author vincenzoingenito
 *
 * Schema interface repository.
 */
public interface ISchemaRepo extends Serializable {

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
