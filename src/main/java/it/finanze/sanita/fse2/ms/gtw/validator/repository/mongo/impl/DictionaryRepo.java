/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class DictionaryRepo implements IDictionaryRepo {

	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5236847076186265946L;

	@Autowired
	private transient MongoTemplate mongoTemplate;
	
	@Override
	public DictionaryETY findByFilename(String filename) {
		DictionaryETY out;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("filename").is(filename).and("deleted").is(false));
			out = mongoTemplate.findOne(query, DictionaryETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find by filename : " , ex);
			throw new BusinessException("Error while perform find by filename : " , ex);
		}
		return out;
	}
 
}
