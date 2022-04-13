package it.sanita.fse.validator.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class PropertiesCFG {
    
    /**
     * Vocabulary validation ttl.
     */
    @Value("${redis.vocabulary-ttl}")
    private Long validationTTL;

    /**
     * Vocabulary validation ttl.
     */
    @Value("${redis.enabled}")
    private boolean redisEnabled;

}
