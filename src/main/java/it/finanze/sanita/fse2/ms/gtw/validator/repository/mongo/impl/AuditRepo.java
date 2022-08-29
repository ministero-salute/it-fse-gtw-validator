package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;

@Slf4j
@Repository
@ConditionalOnProperty("ms.validator.audit.enabled")
public class AuditRepo implements IAuditRepo {
	
	private static final String COLLECTION_NAME = "audit";
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ProfileUtility profileUtility;

	/**
	 * Salvataggio audit request and response.
	 */
	@Override 
	public void save(final Map<String, Object> auditMap) {
		try { 
			Document doc = new Document(auditMap);
			String collection = COLLECTION_NAME;
			if (profileUtility.isTestProfile()) {
				collection = Constants.Profile.TEST_PREFIX + COLLECTION_NAME;
			}
			mongoTemplate.insert(doc, collection);
		} catch (final Exception ex) {
			log.error("Errore durante il salvataggio dell'audit", ex);
			throw new BusinessException("Errore durante il salvataggio dell'audit", ex);
		}
	}
}
