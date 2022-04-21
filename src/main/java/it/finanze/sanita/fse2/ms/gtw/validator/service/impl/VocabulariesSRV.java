package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.config.properties.PropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IVocabulariesMongoRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;

/**
 * The Service to handle the Vocabularies on Redis.
 * 
 * @author Simone Lungarella
 */
@Slf4j
@Service
public class VocabulariesSRV implements IVocabulariesSRV {

    @Autowired
    private IVocabulariesRedisRepo vocabulariesRedisRepo;

    @Autowired
    private IVocabulariesMongoRepo vocabulariesMongoRepo;

    @Autowired
    private PropertiesCFG propsCFG;

    @Override
    public boolean vocabulariesExists(Map<String, List<String>> terminology) {

        boolean exists = false;

        try {

            if (propsCFG.isRedisEnabled()) {
                log.info("Searching terminology on Redis...");
                exists = vocabulariesRedisRepo.allKeysExists(terminology);
            }

            if (!exists) {
                log.info("Searching terminology on Mongo...");

                exists = true;
                
                for (String system : terminology.keySet()) {

                    log.info("Checking existence of {} codes for system {}", terminology.get(system).size(), system);
                    if (!vocabulariesMongoRepo.allCodesExists(system, terminology.get(system))) {
                        log.info("Not all codes for system {} are present on Mongo", system);
                        exists = false;
                        break;
                    }
                }

                if (exists) {
                    if (propsCFG.isRedisEnabled()) {
                        log.info("Updating terminology on Redis...");
                        vocabulariesRedisRepo.insertAll(terminology, propsCFG.getValidationTTL());
                    }
                } else {
                    log.info("Terminology not present on Mongo, validation failed.");
                }
            } else {
                log.info("Terminology validated with Redis");
            }
        } catch (Exception e) {
            log.error("Error while checking terminology existence on database", e);
            throw new BusinessException("Error while checking terminology existence on database", e);
        }

        return exists;
    }

}
