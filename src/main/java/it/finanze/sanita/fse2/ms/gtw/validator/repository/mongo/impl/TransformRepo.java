package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITransformRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TransformETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.TransformRepo;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class TransformRepo implements ITransformRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5816251263943368882L;
	
	@Autowired
	private MongoTemplate mongoTemplate;
 
	@Override
	public TransformETY findMapByTemplateIdRoot(String templateIdRoot) {
		TransformETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("template_id_root").is(templateIdRoot).and("deleted").is(false));
			query.fields().include("_id");
			out = mongoTemplate.findOne(query, TransformETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find structure map by name : " , ex);
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}
	
	@Override
	public TransformETY findMapByName(final String mapName) {
		TransformETY out = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("name_structure_map").regex(Pattern.compile("^"+mapName, Pattern.CASE_INSENSITIVE))
					.and("deleted").is(false));  
			out = mongoTemplate.findOne(query, TransformETY.class);
		} catch(Exception ex) {
			log.error("Error while perform find structure map by name : " , ex);
			throw new BusinessException("Error while perform find structure map by name : " , ex);
		}
		return out;
	}


}