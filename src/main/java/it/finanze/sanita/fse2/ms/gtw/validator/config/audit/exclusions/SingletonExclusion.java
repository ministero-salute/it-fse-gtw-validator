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

import javax.servlet.http.HttpServletRequest;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.AuditExclusion;

@Component
public class SingletonExclusion implements AuditExclusion {
 
    @Override
    public boolean verify(String uri, HttpServletRequest req) {
        boolean skip = false;
        // Skip check if uri is null or swagger not enabled
        if(uri != null) {
            skip = uri.startsWith("/v1/singletons");
        }
        return skip;
    }
}
