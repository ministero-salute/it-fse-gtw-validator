package it.finanze.sanita.fse2.ms.gtw.validator.repository.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractRedisRepo {

	@Autowired
	@Qualifier("stringRedisTemplate")
	private StringRedisTemplate redisTemplate;

	protected String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	protected abstract String checkAndChangeKey(String key);
}
