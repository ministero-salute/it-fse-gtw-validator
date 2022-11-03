/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;

@Configuration
public class CollectionNaming {

    @Autowired
    private ProfileUtility profileUtility;

    @Bean("dictionaryBean")
    public String getDictionaryCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.DICTIONARY;
        }
        return Constants.Collections.DICTIONARY;
    }

    @Bean("xslTransformBean")
    public String getXslTransformCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.XSL_TRANSFORM;
        }
        return Constants.Collections.XSL_TRANSFORM;
    }

    @Bean("schemaBean")
    public String getSchemaCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.SCHEMA;
        }
        return Constants.Collections.SCHEMA;
    }

    @Bean("schematronBean")
    public String getSchematronCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.SCHEMATRON;
        }
        return Constants.Collections.SCHEMATRON;
    }
    
    @Bean("transformBean")
    public String getStructureMapCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.TRANSFORM;
        }
        return Constants.Collections.TRANSFORM;
    }

    @Bean("terminologyBean")
    public String getTerminologyCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.TERMINOLOGY;
        }
        return Constants.Collections.TERMINOLOGY;
    }
}
