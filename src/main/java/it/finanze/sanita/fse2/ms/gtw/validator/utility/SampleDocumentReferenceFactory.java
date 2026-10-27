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
package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.DocumentReferenceDTO;

/**
 * Supplies synthetic publication metadata for the disposable validation bundle.
 * This triggers the same DocumentReference enrichment as publication, without
 * requiring publication metadata in the validation API or persisting sample data.
 * It does not validate the real metadata that will be supplied at publication.
 */
public final class SampleDocumentReferenceFactory {

    private SampleDocumentReferenceFactory() {}

    public static DocumentReferenceDTO create() {
        DocumentReferenceDTO metadata = new DocumentReferenceDTO();
        // Attachment values are placeholders, not the hash/size of the input CDA or PDF.
        metadata.setSize(1);
        metadata.setHash("0000000000000000000000000000000000000000");
        metadata.setFacilityTypeCode("Ospedale");
        // Exercise security-label enrichment as well as context.event generation.
        metadata.setEventCode(List.of("P99"));
        // Publication forwards PracticeSettingCodeEnum.getDescription(), not its code.
        metadata.setPracticeSettingCode("Medicina Generale");
        metadata.setTipoDocumentoLivAlto("REF");
        metadata.setRepositoryUniqueID("urn:oid:2.16.840.1.113883.2.9.2.0.0");
        metadata.setServiceStartTime("20240101000000");
        metadata.setServiceStopTime("20240101010000");
        metadata.setIdentificativoDoc("urn:oid:2.16.840.1.113883.2.9.2.0.0^validation-sample");
        metadata.setAdministrativeRequestEnum(List.of("SSN"));
        return metadata;
    }

}
