/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.AbstractRedisRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
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

    @Autowired
    private ProfileUtility profileUtility;

    @Override
    public String get(String key) {
        String redisKey = checkAndChangeKey(key);
        return super.get(redisKey);
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
            String redisKey = checkAndChangeKey(key);
            if (!redisTemplate.hasKey(redisKey)) {
                inserted = redisTemplate.opsForValue().setIfAbsent(redisKey, "");
                if (inserted && ttlSeconds != null) {
                    redisTemplate.expire(redisKey, ttlSeconds, TimeUnit.SECONDS);
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
                log.debug("Updating Redis TTL of {} keys", redisEntries.keySet().size());
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
        return checkAndChangeKey(system + "_" + code);
    }

    @Override
    protected String checkAndChangeKey(String key) {
        if (profileUtility.isTestProfile() && key != null && !key.isEmpty()) {
            key = Constants.Profile.TEST_PREFIX + key;
        }
        return key;
    }
}
