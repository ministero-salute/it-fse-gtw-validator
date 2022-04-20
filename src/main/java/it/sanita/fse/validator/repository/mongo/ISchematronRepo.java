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
	 * @param code of the Schematron to return.
	 * @param system of the Schematron to return.
	 * @param templateIdExtension of the Schematron to return.
	 * @return Schematron identified by its {@code version}.
	 */
	SchematronETY findByCodeAndSystemAndExtension(String code, String system, String templateIdExtension);
	
	SchematronETY findByName(String name);
	  
}
