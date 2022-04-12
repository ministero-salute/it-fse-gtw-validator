package it.sanita.fse.validator.repository.redis.impl;

import java.util.List;
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
    public boolean allKeysExists(final List<String> keys) {
        boolean keysExisting = false;
        try {
            keysExisting = keys.size() == redisTemplate.countExistingKeys(keys);
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
            throw new RuntimeException(String.format("Error while inserting used vocabulary with key {} on Redis", key), e);
        }

		return inserted;
	}
}
