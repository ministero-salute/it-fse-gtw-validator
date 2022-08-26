package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component 
@Slf4j
@ConditionalOnProperty("ms.validator.audit.enabled")
public class AuditSRV implements IAuditSRV {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4177751094060797214L;

	@Autowired
	private transient IAuditRepo auditServiceRepo;

	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest httpServletRequest,Object body) {
		try {
			String requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
			Map<String, Object> auditMap = new HashMap<>();
			auditMap.put("servizio", httpServletRequest.getRequestURI());
			auditMap.put("start_time", httpServletRequest.getAttribute("START_TIME"));
			auditMap.put("end_time", new Date());
			auditMap.put("request", StringUtility.fromJSON(requestBody, Object.class));
			auditMap.put("response", body);
			auditMap.put("jwt_issuer", httpServletRequest.getAttribute("JWT_ISSUER"));
			httpServletRequest.removeAttribute("JWT_ISSUER");

			auditServiceRepo.save(auditMap);
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}
}
