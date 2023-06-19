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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class DictionaryRepo implements IDictionaryRepo {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<DictionaryETY> getCodeSystems() {
		try {
            Query query = new Query().addCriteria(Criteria.where("deleted").is(false)); 
            return mongoTemplate.find(query, DictionaryETY.class);
		} catch (Exception e) {
			log.error("Error while retrieving all codeSystemVersions from Mongo", e);
			throw new BusinessException("Error while retrieving all codeSystemVersions from Mongo", e);
		}
	}

}
