/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j
@ConditionalOnProperty("ms.validator.audit.enabled")
public class AuditSRV implements IAuditSRV {


	@Autowired
	private IAuditRepo auditServiceRepo;

	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest httpServletRequest,Object body) {
		try {
			String requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray(), StandardCharsets.UTF_8);
			log.info("REQ_BODY IN AUDIT:" + requestBody);
			if (!httpServletRequest.getRequestURI().contains("validate-terminology")) {
				Map<String, Object> auditMap = new HashMap<>();
				auditMap.put("servizio", httpServletRequest.getRequestURI());
				auditMap.put("start_time", httpServletRequest.getAttribute("START_TIME"));
				auditMap.put("end_time", new Date());
				auditMap.put("request", StringUtility.fromJSON(requestBody, Object.class));
				auditMap.put("response", body);
				auditServiceRepo.save(auditMap);
			}
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}
}
