package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemSnapshotDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CodeSystemVersionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.TerminologyValidatorSingleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TerminologySRV implements ITerminologySRV {

    private static final long serialVersionUID = 4152763404310972229L;

	private static final List<String> INVALID_VALUES = Arrays.asList("999", "9999");

    @Autowired
    private transient ITerminologyRepo terminologyRepo;

    @Override
    public VocabularyResultDTO validateCodeSystems(CodeSystemSnapshotDTO snapshot) {
        log.debug("Terminology Validation Stated!");
        consumeWhiteList(snapshot);
        consumeBlackList(snapshot);
        consumeUnknown(snapshot);
        sanitizeMissingVersion(snapshot);
        consumeInvalidVersion(snapshot);
        consumeCodes(snapshot);
        manageRemainingCodes(snapshot);
        log.debug("Terminology Validation Ended!");
        return getResult(snapshot);
    }

	private void sanitizeMissingVersion(CodeSystemSnapshotDTO snapshot) {
		List<CodeDTO> codes = sanitizeMissingVersion(snapshot.getCodes());
		snapshot.getCodes().clear();
		snapshot.getCodes().addAll(codes);
	}

	private void consumeWhiteList(CodeSystemSnapshotDTO snapshot) {
        List<String> whiteList = getWhiteList();
        List<String> whiteListed = snapshot.filterCodeSystems(whiteList);
        snapshot.removeCodeSystems(whiteListed);
        sendLogForWhiteList(whiteListed);
    }

	private void consumeBlackList(CodeSystemSnapshotDTO snapshot) {
        List<String> blackListed = getBlackList(snapshot.getCodeSystems());
        sendLogForBlackList(blackListed);
        throwExceptionForBlackList(blackListed);
    }

	private void consumeUnknown(CodeSystemSnapshotDTO snapshot) {
        List<String> managed = getAllCodeSystems();
        List<String> unknown = snapshot.rejectCodeSystems(managed);
        snapshot.removeCodeSystems(unknown);
        sendLogForUnknown(unknown);
    }

    private void consumeInvalidVersion(CodeSystemSnapshotDTO snapshot) {
    	List<CodeSystemVersionDTO> managed = getAllCodeSystemVersions();
        List<CodeSystemVersionDTO> invalid = snapshot.rejectCodeSystemVersions(managed);
        sendLogForInvalidVersions(invalid);
        throwExceptionForInvalidVersions(invalid);
	}

	private void consumeCodes(CodeSystemSnapshotDTO snapshot) {
		//raggruppare lista codici per stesso codeSystem e version 
		// TODO Auto-generated method stub
		List<CodeDTO> managed = new ArrayList<>();
		snapshot.removeCodes(managed);
	}

	private void manageRemainingCodes(CodeSystemSnapshotDTO snapshot) {
		if (snapshot.getCodes().isEmpty()) return;
        log.warn("CodeSystem not found on Mongo, Terminology Validation Failed!");
		sendLogForInvalidCodes(snapshot.getCodes());
	}

    private void sendLogForInvalidCodes(List<CodeDTO> codes) {
		// TODO Auto-generated method stub
    	
		
	}

	private void sendLogForWhiteList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    }

    private void sendLogForBlackList(List<String> codeSystems) {
    	if (codeSystems.isEmpty()) return;
    }

    private void sendLogForUnknown(List<String> codeSystems) {
		if (codeSystems.isEmpty()) return;
	}
	
	private void sendLogForInvalidVersions(List<CodeSystemVersionDTO> codeSystemVersions) {
		if (codeSystemVersions.isEmpty()) return;
	}
	
    private List<String> getWhiteList() {
		return TerminologyValidatorSingleton.getInstance().getWhiteList();
	}

	private List<String> getAllCodeSystems() {
		return TerminologyValidatorSingleton.getInstance().getCodeSystems();
	}
	
	private List<CodeSystemVersionDTO> getAllCodeSystemVersions() {
		return TerminologyValidatorSingleton.getInstance().getCodeSystemVersions();
	}

	private Map<String, String> getAllCodeSystemMaxVersions() {
		return TerminologyValidatorSingleton.getInstance().getCodeSystemMaxVersions();
	}

	private void throwExceptionForBlackList(List<String> blackListed) {
		if (blackListed.isEmpty()) return;
        log.error("BlackListed CodeSystems found during the validation: [{}]", blackListed);
        throw new BusinessException("BlackListed CodeSystems found during the validation");
	}

	private void throwExceptionForInvalidVersions(List<CodeSystemVersionDTO> invalid) {
		if (invalid.isEmpty()) return;
        log.error("Invalid CodeSystemVersions found during the validation: [{}]", invalid);
        throw new BusinessException("Invalid CodeSystemVersions found during the validation");		
	}
	
	private List<CodeDTO> sanitizeMissingVersion(List<CodeDTO> codes) {
		return codes
				.stream()
				.peek(code -> sanitizeMissingVersion(code))
				.collect(Collectors.toList());	
	}

	private void sanitizeMissingVersion(CodeDTO code) {
		if (!isNullOrEmpty(code.getCodeSystemVersion())) return;
		code.setCodeSystemVersion(getMaxVersion(code));
	}
	
	private String getMaxVersion(CodeDTO code) {
		String codeSystem = code.getCodeSystem();
    	return getAllCodeSystemMaxVersions().get(codeSystem);	
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
    
	private VocabularyResultDTO getResult(CodeSystemSnapshotDTO snapshot) {
		boolean isValid = snapshot.getCodes().isEmpty();
		String message = getRemainingCodeSystemMessage(snapshot);
		return new VocabularyResultDTO(isValid, message);
	}

	private String getRemainingCodeSystemMessage(CodeSystemSnapshotDTO snapshot) {
		return snapshot.getCodes().toString();
		//"[Dizionario : " + system + " ,Vocaboli:" + String.join(",", differences) + "]
	}
}
