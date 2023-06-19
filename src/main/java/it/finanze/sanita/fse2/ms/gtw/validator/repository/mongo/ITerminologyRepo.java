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
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.util.List;

public interface ITerminologyRepo {

    /**
     * Returns {@code true} if all the codes are present in the database, {@code false} otherwise.
     * 
     * @param system The system of codes.
     * @param codes  The codes to check.
     * @return {@code true} if all the codes are present in the database, {@code false} otherwise.
     */
    boolean allCodesExists(String system, List<String> codes);
    
    List<String> findAllCodesExists(String system, List<String> codes);
    
    List<String> findAllCodesExistsForVersion(String system, String version, List<String> codes);
    
    boolean existBySystemAndCode(String system, String code);

    /**
     * Check whether code_system and codes exist on mongo using the following flow
     * query checks "where system and not in codes"
     * result = false -> system and codes exist || system not found -> validation succeeded
     * result = true -> system found, codes not found                -> validation failed
     * @param system
     * @param codes
     * @return
     */
    boolean existBySystemAndNotCodes(String system, List<String> codes);

}
