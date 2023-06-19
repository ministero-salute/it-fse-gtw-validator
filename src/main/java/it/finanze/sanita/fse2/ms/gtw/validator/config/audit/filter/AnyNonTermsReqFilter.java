/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.config.audit.filter;

import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.AuditFilter;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class AnyNonTermsReqFilter implements AuditFilter {
    @Override
    public boolean match(HttpServletRequest req) {
        return !req.getRequestURI().contains("validate-terminology");
    }

    @Override
    public AuditETY apply(String uri, HttpServletRequest req, Object body) throws Exception {
        String content = "{ \"message\": \"Unable to deserialize request body\"}";
        if (req instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) req;
            content = new String(wrapper.getContentAsByteArray(), UTF_8);
        }
        AuditETY entity = new AuditETY();
        entity.setServizio(uri);
        entity.setStart_time((Date) req.getAttribute("START_TIME"));
        entity.setEnd_time(new Date());
        entity.setRequest(StringUtility.fromJSON(content, Object.class));
        entity.setResponse(body);
        return entity;
    }
}
