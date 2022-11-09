/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.TerminologyExtractionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.VocabularyException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TerminologySRV implements ITerminologySRV {

    private static final long serialVersionUID = 4152763404310972229L;

    @Autowired
    private transient ITerminologyRepo terminologyRepo;
    
    @Autowired
    private transient IDictionaryRepo codeSystemRepo;

    @Override
    public VocabularyResultDTO validateTerminologies(TerminologyExtractionDTO terminologies) {
    	try {
	        log.debug("Terminology Validation Stated!");
	        CodeSystemSnapshotDTO snapshot = retrieveManagedCodeSystems();
	        consumeWhiteList(terminologies, snapshot);
	        consumeBlackList(terminologies);
	        consumeUnknown(terminologies, snapshot);
	        sanitizeMissingVersion(terminologies, snapshot);
	        consumeInvalidVersion(terminologies, snapshot);
	        consumeCodes(terminologies);
	        manageRemainingCodes(terminologies);
	        log.debug("Terminology Validation Ended!");
	        return getResult(terminologies);
    	} catch (VocabularyException e) {
         	return new VocabularyResultDTO(false, e.getMessage());
 		}
    }

	private CodeSystemSnapshotDTO retrieveManagedCodeSystems() {
		List<DictionaryETY> codeSystems = codeSystemRepo.getCodeSystems();
		throwExceptionForEmptyDatabase(codeSystems);
		return new CodeSystemSnapshotDTO(codeSystems);
	}

	private void consumeWhiteList(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
        List<String> whiteList = snapshot.getWhiteList();
        List<String> whiteListed = terminologies.filterCodeSystems(whiteList);
        terminologies.removeCodeSystems(whiteListed);
        sendLogForWhiteList(whiteListed);
    }

	private void consumeBlackList(TerminologyExtractionDTO terminologies) {
        List<String> blackListed = CodeSystemUtility.getBlackList(terminologies.getCodeSystems());
        terminologies.removeCodeSystems(blackListed);
        sendLogForBlackList(blackListed);
        throwExceptionForBlackList(blackListed);
    }

	private void consumeUnknown(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
        List<String> managed = snapshot.getCodeSystems();
        List<String> unknown = terminologies.rejectCodeSystems(managed);
        terminologies.removeCodeSystems(unknown);
        sendLogForUnknown(unknown);
    }

	private void sanitizeMissingVersion(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
		List<CodeDTO> codes = CodeSystemUtility.sanitizeMissingVersion(terminologies.getCodes(), snapshot);
		if (codes.isEmpty()) return;
		log.warn("Sanitizing missing CodeSystemVersions: {}", codes.toString());
		terminologies.getCodes().removeAll(codes);
		terminologies.getCodes().addAll(codes);
	}

    private void consumeInvalidVersion(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
    	List<CodeSystemVersionDTO> managed = snapshot.getCodeSystemVersions();
        List<CodeSystemVersionDTO> invalid = terminologies.rejectCodeSystemVersions(managed);
        sendLogForInvalidVersions(invalid);
        throwExceptionForInvalidVersions(invalid);
	}

	private void consumeCodes(TerminologyExtractionDTO terminologies) {
		Map<CodeSystemVersionDTO, List<CodeDTO>> groupedCodes = groupByCodeSystemVersion(terminologies);
		List<CodeDTO> managedCodes = groupedCodes
				.keySet()
				.stream()
				.flatMap(key -> findByCodeSystemVersion(key, groupedCodes.get(key)).stream())
				.collect(Collectors.toList());
		terminologies.removeCodes(managedCodes);
	}
	
	private Map<CodeSystemVersionDTO, List<CodeDTO>> groupByCodeSystemVersion(TerminologyExtractionDTO terminologies) {
		return terminologies
				.getCodes()
				.stream()
				.collect(Collectors.groupingBy(this::getCodeSystemVersionGroupKey));
	}
	
	private List<CodeDTO> findByCodeSystemVersion(CodeSystemVersionDTO codeSystemVersion, List<CodeDTO> codeDTOs) {
		List<String> codes = codeDTOs.stream().map(CodeDTO::getCode).collect(Collectors.toList());
		List<String> foundCodes = terminologyRepo.findAllCodesExistsForVersion(codeSystemVersion.getCodeSystem(), codeSystemVersion.getVersion(), codes); 
		codeDTOs.removeIf(code -> !foundCodes.contains(code.getCode()));
		return codeDTOs;
	}

	private void manageRemainingCodes(TerminologyExtractionDTO terminologies) {
		if (terminologies.getCodes().isEmpty()) return;
        log.warn("One or more CodeSystems were not found on Mongo, Terminology Validation Failed!");
        sendLogForInvalidCodes(terminologies.getCodes());
	}
	
	private VocabularyResultDTO getResult(TerminologyExtractionDTO terminologies) {
		boolean isValid = terminologies.getCodes().isEmpty();
		String message = CodeSystemUtility.getGroupedMessage(terminologies);
		return new VocabularyResultDTO(isValid, message);
	}

	private void sendLogForWhiteList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    	log.warn("Whitelisted CodeSystems found during the validation: {}", codeSystems);
    }

    private void sendLogForBlackList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    	log.error("Blacklisted CodeSystems found during the validation: {}", codeSystems);
    }

    private void sendLogForUnknown(List<String> codeSystems) {
		if (codeSystems.isEmpty()) return;
		log.warn("Unknown CodeSystems found during the validation: {}", codeSystems);
	}
	
	private void sendLogForInvalidVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		if (codeSystemVersions.isEmpty()) return;
		List<String> versions = codeSystemVersions
				.stream()
				.map(CodeSystemVersionDTO::toString)
				.collect(Collectors.toList());
		log.error("Invalid CodeSystemVersions found during the validation: {}", versions);
	}

	private void sendLogForInvalidCodes(List<CodeDTO> codes) {
		if (codes.isEmpty()) return;
		List<String> versions = codes
				.stream()
				.map(CodeDTO::toString)
				.collect(Collectors.toList());
		log.warn("Invalid Codes found during the validation: {}", versions);
	}

	private void throwExceptionForEmptyDatabase(List<DictionaryETY> codeSystems) {
		if (!codeSystems.isEmpty()) return;
        log.error("Managed CodeSystems not found in database");
        throw new VocabularyException("Non è stato trovato alcun dizionario gestito su FSE");
	}
	
	private void throwExceptionForBlackList(List<String> blackListed) {
		if (blackListed.isEmpty()) return;
        throw new VocabularyException("È stato trovato almeno un dizionario presente in Blacklist: " + blackListed.toString());
	}

	private void throwExceptionForInvalidVersions(List<CodeSystemVersionDTO> invalid) {
		if (invalid.isEmpty()) return;
        throw new VocabularyException("È stato trovato almeno una versione non gestita di un dizionario: " + invalid.toString());		
	}

	private CodeSystemVersionDTO getCodeSystemVersionGroupKey(CodeDTO code) {
		CodeSystemVersionDTO codeSystemVersion = code.getCodeSystemVersion();
		if (!code.isAnswerList()) return codeSystemVersion; 
		return CodeSystemUtility.getAnswerList(codeSystemVersion);
	}
	
}
