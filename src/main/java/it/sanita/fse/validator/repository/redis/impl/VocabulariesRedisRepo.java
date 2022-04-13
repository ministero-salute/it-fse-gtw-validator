package it.sanita.fse.validator.repository.redis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.redis.AbstractRedisRepo;
import it.sanita.fse.validator.repository.redis.IVocabulariesRedisRepo;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of Redis repository for vocabulary.
 * 
 * @author Simone Lungarella
 */
@Slf4j
@Repository
public class VocabulariesRedisRepo extends AbstractRedisRepo implements IVocabulariesRedisRepo {

    @Autowired
	@Qualifier("stringRedisTemplate")
	private StringRedisTemplate redisTemplate;

    @Override
    public String get(String key) {
        return super.get(key);
    }

    @Override
    public boolean allKeysExists(final Map<String, List<String>> terminology) {
        boolean keysExisting = false;
        try {
            List<String> redisKeys = buildRedisKeys(terminology);

            long expectedKeys = 0;
            for (List<String> keys : terminology.values()) {
                expectedKeys += keys.size();
            }
            long existingKeys = redisTemplate.countExistingKeys(redisKeys);
            keysExisting = expectedKeys == existingKeys;
        } catch (Exception e) {
            log.error("Error while checking existence of keys on Redis", e);
            throw new BusinessException("Error while checking existence of keys on Redis", e);
        }

        return keysExisting;
    }

    @Override
    public boolean insert(final String key, final Long ttlSeconds) {

        boolean inserted = false;
        try {
            if (!redisTemplate.hasKey(key)) {
                inserted = redisTemplate.opsForValue().setIfAbsent(key, "");
                if (inserted && ttlSeconds != null) {
                    redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            log.error(String.format("Error while inserting used vocabulary with key {} on Redis", key), e);
            throw new BusinessException(String.format("Error while inserting used vocabulary with key {} on Redis", key), e);
        }

		return inserted;
	}

    @Override
    public void insertAll(Map<String, List<String>> terminology, Long validationTTL) {

        try {

            Map<String, String> redisEntries = buildRedisMap(terminology);
            redisTemplate.opsForValue().multiSetIfAbsent(buildRedisMap(terminology));
            if (validationTTL != null) {
                log.info("Updating Redis TTL of {} keys", redisEntries.keySet().size());
                for (String key : redisEntries.keySet()) {
                    redisTemplate.expire(key, validationTTL, TimeUnit.SECONDS);
                }
            }
            
        } catch (Exception e) {
            log.error("Error while inserting missing vocabularies on Redis", e);
            throw new BusinessException("Error while inserting missing vocabularies on Redis", e);
        }

    }

    private List<String> buildRedisKeys(Map<String, List<String>> terminology) {
        List<String> redisKeys = new ArrayList<>();

        for (String key : terminology.keySet()) {
            for (String code : terminology.get(key)) {
                redisKeys.add(redisKey(key, code));
            }
        }
        return redisKeys;
    }

    private Map<String, String> buildRedisMap(Map<String, List<String>> terminology) {
        Map<String, String> redisMap = new HashMap<>();

        for (String key : terminology.keySet()) {
            for (String code : terminology.get(key)) {
                redisMap.put(redisKey(key, code), "");
            }
        }
        return redisMap;
    }

    private String redisKey(final String system, final String code) {
        return system + "_" + code;
    }
}
