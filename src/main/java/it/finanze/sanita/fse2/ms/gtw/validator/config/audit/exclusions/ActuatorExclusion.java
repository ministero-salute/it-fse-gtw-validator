
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
package it.finanze.sanita.fse2.ms.gtw.validator.config.audit.exclusions;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.AuditExclusion;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActuatorExclusion implements AuditExclusion {

    @Autowired
    private WebEndpointProperties endpoints;

    @Override
    public boolean verify(String uri, HttpServletRequest req) {
        boolean skip = false;
        // Skip check if uri is null
        if(uri != null) {
            // Retrieve actuator exposed endpoints
            Set<String> ep = endpoints.getExposure().getInclude();
            // Retrieve actuator base path
            String base = endpoints.getBasePath();
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
                String mapper = mapping.getOrDefault(endpoint, endpoint);
                // Get actuator path
                String path = "";
                try{
                    path = URLDecoder.decode(UriComponentsBuilder.newInstance().pathSegment(base, mapper).toUriString(),"UTF-8");
                    if (uri.startsWith(path)) skip = true;
                } catch(Exception ex){
                    log.error("Error while decode path segment", ex);
                }
                // String path = URLDecoder.decode(UriComponentsBuilder.newInstance().pathSegment(base, mapper).toUriString(),"UTF-8");
                // If path match, exit loop
                if (uri.startsWith(path)) skip = true;
            }
        }
        return skip;
    }
}
