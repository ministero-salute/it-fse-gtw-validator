/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.CodeSystemVersionETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ICodeSystemVersionRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class CodeSystemVersionRepo implements ICodeSystemVersionRepo {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<CodeSystemVersionETY> getCodeSystems() {
        try {
            return mongoTemplate.findAll(CodeSystemVersionETY.class);
         } catch (Exception e) {
            log.error("Error while retrieving all codeSystemVersions from Mongo", e);
            throw new BusinessException("Error while retrieving all codeSystemVersions from Mongo", e);
        }
    }
    
}
