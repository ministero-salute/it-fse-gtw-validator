package it.finanze.sanita.fse2.ms.gtw.validator.service.facade;

import java.io.Serializable;

public interface IVocabulariesFacadeSRV extends Serializable {

	boolean existBySystemAndCode(String system, String code);
}
