
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
package it.finanze.sanita.fse2.ms.gtw.validator.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.RouteUtility.API_VALIDATE_FULL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class MockRequests {

    /**
     * Private constructor to disallow to access from other classes
     */
    private MockRequests() {}

    public static MockHttpServletRequestBuilder validate(ValidationRequestDTO req) throws JsonProcessingException {
        return post(API_VALIDATE_FULL)
            .content(new ObjectMapper().writeValueAsString(req))
            .contentType(MediaType.APPLICATION_JSON);
    }

}
