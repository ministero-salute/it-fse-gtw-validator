/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.WarnLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.VocabularyException;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ITerminologyRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.ITerminologySRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.CodeSystemUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TerminologySRV implements ITerminologySRV {

    @Autowired
    private ITerminologyRepo terminologyRepo;
    
    @Autowired
    private IDictionaryRepo codeSystemRepo;

	@Autowired
	private LoggerHelper logger;

    @Override
    public VocabularyResultDTO validateTerminologies(TerminologyExtractionDTO terminologies, final String workflowInstanceId) {
    	try {
	        log.debug("Terminology Validation Stated!");
	        CodeSystemSnapshotDTO snapshot = retrieveManagedCodeSystems();
	        consumeAllowList(terminologies, snapshot,workflowInstanceId);
	        consumeBlockList(terminologies,workflowInstanceId);
	        consumeUnknown(terminologies, snapshot,workflowInstanceId);
	        sanitizeMissingVersion(terminologies, snapshot);
	        consumeInvalidVersion(terminologies, snapshot,workflowInstanceId);
	        consumeCodes(terminologies);
	        manageRemainingCodes(terminologies,workflowInstanceId);
	        log.debug("Terminology Validation Ended!");
	        return getResult(terminologies);
    	} catch (VocabularyException e) {
         	return new VocabularyResultDTO(false, e.getMessage());
 		}
    }
    
	public CodeSystemSnapshotDTO retrieveManagedCodeSystems() {
		List<DictionaryETY> codeSystems = codeSystemRepo.getCodeSystems();
		throwExceptionForEmptyDatabase(codeSystems);
		return new CodeSystemSnapshotDTO(codeSystems);
	}

	private void consumeAllowList(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot, final String workflowInstanceId) {
		Date startOperation = new Date();
        List<String> allowList = snapshot.getAllowList();
        List<String> allowListed = terminologies.filterCodeSystems(allowList);
        terminologies.removeCodeSystems(allowListed);
        sendLogForAllowList(allowListed, startOperation, workflowInstanceId);
    }

	private void consumeBlockList(TerminologyExtractionDTO terminologies, final String workflowInstanceId) {
		Date startOperation = new Date();
        List<String> blockListed = CodeSystemUtility.getBlockList(terminologies.getCodeSystems());
        terminologies.removeCodeSystems(blockListed);
        sendLogForBlockList(blockListed, startOperation,workflowInstanceId);
        throwExceptionForBlockList(blockListed);
    }

	private void consumeUnknown(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot, final String worfklowInstanceId) {
		Date startOperation = new Date();
        List<String> managed = snapshot.getCodeSystems();
        List<String> unknown = terminologies.rejectCodeSystems(managed);
        terminologies.removeCodeSystems(unknown);
        sendLogForUnknown(unknown, startOperation,worfklowInstanceId);
    }

	private void sanitizeMissingVersion(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot) {
		List<CodeDTO> codes = CodeSystemUtility.sanitizeMissingVersion(terminologies.getCodes(), snapshot);
		if (codes.isEmpty()) return;
		log.warn("Sanitizing missing CodeSystemVersions: {}", codes.toString());
		terminologies.getCodes().removeAll(codes);
		terminologies.getCodes().addAll(codes);
	}

    private void consumeInvalidVersion(TerminologyExtractionDTO terminologies, CodeSystemSnapshotDTO snapshot, final String workflowInstanceId) {
		Date startOperation = new Date();
    	List<CodeSystemVersionDTO> managed = snapshot.getCodeSystemVersions();
        List<CodeSystemVersionDTO> invalid = terminologies.rejectCodeSystemVersions(managed);
        sendLogForInvalidVersions(invalid, startOperation,workflowInstanceId);
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
				.collect(Collectors.groupingBy(CodeDTO::getCodeSystemVersion));
	}
	
	private List<CodeDTO> findByCodeSystemVersion(CodeSystemVersionDTO codeSystemVersion, List<CodeDTO> codeDTOs) {
		List<String> codes = codeDTOs.stream().map(CodeDTO::getCode).collect(Collectors.toList());
		List<String> foundCodes = terminologyRepo.findAllCodesExistsForVersion(codeSystemVersion.getCodeSystem(), codeSystemVersion.getVersion(), codes); 
		codeDTOs.removeIf(code -> !foundCodes.contains(code.getCode()));
		return codeDTOs;
	}

	private void manageRemainingCodes(TerminologyExtractionDTO terminologies, final String workflowInstanceId) {
		Date startOperation = new Date();
		if (terminologies.getCodes().isEmpty()) return;
        log.warn("One or more CodeSystems were not found on Mongo, Terminology Validation Failed!");
        sendLogForInvalidCodes(terminologies.getCodes(), startOperation,workflowInstanceId);
	}
	
	private VocabularyResultDTO getResult(TerminologyExtractionDTO terminologies) {
		boolean isValid = terminologies.getCodes().isEmpty();
		String message = CodeSystemUtility.getGroupedMessage(terminologies);
		return new VocabularyResultDTO(isValid, message);
	}

	private void sendLogForAllowList(List<String> codeSystems, Date startOperation, final String workflowInstanceId) {
    	if (codeSystems.isEmpty()) return;
		logger.warn(workflowInstanceId,String.format("Allowlisted CodeSystems found during the validation: [%s]", codeSystems.stream().collect(Collectors.joining(", "))), OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.WARN, startOperation, WarnLogEnum.ALLOWED);
    }

    private void sendLogForBlockList(List<String> codeSystems, Date startOperation, final String workflowInstanceId) {
    	if (codeSystems.isEmpty()) return;
		logger.error(workflowInstanceId,String.format("Blocklisted CodeSystems found during the validation: [%s]", codeSystems.stream().collect(Collectors.joining(", "))), OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.KO, startOperation, ErrorLogEnum.BLOCKLIST_ERROR);
    }

    private void sendLogForUnknown(List<String> codeSystems, Date startOperation, final String workflowInstanceId) {
		if (codeSystems.isEmpty()) return;
		logger.warn(workflowInstanceId,String.format("Unknown CodeSystems found during the validation: [%s]", codeSystems.stream().collect(Collectors.joining(", "))), OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.WARN, startOperation, WarnLogEnum.UNKNOWN);
	}
	
	private void sendLogForInvalidVersions(List<CodeSystemVersionDTO> codeSystemVersions, Date startOperation, final String workflowInstanceId) {
		if (codeSystemVersions.isEmpty()) return;
		List<String> versions = codeSystemVersions
				.stream()
				.map(CodeSystemVersionDTO::toString)
				.collect(Collectors.toList());
		logger.error(workflowInstanceId,String.format("Invalid CodeSystemVersions found during the validation: [%s]", versions.stream().collect(Collectors.joining(", "))), OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.KO, startOperation, ErrorLogEnum.INVALID_VERSION);
	}

	private void sendLogForInvalidCodes(List<CodeDTO> codes, Date startOperation, final String workflowInstanceId) {
		if (codes.isEmpty()) return;
		List<String> versions = logGroupedBySystem(codes);
		
		logger.warn(workflowInstanceId,String.format("Invalid Codes found during the validation: [%s]", versions.stream().collect(Collectors.joining(", "))), OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.WARN, startOperation, WarnLogEnum.INVALID_CODE);
	}

	private List<String> logGroupedBySystem(List<CodeDTO> codes) {
		Map<String, String> systems = new HashMap<>();
		List<String> logs = new ArrayList<>();
		codes.forEach(dto -> systems.putIfAbsent(dto.getCodeSystem(), dto.getCodeSystemName()));
		
		for (Map.Entry<String, String> entry : systems.entrySet()) {
			List<String> innerCodes = new ArrayList<>();
			innerCodes.addAll(codes.stream()
			.filter(dto -> dto.getCodeSystem().equals(entry.getKey()))
			.map(CodeDTO::toString)
			.collect(Collectors.toList()));

			String log = String.format("{\"system\":\"%s\",\"systemName\":\"%s\",\"codes\":\"[%s]}", entry.getKey(), entry.getValue(), innerCodes.stream().collect(Collectors.joining(", ")));
			logs.add(log);
		} 
		
		return logs;
	}

	private void throwExceptionForEmptyDatabase(List<DictionaryETY> codeSystems) {
		if (!codeSystems.isEmpty()) return;
        log.error("Managed CodeSystems not found in database");
        throw new VocabularyException("Non è stato trovato alcun dizionario gestito su FSE");
	}
	
	private void throwExceptionForBlockList(List<String> blockListed) {
		if (blockListed.isEmpty()) return;
        throw new VocabularyException("È stato trovato almeno un dizionario presente in Blocklist: " + blockListed.toString());
	}

	private void throwExceptionForInvalidVersions(List<CodeSystemVersionDTO> invalid) {
		if (invalid.isEmpty()) return;
        throw new VocabularyException("È stata trovata almeno una versione non gestita di un dizionario: " + invalid.toString());		
	}

}
