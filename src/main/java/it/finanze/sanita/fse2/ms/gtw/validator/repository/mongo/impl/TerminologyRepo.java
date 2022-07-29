package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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
            query.addCriteria(Criteria.where("system").is(system).and("code").in(codes));

            validationSuccess = mongoTemplate.exists(query, TerminologyETY.class);
         } catch (Exception e) {
            log.error(String.format("Error while executing validation on vocabularies for system %s", system), e);
            throw new BusinessException(String.format("Error while executing validation on vocabularies for system %s", system), e);
        }

        return validationSuccess;
    }

    @Override
    public List<String> findAllCodesExists(String system, List<String> codes) {
        
    	List<String> output = new ArrayList<>();
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("system").is(system).and("code").in(codes));

            List<TerminologyETY> etys = mongoTemplate.find(query, TerminologyETY.class);
            if(!etys.isEmpty()) {
            	output = etys.stream().map(e-> e.getCode()).collect(Collectors.toList());
            }
         } catch (Exception e) {
            log.error(String.format("Error while executing validation on vocabularies for system %s", system), e);
            throw new BusinessException(String.format("Error while executing validation on vocabularies for system %s", system), e);
        }

        return output;
    }
    
    @Override
    public boolean existBySystemAndCode(final String system, final String code) {
    	 boolean exists = false;
         try {
             Query query = new Query();
             query.addCriteria(Criteria.where("system").is(system).and("code").is(code));

             exists = mongoTemplate.exists(query, TerminologyETY.class);
          } catch (Exception ex) {
        	  log.error("Error while execute find by system and code of vocabularies : " , ex);
      		  throw new BusinessException("Error while execute find by system and code of vocabularies : " , ex);
         }

         return exists;
    }
}
