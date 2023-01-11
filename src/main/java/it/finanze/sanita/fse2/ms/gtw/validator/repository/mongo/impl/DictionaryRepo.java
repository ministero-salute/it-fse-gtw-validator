/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
