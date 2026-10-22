/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.controller.impl;

import static it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum.OK;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum.SEMANTIC_WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.client.IFhirMappingClient;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronFailedAssertionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ValidationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.TransformResDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.SystemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 *	Validation controller.
 */
@Slf4j
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {
	
	
	@Autowired
	private IValidationFacadeSRV validationSRV;

	@Autowired
	private IFhirMappingClient fhirMappingClient;
	
	
	@Override
	public ValidationResponseDTO validation(ValidationRequestDTO requestBody, HttpServletRequest request, List<String> validations) {
		//recupera object id e mettilo nella risposta
		List<String> messages = new ArrayList<>();
		Validation.notNull(requestBody.getCda());
		
		RawValidationEnum outcome = RawValidationEnum.OK;

		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(requestBody.getCda(), request.getHeader(SYSTEM_TYPE_HEADER));

		CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(), infoDTO.getTypeIdExtension());
		if(CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
			if(StringUtility.isNullOrEmpty(validationResult.getMessage())){
				for(Entry<CDASeverityViolationEnum, List<String>> violations : validationResult.getViolations().entrySet()) {
					String severity = violations.getKey().toString();
					for(String violation : violations.getValue()) {
						messages.add(severity + ": " + violation);
					}
				}
			} else {
				messages.add(validationResult.getMessage());
			}
			outcome = RawValidationEnum.SYNTAX_ERROR;
		}	

		if(RawValidationEnum.OK.equals(outcome)) {
			SchematronValidationResultDTO semanticValidation = validationSRV.validateSemantic(requestBody.getCda(),infoDTO);
			if(StringUtility.isNullOrEmpty(semanticValidation.getMessage())){
				if(Boolean.FALSE.equals(semanticValidation.getValidSchematron())) {
					messages.add("Invalid schematron");
					outcome = RawValidationEnum.SEMANTIC_ERROR;
				} else if(semanticValidation.getFailedAssertions()!= null && !semanticValidation.getFailedAssertions().isEmpty()) {
					for(SchematronFailedAssertionDTO violation : semanticValidation.getFailedAssertions()) {
						messages.add(violation.getText());
					}
					outcome = RawValidationEnum.SEMANTIC_WARNING;
					if(Boolean.FALSE.equals(semanticValidation.getValidXML())){
						outcome = RawValidationEnum.SEMANTIC_ERROR;
					}
				}
			} else {
				messages.add(semanticValidation.getMessage());
				outcome = RawValidationEnum.SEMANTIC_ERROR;
			}

			if(infoDTO.getSystem() != SystemTypeEnum.TS) {
				if(RawValidationEnum.OK.equals(outcome) || RawValidationEnum.SEMANTIC_WARNING.equals(outcome)) {
					VocabularyResultDTO result =  validationSRV.validateVocabularies(requestBody.getCda(), requestBody.getWorkflowInstanceId());
					if(Boolean.TRUE.equals(result.getValid())) {
						log.debug("Validation completed successfully!");
					} else {
						outcome = RawValidationEnum.VOCABULARY_ERROR;
						messages = new ArrayList<>();
						messages.add(result.getMessage());
					}
				}
			} else {
				log.debug("Skipping vocabulary validation because system set as TS");
				log.debug("Validation completed successfully!");
			}
		}

		Pair<String, String> p = Pair.of("", "");
		if(Arrays.asList(OK, SEMANTIC_WARNING).contains(outcome)) {
			p = validationSRV.getStructureObjectID(infoDTO.getTemplateIdSchematron());

			if(validations != null && !validations.isEmpty()) {
				outcome = executeFhirValidations(requestBody.getCda(), p, validations, messages, outcome);
			}
		}

		ValidationInfoDTO out = ValidationInfoDTO.builder()
			.result(outcome)
			.message(messages)
			.engineID(p.getKey())
			.transformID(p.getValue()).
			build();

		return new ValidationResponseDTO(getLogTraceInfo(), out);
	}

	/**
	 * Esegue le validazioni aggiuntive invocando il fhir-mapping-engine.
	 * Se almeno una validazione non è valida l'esito è l'errore associato alla prima validazione
	 * (in ordine di esecuzione) non valida. I messaggi sono sempre popolati con le issues (severity e message).
	 *
	 * @param cda				cda da validare
	 * @param engineAndObject	coppia engineId e objectId (structure-map)
	 * @param validations		validazioni da eseguire
	 * @param messages			messaggi da popolare
	 * @param currentOutcome	esito corrente
	 * @return					esito aggiornato
	 */
	private RawValidationEnum executeFhirValidations(String cda, Pair<String, String> engineAndObject,
			List<String> validations, List<String> messages, RawValidationEnum currentOutcome) {

		TransformResDTO res = fhirMappingClient.callConvertCdaInBundle(
				new FhirResourceDTO(cda, engineAndObject.getValue(), engineAndObject.getKey()), validations);

		if(res == null) {
			throw new BusinessException("Risposta nulla dal fhir-mapping-engine durante l'esecuzione delle validazioni");
		}
		if(!StringUtility.isNullOrEmpty(res.getErrorMessage())) {
			throw new BusinessException("Errore dal fhir-mapping-engine durante l'esecuzione delle validazioni: " + res.getErrorMessage());
		}
		FhirValidationResponseDTO validationResponse = res.getValidationResponse();
		if(validationResponse == null || validationResponse.getResults() == null) {
			throw new BusinessException("Esiti delle validazioni assenti nella risposta del fhir-mapping-engine");
		}

		RawValidationEnum outcome = currentOutcome;
		boolean errorAssigned = false;

		for(FhirValidationResponseDTO.ValidationResultDTO result : validationResponse.getResults()) {
			if(!result.isValid() && !errorAssigned) {
				RawValidationEnum error = RawValidationEnum.fromValidationType(result.getType());
				if(error == null) {
					throw new BusinessException("Tipo di validazione non gestito: " + result.getType());
				}
				outcome = error;
				errorAssigned = true;
			}
			if(result.getIssues() != null) {
				for(FhirValidationResponseDTO.ValidationIssueDTO issue : result.getIssues()) {
					messages.add(issue.getSeverity() + ": " + issue.getMessage());
				}
			}
		}

		return outcome;
	}
	
}