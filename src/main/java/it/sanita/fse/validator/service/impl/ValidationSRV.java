package it.sanita.fse.validator.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.sanita.fse.validator.cda.CDAHelper;
import it.sanita.fse.validator.exceptions.BusinessException;
import it.sanita.fse.validator.service.IValidationSRV;
import it.sanita.fse.validator.service.IVocabulariesSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationSRV implements IValidationSRV {

    @Autowired
    private IVocabulariesSRV vocabulariesRV;

    @Override
    public boolean validateVocabularies(final String cda) {
        boolean validationSuccess = true;
        
        try {
            Map<String, List<String>> vocabularies = CDAHelper.extractTerminology(cda);
            log.info("Validating {} systems...", vocabularies.size());
            validationSuccess = vocabulariesRV.vocabulariesExists(vocabularies);
        } catch (Exception e) {
            log.error("Error while executing validation on vocabularies", e);
            throw new BusinessException("Error while executing validation on vocabularies", e);
        }

        return validationSuccess;
    }
    
}
