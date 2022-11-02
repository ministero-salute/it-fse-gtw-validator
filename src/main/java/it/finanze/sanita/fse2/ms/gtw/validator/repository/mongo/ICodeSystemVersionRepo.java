package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.CodeSystemVersionETY;

public interface ICodeSystemVersionRepo {

    List<CodeSystemVersionETY> getCodeSystems();

}
