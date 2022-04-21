package it.finanze.sanita.fse2.ms.gtw.validator.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class PropertiesCFG {
    
    /**
     * Vocabulary validation ttl.
     */
    @Value("${redis.vocabulary-ttl-seconds}")
    private Long validationTTL;

    /**
     * Vocabulary validation ttl.
     */
    @Value("${redis.vocabulary-enabled}")
    private boolean redisEnabled;

}
