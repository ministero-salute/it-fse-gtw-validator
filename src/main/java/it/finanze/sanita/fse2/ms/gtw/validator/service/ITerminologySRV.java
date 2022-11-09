/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.TerminologyExtractionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;

public interface ITerminologySRV extends Serializable {

	VocabularyResultDTO validateTerminologies(TerminologyExtractionDTO snapshot);

}
