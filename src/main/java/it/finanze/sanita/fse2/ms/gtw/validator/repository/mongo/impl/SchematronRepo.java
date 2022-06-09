package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
	public SchematronETY findByTemplateIdRoot(final String templateIdRoot) {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("template_id_root").is(templateIdRoot));
			query.with(Sort.by(Sort.Direction.DESC, "template_id_extension"));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}
	
	@Override
	public List<SchematronETY> findChildrenBySystem(final String system) {
		List<SchematronETY> output = new ArrayList<>();
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("cda_code_system").is(system).
					and("root_schematron").is(false));
			query.with(Sort.by(Sort.Direction.DESC, "template_id_extension"));
			output = mongoTemplate.find(query, SchematronETY.class);
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
	
	
	@Override
	public SchematronETY findBySystemAndVersion(final String system, final String version) {
		SchematronETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("template_id_root").is(system).
					and("template_id_extension").gt(version));
			query.with(Sort.by(Sort.Direction.DESC, "template_id_extension"));
			output = mongoTemplate.findOne(query, SchematronETY.class);
		} catch(Exception ex) {
			log.error("Error while executing find by version on schematron ETY", ex);
			throw new BusinessException("Error while executing find by version on schematron ETY", ex);
		}
		return output;
	}
 
	
}
