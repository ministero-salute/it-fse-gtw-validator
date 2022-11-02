package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility.isNullOrEmpty;

import java.util.Arrays;
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
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.CodeSystemVersionETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ICodeSystemVersionRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TerminologySRV implements ITerminologySRV {

    private static final long serialVersionUID = 4152763404310972229L;

	private static final List<String> INVALID_VALUES = Arrays.asList("999", "9999");

    @Autowired
    private transient ITerminologyRepo terminologyRepo;
    
    @Autowired
    private transient ICodeSystemVersionRepo codeSystemRepo;

    @Override
    public VocabularyResultDTO validateTerminologies(TerminologyExtractionDTO terminologies) {
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
    }

	private CodeSystemSnapshotDTO retrieveManagedCodeSystems() {
		List<CodeSystemVersionETY> codeSystems = codeSystemRepo.getCodeSystems();
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
        List<String> blackListed = getBlackList(terminologies.getCodeSystems());
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
		List<CodeDTO> codes = sanitizeMissingVersion(terminologies.getCodes(), snapshot);
		terminologies.getCodes().clear();
		terminologies.getCodes().addAll(codes);
	}
	
    private void consumeInvalidVersion(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
    	List<CodeSystemVersionDTO> managed = snapshot.getCodeSystemVersions();
        List<CodeSystemVersionDTO> invalid = terminologies.rejectCodeSystemVersions(managed);
        sendLogForInvalidVersions(invalid);
        throwExceptionForInvalidVersions(invalid);
	}

	private void consumeCodes(TerminologyExtractionDTO terminologies) {
		Map<CodeSystemVersionDTO, List<String>> groupedCodes = groupByCodeSystemVersion(terminologies);
		List<CodeDTO> managedCodes = groupedCodes
				.keySet()
				.stream()
				.flatMap(key -> findByCodeSystemVersion(key, groupedCodes.get(key)).stream())
				.collect(Collectors.toList());
		terminologies.removeCodes(managedCodes);
	}
	
	private Map<CodeSystemVersionDTO, List<String>> groupByCodeSystemVersion(TerminologyExtractionDTO terminologies) {
		return terminologies
				.getCodes()
				.stream()
				.collect(Collectors.groupingBy(CodeDTO::getCodeSystemVersion, Collectors.mapping(CodeDTO::getCode, Collectors.toList())));
	}
	
	private List<CodeDTO> findByCodeSystemVersion(CodeSystemVersionDTO codeSystemVersion, List<String> codes) {
		List<String> foundCodes = terminologyRepo.findAllCodesExistsForVersion(codeSystemVersion.getCodeSystem(), codeSystemVersion.getVersion(), codes); 
		return getManagedCodes(codeSystemVersion, foundCodes);
	}

	private List<CodeDTO> getManagedCodes(CodeSystemVersionDTO codeSystemVersion, List<String> codes) {
		return codes
				.stream()
				.map(code -> new CodeDTO(codeSystemVersion.getCodeSystem(), codeSystemVersion.getVersion(), code))
				.collect(Collectors.toList());
	}

	private void manageRemainingCodes(TerminologyExtractionDTO terminologies) {
		if (terminologies.getCodes().isEmpty()) return;
        log.warn("CodeSystem not found on Mongo, Terminology Validation Failed!");
		sendLogForInvalidCodes(terminologies.getCodes());
	}
	
	private List<CodeDTO> sanitizeMissingVersion(List<CodeDTO> codes, CodeSystemSnapshotDTO snapshot) {
		return codes
				.stream()
				.peek(code -> sanitizeMissingVersion(code, snapshot))
				.collect(Collectors.toList());	
	}

	private void sanitizeMissingVersion(CodeDTO code, CodeSystemSnapshotDTO snapshot) {
		if (!isNullOrEmpty(code.getVersion())) return;
		String codeSystem = code.getCodeSystem();
		String maxVersion = snapshot.getCodeSystemMaxVersions().get(codeSystem);	
		code.setVersion(maxVersion);
	}
	
	private List<String> getBlackList(List<String> codeSystems) {
		return codeSystems
				.stream()
				.filter(this::isBlacklisted)
				.collect(Collectors.toList());
	}
	
    private boolean isBlacklisted(String codeSystem) {
        if (isNullOrEmpty(codeSystem)) return false;
        return Arrays
            .stream(codeSystem.split("\\."))
            .anyMatch(INVALID_VALUES::contains);
    }
    
	private VocabularyResultDTO getResult(TerminologyExtractionDTO terminologies) {
		boolean isValid = terminologies.getCodes().isEmpty();
		String message = getRemainingCodeSystemMessage(terminologies);
		return new VocabularyResultDTO(isValid, message);
	}

	private String getRemainingCodeSystemMessage(TerminologyExtractionDTO terminologies) {
		return terminologies.getCodes().toString();
		//"[Dizionario : " + system + " ,Vocaboli:" + String.join(",", differences) + "]
	}

   	private void sendLogForWhiteList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    	log.warn("Whitelisted CodeSystems found during the validation: [{}]", codeSystems);
    }

    private void sendLogForBlackList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    	log.error("Blacklisted CodeSystems found during the validation: [{}]", codeSystems);
    }

    private void sendLogForUnknown(List<String> codeSystems) {
		if (codeSystems.isEmpty()) return;
		log.warn("Unknown CodeSystems found during the validation: [{}]", codeSystems);
	}
	
	private void sendLogForInvalidVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		if (codeSystemVersions.isEmpty()) return;
		List<String> versions = codeSystemVersions
				.stream()
				.map(CodeSystemVersionDTO::toString)
				.collect(Collectors.toList());
		log.error("Invalid CodeSystemVersions found during the validation: [{}]", versions);
	}

	private void sendLogForInvalidCodes(List<CodeDTO> codes) {
		if (codes.isEmpty()) return;
		List<String> versions = codes
				.stream()
				.map(CodeDTO::getCode)
				.collect(Collectors.toList());
		log.warn("Invalid Codes found during the validation: [{}]", versions);
	}

	private void throwExceptionForEmptyDatabase(List<CodeSystemVersionETY> codeSystems) {
		if (codeSystems.isEmpty()) return;
        log.error("Managed CodeSystems not found in database");
        throw new BusinessException("Managed CodeSystems not found in database");
	}
	
	private void throwExceptionForBlackList(List<String> blackListed) {
		if (blackListed.isEmpty()) return;
        throw new BusinessException("BlackListed CodeSystems found during the validation");
	}

	private void throwExceptionForInvalidVersions(List<CodeSystemVersionDTO> invalid) {
		if (invalid.isEmpty()) return;
        throw new BusinessException("Invalid CodeSystemVersions found during the validation");		
	}
}
