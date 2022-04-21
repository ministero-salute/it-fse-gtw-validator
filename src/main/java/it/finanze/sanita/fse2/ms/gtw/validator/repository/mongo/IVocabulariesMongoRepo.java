package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.util.List;

public interface IVocabulariesMongoRepo {

    /**
     * Returns {@code true} if all the codes are present in the database, {@code false} otherwise.
     * 
     * @param system The system of codes.
     * @param codes  The codes to check.
     * @return {@code true} if all the codes are present in the database, {@code false} otherwise.
     */
    boolean allCodesExists(String system, List<String> codes);
}
