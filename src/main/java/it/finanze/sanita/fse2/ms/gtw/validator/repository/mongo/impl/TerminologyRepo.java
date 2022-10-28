/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.TerminologyETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TerminologyRepo implements ITerminologyRepo {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean allCodesExists(String system, List<String> codes) {
        
        boolean validationSuccess = true;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("system").is(system).and("code").in(codes)
            		.and("deleted").is(false));

            validationSuccess = mongoTemplate.exists(query, TerminologyETY.class);
         } catch (Exception e) {
            log.error(String.format(Constants.Logs.ERR_VOCABULARY_VALIDATION, system), e);
            throw new BusinessException(String.format(Constants.Logs.ERR_VOCABULARY_VALIDATION, system), e);
        }

        return validationSuccess;
    }

    @Override
    public List<String> findAllCodesExists(String system, List<String> codes) {
        
    	List<String> output = new ArrayList<>();
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("system").is(system).and("code").in(codes)
            		.and("deleted").is(false)); 

            List<TerminologyETY> etys = mongoTemplate.find(query, TerminologyETY.class);
            if(!etys.isEmpty()) {
            	output = etys.stream().map(TerminologyETY::getCode).collect(Collectors.toList());
            }
         } catch (Exception e) {
            log.error(String.format(Constants.Logs.ERR_VOCABULARY_VALIDATION, system), e);
            throw new BusinessException(String.format(Constants.Logs.ERR_VOCABULARY_VALIDATION, system), e);
        }

        return output;
    }
    
    @Override
    public boolean existBySystemAndCode(final String system, final String code) {
    	 boolean exists = false;
         try {
             Query query = new Query();
             query.addCriteria(Criteria.where("system").is(system).and("code").is(code)
            		 .and("deleted").is(false));

             exists = mongoTemplate.exists(query, TerminologyETY.class);
          } catch (Exception ex) {
        	  log.error("Error while execute find by system and code of vocabularies : " , ex);
      		  throw new BusinessException("Error while execute find by system and code of vocabularies : " , ex);
         }

         return exists;
    }

    @Override
    public boolean existBySystemAndNotCodes(String system, List<String> codes) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where(Constants.App.SYSTEM_KEY).is(system).and(Constants.App.CODE_KEY).not().in(codes));
            return mongoTemplate.exists(query, TerminologyETY.class);
        } catch (Exception e) {
            log.error("", e);
            throw new BusinessException("", e);
        }
    }

	@Override
	public List<TerminologyETY> getAllUniqueCodeSystemsAndVersions() {
		try {
//            return mongoTemplate.aggregate(null, null);
            return new ArrayList<>();
        } catch (Exception e) {
        	log.error("Error while retriev all codeSystems: " , e);
            throw new BusinessException("", e);
        }
	}
    
    
}
