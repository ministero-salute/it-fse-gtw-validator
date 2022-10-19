/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;


public interface IDictionaryRepo extends Serializable {
 
	DictionaryETY findByFilename(String filename);
}
