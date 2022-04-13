package it.sanita.fse.validator.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.service.IValidationSRV;
import it.sanita.fse.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationSRV implements IValidationSRV {

    @Autowired
    private IVocabulariesSRV vocabulariesRedisSRV;

    @Override
    public boolean validateVocabularies(final Map<String, List<String>> vocabularies) {
        boolean validationSuccess = true;
        log.info("Validating {} vocabularies...", vocabularies.size());

        try {
            validationSuccess = vocabulariesRedisSRV.vocabulariesExists(vocabularies);
        } catch (Exception e) {
            log.error("Error while executing validation on vocabularies", e);
            throw new BusinessException("Error while executing validation on vocabularies", e);
        }

        return validationSuccess;
    }
    
}
