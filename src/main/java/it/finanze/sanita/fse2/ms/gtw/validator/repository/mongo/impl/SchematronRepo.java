/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 *	Schema repository.
 */
@Slf4j
@Repository
public class SchematronRepo implements ISchematronRepo {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public SchematronETY findByRootAndSystem(final String root, final String system) {
		SchematronETY output;
		try {
			Query query = new Query();
			query.addCriteria(where("template_id_root").is(root).and("system").is(system).and("deleted").is(false));
			query.with(Sort.by(Sort.Direction.DESC, "version"));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}

	@Override
	public SchematronETY findGreaterOne(final String root, final String system, final String version) {
		SchematronETY output;
		try {
			Query query = new Query();
			query.addCriteria(
				where("template_id_root").is(root).
				and("system").is(system).
				and("version").gt(version).
				and("deleted").is(false)
			);
			query.with(Sort.by(Sort.Direction.DESC, "version"));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}
 
	
}
