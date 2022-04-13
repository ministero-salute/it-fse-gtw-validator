package it.sanita.fse.validator.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.mongo.IVocabulariesMongoRepo;
import it.sanita.fse.validator.repository.redis.IVocabulariesRedisRepo;
import it.sanita.fse.validator.service.IVocabulariesSRV;
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

    /**
     * Vocabulary validation ttl.
     */
    @Value("${redis.vocabulary-ttl}")
    private Long validationTTL;

    @Override
    public boolean cacheVocabulary(final String system, final String code) {

        boolean inserted = false;
        try {
            inserted = vocabulariesRedisRepo.insert(redisKey(system, code), validationTTL);
        } catch (Exception e) {
            log.error(String.format("Error while inserting used vocabulary with code {} on Redis", code), e);
            throw new BusinessException(String.format("Error while inserting used vocabulary with code {} on Redis", code), e);
        }

        return inserted;
    }

    @Override
    public boolean vocabulariesExists(Map<String, List<String>> terminology) {

        boolean exists = false;

        try {

            log.info("Searching terminology on Redis...");
            List<String> redisKeys = buildRedisKeys(terminology);
            exists = vocabulariesRedisRepo.allKeysExists(redisKeys);

            if (!exists) {
                log.info("Searching terminology on Mongo...");

                exists = true;
                for (String system : terminology.keySet()) {

                    log.info("Checking existence of {} codes for system {}", terminology.size(), system);
                    if (!vocabulariesMongoRepo.allCodesExists(system, terminology.get(system))) {
                        exists = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while checking terminology existence on database", e);
            throw new BusinessException("Error while checking terminology existence on database", e);
        }

        return exists;
    }

    private List<String> buildRedisKeys(Map<String, List<String>> terminology) {
        List<String> redisKeys = null;

        for (String key : terminology.keySet()) {
            List<String> codes = terminology.get(key);
            for (String code : codes) {
                if (redisKeys == null) {
                    redisKeys = new java.util.ArrayList<>();
                }
                redisKeys.add(redisKey(key, code));
            }
        }
        return redisKeys;
    }

    private String redisKey(final String system, final String code) {
        return system + "_" + code;
    }

}
