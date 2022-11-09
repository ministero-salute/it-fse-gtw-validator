/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.redis;

import java.util.List;
import java.util.Map;

/**
 * Interface of Vocabulary Repository.
 * 
 */
public interface IVocabulariesRedisRepo {

    /**
     * Returns the value of the key if exists or {@code null} if it does not exist.
     * 
     * @param key The key.
     * @return The value of the key if exists or {@code null} if it does not exist.
     */
    String get(String key);

    /**
     * Returns {@code true} of all keys are present.
     * 
     * @param terminology terminology to search on Redis.
     * @return {@code true} of all keys are present.
     */
    boolean allKeysExists(Map<String, List<String>> terminology);

    /**
     * Inserts the key into the repository if the key does not exist.
     * 
     * @param key         The key.
     * @param ttlSeconds The time to live in seconds.
     * @return {@code true} if the key-value pair was inserted, {@code false} otherwise.
     */
    boolean insert(String key, Long ttlSeconds);

    /**
     * Inserts the keys into the Redis repository if the key does not exist.
     * 
     * @param terminology The terminology to insert.
     * @param validationTTL The time to live in seconds, if {@code null} the entries will not have ttl.
     */
    void insertAll(Map<String, List<String>> terminology, Long validationTTL);

}
