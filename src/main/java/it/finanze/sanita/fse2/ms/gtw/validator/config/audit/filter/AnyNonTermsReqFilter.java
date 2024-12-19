
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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

        AuditETY entity = null;
        if (!content.contains("<!--CDA_BENCHMARK_TEST-->")) {
            entity = new AuditETY();
            entity.setServizio(uri);
            entity.setStart_time((Date) req.getAttribute("START_TIME"));
            entity.setEnd_time(new Date());
            entity.setRequest(StringUtility.fromJSON(content, Object.class));
            entity.setResponse(body);
        }

        return entity;
    }
}
