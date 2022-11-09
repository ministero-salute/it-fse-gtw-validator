/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
public class VocabularyRedisRepoTest {

    @SpyBean
    private StringRedisTemplate redis;
    @Autowired
    private IVocabulariesRedisRepo repository;

    @Test
    void getTest() {
        assertNull(repository.get("test0"));
    }

    @Test
    void insertTest() {
        // Provide knowledge
        when(redis.hasKey(anyString())).thenThrow(new RuntimeException());
        // Execute
        assertThrows(BusinessException.class, () -> repository.insert("test", 1000L));
    }

}
