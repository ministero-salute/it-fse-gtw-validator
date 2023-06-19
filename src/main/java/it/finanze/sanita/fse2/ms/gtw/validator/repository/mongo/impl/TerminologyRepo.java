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
    public List<String> findAllCodesExistsForVersion(String system, String version, List<String> codes) {
        
    	List<String> output = new ArrayList<>();
        try {
            Criteria criteria = Criteria
            		.where("system").is(system)
            		.and("code").in(codes)
            		.and("deleted").is(false);
            if (version != null) criteria = criteria.and("version").is(version);
            Query query = new Query();
            query.addCriteria(criteria);

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

    
}
