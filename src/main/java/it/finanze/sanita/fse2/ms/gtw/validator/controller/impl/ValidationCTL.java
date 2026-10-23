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
import org.springframework.util.CollectionUtils;
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
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirValidationResponseDTO.ValidationIssueDTO;
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
 * Validation controller.
 */
@Slf4j
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {

    @Autowired
    private IValidationFacadeSRV validationSRV;

    @Autowired
    private IFhirMappingClient fhirMappingClient;

    @Override
    public ValidationResponseDTO validation(ValidationRequestDTO requestBody, HttpServletRequest request,
            List<String> validations, boolean includeRemovalReport) {
        List<String> messages = new ArrayList<>();
        Validation.notNull(requestBody.getCda());

        RawValidationEnum outcome = RawValidationEnum.OK;

        ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(requestBody.getCda(), request.getHeader(SYSTEM_TYPE_HEADER));

        CDAValidationDTO validationResult = validationSRV.validateSyntactic(requestBody.getCda(),
                infoDTO.getTypeIdExtension());
        if (CDAValidationStatusEnum.NOT_VALID.equals(validationResult.getStatus())) {
            if (StringUtility.isNullOrEmpty(validationResult.getMessage())) {
                for (Entry<CDASeverityViolationEnum, List<String>> violations : validationResult.getViolations()
                        .entrySet()) {
                    String severity = violations.getKey().toString();
                    for (String violation : violations.getValue()) {
                        messages.add(severity + ": " + violation);
                    }
                }
            } else {
                messages.add(validationResult.getMessage());
            }
            outcome = RawValidationEnum.SYNTAX_ERROR;
        }

        if (RawValidationEnum.OK.equals(outcome)) {
            SchematronValidationResultDTO semanticValidation = validationSRV.validateSemantic(requestBody.getCda(),
                    infoDTO);
            if (StringUtility.isNullOrEmpty(semanticValidation.getMessage())) {
                if (Boolean.FALSE.equals(semanticValidation.getValidSchematron())) {
                    messages.add("Invalid schematron");
                    outcome = RawValidationEnum.SEMANTIC_ERROR;
                } else if (semanticValidation.getFailedAssertions() != null
                        && !semanticValidation.getFailedAssertions().isEmpty()) {
                    for (SchematronFailedAssertionDTO violation : semanticValidation.getFailedAssertions()) {
                        messages.add(violation.getText());
                    }
                    outcome = RawValidationEnum.SEMANTIC_WARNING;
                    if (Boolean.FALSE.equals(semanticValidation.getValidXML())) {
                        outcome = RawValidationEnum.SEMANTIC_ERROR;
                    }
                }
            } else {
                messages.add(semanticValidation.getMessage());
                outcome = RawValidationEnum.SEMANTIC_ERROR;
            }

            if (infoDTO.getSystem() != SystemTypeEnum.TS) {
                if (RawValidationEnum.OK.equals(outcome) || RawValidationEnum.SEMANTIC_WARNING.equals(outcome)) {
                    VocabularyResultDTO result = validationSRV.validateVocabularies(requestBody.getCda(),
                            requestBody.getWorkflowInstanceId());
                    if (!Boolean.TRUE.equals(result.getValid())) {
                        outcome = RawValidationEnum.VOCABULARY_ERROR;
                        messages = new ArrayList<>();
                        messages.add(result.getMessage());
                    }
                }
            } else {
                log.debug("Skipping vocabulary validation for the TS system");
            }
        }

        Pair<String, String> p = Pair.of("", "");
        if (Arrays.asList(OK, SEMANTIC_WARNING).contains(outcome)) {
            p = validationSRV.getStructureObjectID(infoDTO.getTemplateIdSchematron());

            boolean hasValidations = validations != null && !validations.isEmpty();
            if (hasValidations || includeRemovalReport) {
                outcome = executeFhirValidations(requestBody.getCda(), p, validations, includeRemovalReport, messages,
                        outcome);
            }
        }

        ValidationInfoDTO out = ValidationInfoDTO.builder()
                .result(outcome)
                .message(messages)
                .engineID(p.getKey())
                .transformID(p.getValue()).build();

        return new ValidationResponseDTO(getLogTraceInfo(), out);
    }

    /**
     * Collects normalization and validation diagnostics from the mapping engine.
     * Validation errors take precedence over normalization warnings; otherwise,
     * the previous outcome is preserved when no normalization issues are reported.
     *
     * @param engineAndObject engine ID and structure-map object ID
     * @param validations optional validation types; normalization does not require them
     * @param messages accumulates diagnostics without discarding earlier findings
     */
    private RawValidationEnum executeFhirValidations(String cda, Pair<String, String> engineAndObject,
            List<String> validations, boolean includeRemovalReport, List<String> messages,
            RawValidationEnum currentOutcome) {

        TransformResDTO res = fhirMappingClient.callConvertCdaInBundle(
                new FhirResourceDTO(cda, engineAndObject.getValue(), engineAndObject.getKey()), validations,
                includeRemovalReport);

        if (res == null) {
            log.error("FHIR mapping engine returned an empty response");
            throw new BusinessException(
                    "FHIR mapping engine returned an empty response");
        }
        if (!StringUtility.isNullOrEmpty(res.getErrorMessage())) {
            log.error("FHIR mapping engine reported a transformation failure");
            throw new BusinessException(
                    "FHIR mapping engine reported a transformation failure: " + res.getErrorMessage());
        }

        RawValidationEnum outcome = currentOutcome;

        // Normalization issues also apply to removal-report-only calls.
        if (!CollectionUtils.isEmpty(res.getNormalizationIssues())) {
            logIssueSummary("normalization", res.getNormalizationIssues());
            outcome = RawValidationEnum.PRUNING_WARNING;
            for (ValidationIssueDTO issue : res.getNormalizationIssues()) {
                messages.add(issue.getSeverity() + ": " + issue.getMessage());
            }
        }

        if (!CollectionUtils.isEmpty(validations)) {

            FhirValidationResponseDTO validationResponse = res.getValidationResponse();
            if (validationResponse == null || validationResponse.getResults() == null) {
                log.error("FHIR mapping engine response is missing the requested validation results");
                throw new BusinessException("FHIR mapping engine response is missing the requested validation results");
            }

            if (validationResponse.getResults().stream().anyMatch(r -> !r.isValid())) {
                outcome = RawValidationEnum.FHIR_VALIDATION_ERROR;
            }

            for (FhirValidationResponseDTO.ValidationResultDTO result : validationResponse.getResults()) {
                logIssueSummary(result.getType(), result.getIssues());
                if (!CollectionUtils.isEmpty(result.getIssues())) {
                    for (ValidationIssueDTO issue : result.getIssues()) {
                        messages.add(issue.getSeverity() + ": " + issue.getMessage());
                    }
                }
            }

        }

        return outcome;
    }

    private void logIssueSummary(String stage, List<ValidationIssueDTO> issues) {
        long errors = issues == null ? 0 : issues.stream()
                .filter(issue -> "ERROR".equalsIgnoreCase(issue.getSeverity())
                        || "FATAL".equalsIgnoreCase(issue.getSeverity()))
                .count();
        long warnings = issues == null ? 0 : issues.stream()
                .filter(issue -> "WARNING".equalsIgnoreCase(issue.getSeverity()))
                .count();
        int total = issues == null ? 0 : issues.size();

        // Diagnostics may contain clinical data; log counts rather than their contents.
        if (errors > 0) {
            log.error("FHIR {} reported {} issues (errors: {}, warnings: {})", stage, total, errors, warnings);
        } else if (warnings > 0) {
            log.warn("FHIR {} reported {} issues (errors: {}, warnings: {})", stage, total, errors, warnings);
        } else {
            log.debug("FHIR {} completed with {} issues and no errors or warnings", stage, total);
        }
    }

}
