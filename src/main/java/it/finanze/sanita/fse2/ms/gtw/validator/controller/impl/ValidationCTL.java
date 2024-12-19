
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

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.controller.Validation;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.request.ValidationRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.ValidationResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDASeverityViolationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.SystemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import static it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum.OK;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.RawValidationEnum.SEMANTIC_WARNING;

/**
 *	Validation controller.
 */
@Slf4j
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {
	
	
	@Autowired
	private IValidationFacadeSRV validationSRV;
 
	
	@Override
	public ValidationResponseDTO validation(ValidationRequestDTO requestBody, HttpServletRequest request) {

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
		}

		ValidationInfoDTO out = ValidationInfoDTO.builder()
			.result(outcome)
			.message(messages)
			.engineID(p.getKey())
			.transformID(p.getValue()).
			build();

		return new ValidationResponseDTO(getLogTraceInfo(), out);
	}
	 
	
}
