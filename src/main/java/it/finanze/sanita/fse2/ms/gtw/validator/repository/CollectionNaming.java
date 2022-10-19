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
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.DICTIONARY;
        }
        return Constants.ComponentScan.Collections.DICTIONARY;
    }

    @Bean("gtwDbRulesBean")
    public String getGtwDbRulesCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.GTW_DB_RULES;
        }
        return Constants.ComponentScan.Collections.GTW_DB_RULES;
    }

    @Bean("schemaBean")
    public String getSchemaCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.SCHEMA;
        }
        return Constants.ComponentScan.Collections.SCHEMA;
    }

    @Bean("schematronBean")
    public String getSchematronCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.SCHEMATRON;
        }
        return Constants.ComponentScan.Collections.SCHEMATRON;
    }

    @Bean("terminologyBean")
    public String getTerminologyCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.TERMINOLOGY;
        }
        return Constants.ComponentScan.Collections.TERMINOLOGY;
    }
}
