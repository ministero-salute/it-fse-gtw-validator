package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.config.properties.PropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.redis.IVocabulariesRedisRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;

/**
 * The Service to handle the Vocabularies on Redis.
 * 
 * @author Simone Lungarella
 */
@Slf4j
@Service
public class VocabulariesSRV implements IVocabulariesSRV {

    @Autowired
    private IVocabulariesRedisRepo vocabulariesRedisRepo;

    @Autowired
    private ITerminologyRepo vocabulariesMongoRepo;

    @Autowired
    private PropertiesCFG propsCFG;

    @Override
    public VocabularyResultDTO vocabulariesExists(Map<String, List<String>> terminology) {

        boolean exists = false;

        String vocaboliInesistenti = "";
        try {

            if (propsCFG.isRedisEnabled()) {
                log.info("Searching terminology on Redis...");
                exists = vocabulariesRedisRepo.allKeysExists(terminology);
            }

            if (!exists) {
                log.info("Searching terminology on Mongo...");

                exists = true;
                
                log.info("TERMINOLOGY KEYSET SIZE : " + terminology.keySet().size()); 
                Integer elementInQuery = 0;
                Long startTime = new Date().getTime();
                for (String system : terminology.keySet()) {
        			List<String> terminologyList = terminology.get(system);
        			elementInQuery = elementInQuery + terminologyList.size(); 
        			if(terminologyList!=null && !terminologyList.isEmpty()) {
        				log.info("Checking existence of {} codes for system {}", terminologyList.size(), system);
        				
        				if(propsCFG.isFindSpecificErrorVocabulary()) {
        					List<String> findedCodes = vocabulariesMongoRepo.findAllCodesExists(system, terminologyList);
            				List<String> differences = terminologyList.stream().filter(element -> !findedCodes.contains(element)).collect(Collectors.toList());
            				if(!differences.isEmpty()) {
            					log.info("Not all codes for system {} are present on Mongo", system);
            					vocaboliInesistenti = "[Dizionario : " + system + " ,Vocaboli:" + differences.stream().collect(Collectors.joining(",")) + "]";
            					exists = false;
            					break;
            				}
        				} else {
        					if (!vocabulariesMongoRepo.allCodesExists(system, terminology.get(system))) {
        						log.info("Not all codes for system {} are present on Mongo", system);
        						vocaboliInesistenti = terminology.get(system).stream().collect(Collectors.joining(","));
        						exists = false;
        						break;
        					}
        				 
        				}
        			}
                }
                Long endDate = new Date().getTime() - startTime;
				log.info("END DATE VOCABULARY QUERY OF TOTAL ELEMENT" + elementInQuery + " TIME : " + endDate + " ms");

                if (exists) {
                    if (propsCFG.isRedisEnabled()) {
                        log.info("Updating terminology on Redis...");
                        vocabulariesRedisRepo.insertAll(terminology, propsCFG.getValidationTTL());
                    }
                } else {
                    log.info("Terminology not present on Mongo, validation failed.");
                }
            } else {
                log.info("Terminology validated with Redis");
            }
        } catch (Exception e) {
            log.error("Error while checking terminology existence on database", e);
            throw new BusinessException("Error while checking terminology existence on database", e);
        }

        return new VocabularyResultDTO(exists, vocaboliInesistenti);
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
