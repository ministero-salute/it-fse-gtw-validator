
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
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AuditRepo implements IAuditRepo {


	@Autowired
	private MongoTemplate mongo;


	/**
	 * Salvataggio audit request and response.
	 */
	@Override
	public void save(AuditETY audit) {
		try {
			mongo.insert(audit);
		} catch (final Exception ex) {
			log.error("Errore durante il salvataggio dell'audit", ex);
			throw new BusinessException("Errore durante il salvataggio dell'audit", ex);
		}
	}


}