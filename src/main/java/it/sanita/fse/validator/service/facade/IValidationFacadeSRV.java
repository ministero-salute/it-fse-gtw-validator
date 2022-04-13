package it.sanita.fse.validator.service.facade;

/**
 * Interface of Validation Service.
 * 
 * @author Simone Lungarella
 */
public interface IValidationFacadeSRV {

    /**
     * Execute validation on the given terminology.
     * 
     * @param cda CDA to validate.
     * @return The result of the validation.
     */
    boolean validateVocabularies(String cda);
}
