/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.config.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class PropertiesCFG {
    
    
    /**
     * Vocabulary find specific error.
     */
    @Value("${vocabulary.find-specific-error}")
    private boolean findSpecificErrorVocabulary;

    /**
     * Vocabulary specific config to check whether system exists indipendently from codes
     */
    @Value("${vocabulary.find-system-code-independence}")
    private boolean findSystemAndCodesIndependence;
}
