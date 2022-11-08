/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
