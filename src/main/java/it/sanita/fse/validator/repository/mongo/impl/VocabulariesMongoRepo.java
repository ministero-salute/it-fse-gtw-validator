package it.sanita.fse.validator.repository.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.repository.entity.VocabularyETY;
import it.sanita.fse.validator.repository.mongo.IVocabulariesMongoRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class VocabulariesMongoRepo implements IVocabulariesMongoRepo {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public boolean allCodesExists(String system, List<String> codes) {

        boolean validationSuccess = true;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("system").is(system).and("code").in(codes));

            final long numExistingPairs = mongoTemplate.count(query, VocabularyETY.class);
            if (numExistingPairs != codes.size()) {
                log.info("System {} has {} terminology, but {} terminology are present in the database", system, codes.size(), numExistingPairs);
                validationSuccess = false;
            }
        } catch (Exception e) {
            log.error(String.format("Error while executing validation on vocabularies for system %s", system), e);
            throw new BusinessException(
                    String.format("Error while executing validation on vocabularies for system %s", system), e);
        }

        return validationSuccess;
    }
}
