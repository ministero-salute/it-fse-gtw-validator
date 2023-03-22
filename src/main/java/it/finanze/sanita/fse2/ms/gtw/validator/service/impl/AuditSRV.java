/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IAuditSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component 
@Slf4j
@ConditionalOnProperty("ms.validator.audit.enabled")
public class AuditSRV implements IAuditSRV {


	@Autowired
	private IAuditRepo auditServiceRepo;

	@Autowired
	private WebEndpointProperties endpoints;

	@Autowired
	private SwaggerUiConfigProperties swagger;

	@Autowired
	private SpringDocConfigProperties api;


	/*
	 * Metodo di utility per l'audit dei servizi. 
	 */
	@Override
	public void saveAuditReqRes(HttpServletRequest request, Object body) {
		try {
			String requestBody = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray(), UTF_8);
			String service = request.getRequestURI();
			if(service != null) service = URLDecoder.decode(service, UTF_8.name());

			if(!skip(service)) {
				if (!request.getRequestURI().contains("validate-terminology")) {
					Map<String, Object> auditMap = new HashMap<>();
					auditMap.put("servizio", service);
					auditMap.put("start_time", request.getAttribute("START_TIME"));
					auditMap.put("end_time", new Date());
					auditMap.put("request", StringUtility.fromJSON(requestBody, Object.class));
					auditMap.put("response", body);
					auditServiceRepo.save(auditMap);
				}
			}else {
				log.debug("Skipping audit on path: {}", service);
			}
		} catch(Exception ex) {
			log.error("Errore nel salvataggio dell'audit : ", ex);
			throw new BusinessException("Errore nel salvataggio dell'audit : ", ex);
		}
	}

	private boolean skip(String uri) {
		return skipActuator(uri) || skipSwagger(uri);
	}

	private boolean skipActuator(String uri) {
		boolean skip = false;
		// Skip check if uri is null
		if(uri != null) {
			// Retrieve actuator exposed endpoints
			Set<String> ep = endpoints.getExposure().getInclude();
			// Retrieve mapping
			Map<String, String> mapping = endpoints.getPathMapping();
			// Iterate
			Iterator<String> iterator = ep.iterator();
			// Until we find match
			while (iterator.hasNext() && !skip) {
				// Get value
				String endpoint = iterator.next();
				// Retrieve associated mapping
				// because it may have been re-defined (e.g live -> status ...)
				// If it wasn't overwritten, it will return null therefore we are using the default mapping value
				String path = mapping.getOrDefault(endpoint, endpoint);
				// If path match, exit loop
				if (uri.contains(path)) skip = true;
			}
		}
		return skip;
	}

	private boolean skipSwagger(String uri) {
		boolean skip = false;
		// Skip check if uri is null or swagger not enabled
		if(uri != null && swagger.isEnabled()) {
			// Swagger page
			String ui = swagger.getPath();
			// Generative API
			String docs = api.getApiDocs().getPath();
			// Retrieve swagger-ui exposed endpoints
			skip = uri.contains(ui) || uri.contains(docs);
		}
		return skip;
	}
}
