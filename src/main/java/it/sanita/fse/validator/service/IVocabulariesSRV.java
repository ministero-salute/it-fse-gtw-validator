package it.sanita.fse.validator.service;

import java.util.List;
import java.util.Map;

import it.sanita.fse.validator.service.facade.IVocabulariesFacadeSRV;

/**
 * Interface of Vocabulary Service.
 * 
 * @author Simone Lungarella
 */
public interface IVocabulariesSRV extends IVocabulariesFacadeSRV {

    /**
     * Returns {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     * 
     * @param terminology The terminology to validate.
     * @return {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     */
    boolean vocabulariesExists(Map<String, List<String>> terminology);

}