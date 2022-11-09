/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TransformETY;



public interface ITransformRepo extends Serializable {

	TransformETY findMapByTemplateIdRoot(String templateIdRoot);
	
	TransformETY findMapByName(String mapName);
	
}
