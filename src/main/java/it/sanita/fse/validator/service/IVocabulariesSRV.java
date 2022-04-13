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
     * Insert the vocabulary into the repository if not already present.
     * 
     * @param system The system.
     * @param code   The code of the vocabulary.
     * @return {@code true} if the vocabulary was inserted, {@code false} otherwise.
     */
    boolean cacheVocabulary(String system, String code);

    /**
     * Returns {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     * 
     * @param terminology The terminology to validate.
     * @return {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     */
    boolean vocabulariesExists(Map<String, List<String>> terminology);

}
