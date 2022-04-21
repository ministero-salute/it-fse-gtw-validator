package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
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
	public SchematronETY findByCodeAndSystemAndExtension(final String code, final String system, final String templateIdExtension) {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("cda_code").is(code).and("cda_code_system").is(system).
					and("template_id_extension").is(templateIdExtension));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}

	@Override
	public SchematronETY findByName(final String name) {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("name_schematron").is(name));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by name on schematron ETY", ex);
			throw new BusinessException("Error while executing find by name on schematron ETY", ex);
		}
		return output;
	}
 
}
