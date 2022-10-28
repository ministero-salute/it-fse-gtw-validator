package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;

public interface ITerminologySRV extends Serializable {

	VocabularyResultDTO validateCodeSystems(CodeSystemSnapshotDTO snapshot);

}
