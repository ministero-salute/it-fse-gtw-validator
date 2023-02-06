/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;

public interface IEngineRepo {

	EngineETY getLatestEngine();
	
}
