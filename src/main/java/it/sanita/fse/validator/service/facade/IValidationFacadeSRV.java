package it.sanita.fse.validator.service.facade;

import java.util.List;
import java.util.Map;

/**
 * Interface of Validation Service.
 * 
 * @author Simone Lungarella
 */
public interface IValidationFacadeSRV {

    /**
     * Execute validation on the given terminology.
     * 
     * @param vocabularies The terminology to validate.
     * @return The result of the validation.
     */
    boolean validateVocabularies(Map<String, List<String>> vocabularies);
}
