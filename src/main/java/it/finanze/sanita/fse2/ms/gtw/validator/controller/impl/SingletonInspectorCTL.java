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

import it.finanze.sanita.fse2.ms.gtw.validator.controller.ISingletonInspectorCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchemaSingletonInfo;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronSingletonInfo;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SingletonInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.SingletonInspectorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *	Singleton Inspector controller.
 */
@RestController
public class SingletonInspectorCTL extends AbstractCTL implements ISingletonInspectorCTL {

	@Override
    public SingletonInspectorResponseDTO getSingletons(HttpServletRequest request) {

        Map<String, SchemaValidatorSingleton> schemaInstance = SchemaValidatorSingleton.getMapInstance();
        Map<String, SchematronValidatorSingleton> schematronInstance = SchematronValidatorSingleton.getMapInstance();

        List<SchematronSingletonInfo> schematrons = new ArrayList<>();
        List<SchemaSingletonInfo> schemas = new ArrayList<>();

        if (schemaInstance != null) {
            for (Map.Entry<String, SchemaValidatorSingleton> entry : schemaInstance.entrySet()) {
                schemas.add(SchemaSingletonInfo.builder()
                        .typeIdExtension(entry.getValue().getTypeIdExtension())
                        .dataUltimoAggiornamento(entry.getValue().getDataUltimoAggiornamento())
                        .build());
            }
        }

        if (schematronInstance != null) {
            for (Map.Entry<String, SchematronValidatorSingleton> entry : schematronInstance.entrySet()) {
                schematrons.add(SchematronSingletonInfo.builder()
                		.templateIdRoot(entry.getValue().getTemplateIdRoot())
                        .version(entry.getValue().getVersion())
                        .system(entry.getValue().getSystem())
                        .dataUltimoAggiornamento(entry.getValue().getDataUltimoAggiornamento())
                        .build());
            }
        }

        SingletonInfoDTO out = SingletonInfoDTO.builder().schemas(schemas).schematrons(schematrons).build();

        return new SingletonInspectorResponseDTO(getLogTraceInfo(), out);

    }
 
}
