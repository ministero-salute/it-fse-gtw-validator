package it.sanita.fse.validator.repository.mongo;

import java.io.Serializable;

import it.sanita.fse.validator.repository.entity.SchematronETY;

/**
 *	@author vincenzoingenito
 *
 *	Schemamatron interface repository.
 */
public interface ISchematronRepo extends Serializable {
	
	/**
	 * Returns a Schematron identified by its {@code version}.
	 * 
	 * @param Version of the Schematron to return.
	 * @return Schematron identified by its {@code version}.
	 */
	SchematronETY findByVersion(String version);
	 
	/**
	 * Returns last version schematron.
	 * 
	 * @return Last schematron.
	 */
	SchematronETY findLastVersion();

}
