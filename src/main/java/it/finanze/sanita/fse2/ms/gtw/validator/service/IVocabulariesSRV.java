/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.util.List;
import java.util.Map;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IVocabulariesFacadeSRV;

/**
 * Interface of Vocabulary Service.
 * 
 */
public interface IVocabulariesSRV extends IVocabulariesFacadeSRV {

    /**
     * Returns {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     * 
     * @param terminology The terminology to validate.
     * @return {@code true} if all vocabularies exists in the repository, {@code false} otherwise.
     */
    VocabularyResultDTO vocabulariesExists(Map<String, List<String>> terminology);

}
