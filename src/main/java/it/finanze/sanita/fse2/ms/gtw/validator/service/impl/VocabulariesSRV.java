/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.config.properties.PropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * The Service to handle the Vocabularies on Redis.
 * 
 */
@Slf4j
@Service
public class VocabulariesSRV implements IVocabulariesSRV {

    @Autowired
    private transient IVocabulariesRedisRepo vocabulariesRedisRepo;

    @Autowired
    private transient ITerminologyRepo vocabulariesMongoRepo;

    @Autowired
    private transient PropertiesCFG propsCFG;

    @Override
    public VocabularyResultDTO vocabulariesExists(Map<String, List<String>> terminology) {
        boolean exists = false;
        String vocaboliInesistenti = "";
        try {
            if (propsCFG.isRedisEnabled()) {
                log.debug("Searching terminology on Redis...");
                exists = vocabulariesRedisRepo.allKeysExists(terminology);
            }

            if (!exists) {
                log.debug("Searching terminology on Mongo...");
                exists = true;
                log.debug("TERMINOLOGY KEYSET SIZE : " + terminology.keySet().size()); 
                int elementInQuery = 0;
                long startTime = new Date().getTime();
                Iterator<Map.Entry<String, List<String>>> iterator = terminology.entrySet().iterator();
                while (iterator.hasNext() && exists) {
                    Map.Entry<String, List<String>> entry = iterator.next();
                    String system = entry.getKey();
        			List<String> terminologyList = entry.getValue();
        			elementInQuery = elementInQuery + terminologyList.size();
        			if (!CollectionUtils.isEmpty(terminologyList)) {
                        log.debug("Checking existence of {} codes for system {}", terminologyList.size(), system);
                        if (propsCFG.isFindSpecificErrorVocabulary()) {
                            List<String> findedCodes = vocabulariesMongoRepo.findAllCodesExists(system, terminologyList);
                            List<String> differences = terminologyList.stream().filter(element -> !findedCodes.contains(element)).collect(Collectors.toList());
                            if (!differences.isEmpty()) {
                                log.debug(Constants.Logs.ERR_NOT_ALL_CODES_FOUND, system);
                                vocaboliInesistenti = "[Dizionario : " + system + " ,Vocaboli:" + String.join(",", differences) + "]";
                                exists = false;
                            }
                        } else if (propsCFG.isFindSystemAndCodesIndependence()) {
                            if (!isDefaultSuccessSystemPattern(system) && vocabulariesMongoRepo.existBySystemAndNotCodes(system, terminology.get(system))) {
                                log.debug(Constants.Logs.ERR_NOT_ALL_CODES_FOUND, system);
                                vocaboliInesistenti = String.join(",", terminology.get(system));
                                exists = false;
                            }
                        } else {
        					if (!vocabulariesMongoRepo.allCodesExists(system, terminology.get(system))) {
        						log.debug(Constants.Logs.ERR_NOT_ALL_CODES_FOUND, system);
        						vocaboliInesistenti = String.join(",", terminology.get(system));
        						exists = false;
        					}
        				}
        			}
                }
                long endDate = new Date().getTime() - startTime;
				log.debug("END DATE VOCABULARY QUERY OF TOTAL ELEMENT" + elementInQuery + " TIME : " + endDate + " ms");

                if (exists) {
                    if (propsCFG.isRedisEnabled()) {
                        log.debug("Updating terminology on Redis...");
                        vocabulariesRedisRepo.insertAll(terminology, propsCFG.getValidationTTL());
                    }
                } else {
                    log.warn("Terminology not present on Mongo, validation failed.");
                }
            } else {
                log.debug("Terminology validated with Redis");
            }
        } catch (Exception e) {
            log.error("Error while checking terminology existence on database", e);
            throw new BusinessException("Error while checking terminology existence on database", e);
        }

        return new VocabularyResultDTO(exists, vocaboliInesistenti);
    }

    /**
     * xxx.xxx.x.x.999 or x.999.x.xxx.xx (999 o 9999) returns true
     * @param system
     * @return
     */
    private boolean isDefaultSuccessSystemPattern(String system) {
        if (!StringUtility.isNullOrEmpty(system)) {
            return Arrays.stream(system.split("\\.")).anyMatch(group -> group.equals("999") || group.equals("9999"));
        }
        return false;
    }

    @Override
    public boolean existBySystemAndCode(final String system, final String code) {
    	boolean output = false;
    	try {
    		output = vocabulariesMongoRepo.existBySystemAndCode(system, code);
    	} catch(Exception ex) {
    		log.error("Error while execute find by system and code of vocabularies : " , ex);
    		throw new BusinessException("Error while execute find by system and code of vocabularies : " , ex);
    	} 
    	return output;
    }
}
