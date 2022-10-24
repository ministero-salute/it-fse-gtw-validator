/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import lombok.extern.slf4j.Slf4j;

/**
 *	@author vincenzoingenito
 *
 *	Schema repository.
 */
@Slf4j
@Repository
public class SchemaRepo implements ISchemaRepo {
	
	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;
	
	@Autowired
	private MongoTemplate mongoTemplate;
    
	
	@Override
	public List<SchemaETY> findByExtensionAndLastUpdateDate(final String typeIdExtension, final Date lastUpdateDate) {
		List<SchemaETY> output = new ArrayList<>();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("type_id_extension").is(typeIdExtension).and("last_update_date").gt(lastUpdateDate));
			query.with(Sort.by(Sort.Direction.DESC, "last_update_date"));
			output = mongoTemplate.find(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while perform findByExtensionAndLastUpdateDate" , ex);
			throw new BusinessException("Error while perform findByExtensionAndLastUpdateDate" , ex);
		}
		return output;
	}

	@Override
	public List<SchemaETY> findChildrenXsd(final String version) {
		List<SchemaETY> output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("root_schema").is(false).and("type_id_extension").is(version));
			output = mongoTemplate.find(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while searching for child schemes" , ex);
			throw new BusinessException("Error while searching for child schemes" , ex);
		}
		return output;
	}
	
	@Override
	public SchemaETY findFatherXsd(final String version) {
		SchemaETY output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("root_schema").is(true).and("type_id_extension").is(version));
			output = mongoTemplate.findOne(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while searching for father schema" , ex);
			throw new BusinessException("Error while searching for father schema" , ex);
		}
		return output;
	}
	
	@Override
	public SchemaETY findFatherLastVersionXsd() {
		SchemaETY output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("root_schema").is(true));
			output = mongoTemplate.findOne(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while searching for father schema" , ex);
			throw new BusinessException("Error while searching for father schema" , ex);
		}
		return output;
	}

	@Override
	public SchemaETY findByNameAndVersion(final String nameSchema, final String version) {
		SchemaETY output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("name_schema").is(nameSchema).and("type_id_extension").is(version));
			output = mongoTemplate.findOne(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while searching for find by name and version" , ex);
			throw new BusinessException("Error while searching for find by name and version" , ex);
		}
		return output;
	}

	@Override
	public List<SchemaETY> findByVersion(final String version) {
		List<SchemaETY> output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("type_id_extension").is(version));
			output = mongoTemplate.find(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error(String.format("Error while running find by version: %s", ex));
			throw new BusinessException(String.format("Error while running find by version: %s", ex));
		}
		return output;
	}

	@Override
	public SchemaETY findGtLastUpdate(final String typeIdExtension) {
		SchemaETY output;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("type_id_extension").is(typeIdExtension));
			query.with(Sort.by(Sort.Direction.DESC, "last_update_date"));
			output = mongoTemplate.findOne(query, SchemaETY.class);
		} catch(Exception ex) {
			log.error("Error while perform findGtLastUpdate" , ex);
			throw new BusinessException("Error while perform findGtLastUpdate" , ex);
		}
		return output;
	}
	
}
 		
