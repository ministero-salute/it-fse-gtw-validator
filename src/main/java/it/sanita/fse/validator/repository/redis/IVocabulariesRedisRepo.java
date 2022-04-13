package it.sanita.fse.validator.repository.redis;

import java.util.List;

/**
 * Interface of Vocabulary Repository.
 * 
 * @author Simone Lungarella
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
     * @param keys Keys to search on Redis.
     * @return {@code true} of all keys are present.
     */
    boolean allKeysExists(List<String> keys);

    /**
     * Inserts the key into the repository if the key does not exist.
     * 
     * @param key         The key.
     * @param ttlSeconds The time to live in seconds.
     * @return {@code true} if the key-value pair was inserted, {@code false} otherwise.
     */
    boolean insert(String key, Long ttlSeconds);

}
