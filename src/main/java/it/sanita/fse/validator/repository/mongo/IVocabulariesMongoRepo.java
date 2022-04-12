package it.sanita.fse.validator.repository.mongo;

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
