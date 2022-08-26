package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.util.Map;

public interface IAuditRepo {
	
	void save(Map<String, Object> auditMap);

}
