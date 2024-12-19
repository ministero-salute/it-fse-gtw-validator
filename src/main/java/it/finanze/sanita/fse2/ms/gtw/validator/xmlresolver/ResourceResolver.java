
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
package it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver;

import java.io.ByteArrayInputStream;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.NoRecordFoundException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceResolver  implements LSResourceResolver {
	 
	private ISchemaRepo schemaRepo;

	private String version;
	
	public ResourceResolver(String inVersion, final ISchemaRepo inSchemaRepo) {
		version = inVersion;
		if(schemaRepo == null) {
			schemaRepo = inSchemaRepo;
		}
	}
	 
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		Input output = null;
		try {
			String nameFile = StringUtility.getFilename(systemId);
			SchemaETY schema = schemaRepo.findByNameAndVersion(nameFile, version);
			if (schema == null) {
				throw new NoRecordFoundException(String.format("Schema with name %s not found", nameFile));
			}
			try (ByteArrayInputStream bytes = new ByteArrayInputStream(schema.getContentSchema().getData())) {
				output = new Input(publicId, schema.getNameSchema(), bytes); 
			}
		} catch (NoRecordFoundException e) {
			throw e;
		} catch(Exception ex) {
			log.error("Error while resolve resource" , ex);
			throw new BusinessException("Error while resolve resource" , ex);	
		}
		return output;
	}
}


