package it.sanita.fse.validator.repository.mongo;

import java.io.Serializable;
import java.util.List;

import it.sanita.fse.validator.repository.entity.SchemaETY;

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
}
