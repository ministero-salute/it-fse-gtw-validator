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
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Slf4j
public class UpdateSingletonSRV implements IUpdateSingletonSRV {

	@Autowired
	private ISchemaRepo schemaRepo;

	@Autowired
	private ISchematronRepo schematronRepo;

	@Override
	public void updateSingletonInstance() {
		updateSchemaSingleton();
		updateSchematronSingleton();
	}

	private void updateSchemaSingleton() {
		Map<String,SchemaValidatorSingleton> mapSchema = SchemaValidatorSingleton.getMapInstance();
		if(mapSchema!=null && !mapSchema.isEmpty()) {
			for(Entry<String, SchemaValidatorSingleton> map : mapSchema.entrySet()) {
				String searchKey = map.getKey();
				SchemaETY father = schemaRepo.findFatherXsd(searchKey);
				if (father == null) {
					log.warn("No schema found on DB... singleton map will be reset");
					mapSchema.remove(searchKey);
				} else {
					log.debug("Father schema found on DB... check update time");
					List<SchemaETY> schemas = schemaRepo.findByExtensionAndLastUpdateDate(map.getKey(), map.getValue().getDataUltimoAggiornamento());

					if(!schemas.isEmpty()) {
						SchemaValidatorSingleton.getInstance(true, father, schemaRepo, schemas.get(0).getLastUpdateDate());
					}
				}
			}
		}
	}

	private void updateSchematronSingleton() {
		Map<String,SchematronValidatorSingleton> mapSchema = SchematronValidatorSingleton.getMapInstance();
		if (mapSchema != null && !mapSchema.isEmpty()) {
			for(Entry<String, SchematronValidatorSingleton> map : mapSchema.entrySet()) {
				String root = map.getValue().getTemplateIdRoot();
				String system = map.getValue().getSystem();
				SchematronETY schematron = schematronRepo.findByRootAndSystem(root, system);
				if (schematron == null) {
					log.warn("No schematron found on DB... singleton map will be reset");
					mapSchema.remove(SchematronValidatorSingleton.identifier(root, system));
				} else {
					boolean isDifferent = checkDataUltimoAggiornamento(map.getValue().getDataUltimoAggiornamento(), schematron.getLastUpdateDate());

					if(Boolean.TRUE.equals(isDifferent)) {
						SchematronValidatorSingleton.getInstance(true,schematron);
					}
				}
			}
		}
	}

	boolean checkDataUltimoAggiornamento(Date dataInstanza, Date dataETY) {
		if (dataInstanza != null && dataETY != null) {
			return !dataInstanza.equals(dataETY);
		}
		return false;
	}

}
