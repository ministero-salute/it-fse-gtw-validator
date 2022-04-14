package it.sanita.fse.validator.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.sanita.fse.validator.controller.impl.AbstractMongoRepo;
import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.entity.SchematronETY;
import it.sanita.fse.validator.repository.mongo.ISchematronRepo;
import lombok.extern.slf4j.Slf4j;

/**
 *	@author vincenzoingenito
 *
 *	Schema repository.
 */
@Slf4j
@Repository
public class SchematronRepo extends AbstractMongoRepo<SchematronETY, String> implements ISchematronRepo {
	
	/**
	 * Serial version uid. 
	 */
	private static final long serialVersionUID = 8948529146857638945L;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public SchematronETY findByVersion(final String version) {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("version").is(version));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}

	@Override
	public SchematronETY findLastVersion() {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.with(Sort.by(Sort.Direction.DESC, "version"));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by last version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by last version on schematron ETY", ex);
		}
		return output;
	}

	
	
}
