
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
package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class SchematronValidatorSingleton {

	private static ConcurrentHashMap<String,SchematronValidatorSingleton> mapInstance;

	private static SchematronValidatorSingleton instance;

	private SchematronResourceSCH schematronResourceSCH;

	private String templateIdRoot;

	private String version;

	private String system;

	private Date dataUltimoAggiornamento;

	public static SchematronValidatorSingleton getInstance(final boolean forceUpdate, final SchematronETY inSchematronETY) {
		String id = identifier(inSchematronETY);
		if (mapInstance != null && !mapInstance.isEmpty()) {
			instance = mapInstance.get(id);
		} else {
			mapInstance = new ConcurrentHashMap<>();
		}

		boolean getInstanceCondition = instance == null  || CollectionUtils.isEmpty(mapInstance) || Boolean.TRUE.equals(forceUpdate);

		synchronized(SchematronValidatorSingleton.class) {
			if (getInstanceCondition) {
				try (ByteArrayInputStream schematronBytes = new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData());) {

					IReadableResource readableResource = new ReadableResourceInputStream(StringUtility.generateUUID(), schematronBytes);
					SchematronResourceSCH schematronResourceSCH = new SchematronResourceSCH(readableResource);
					instance = new SchematronValidatorSingleton(
						inSchematronETY.getTemplateIdRoot(),
						inSchematronETY.getVersion(), inSchematronETY.getSystem(),
						inSchematronETY.getLastUpdateDate(),
						schematronResourceSCH
					);

					mapInstance.put(id, instance);
				} catch (Exception e) {
					log.error("Error encountered while updating schematron singleton", e);
					throw new BusinessException("Error encountered while updating schematron singleton", e);
				}

			}
		}

		return instance;
	}

	private SchematronValidatorSingleton(final String inTemplateIdRoot,final String inVersion, final String inSystem,
			final Date inDataUltimoAggiornamento,final SchematronResourceSCH inSchematronResource) {
		templateIdRoot = inTemplateIdRoot;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
		schematronResourceSCH = inSchematronResource;
		system = inSystem;
		version = inVersion;
	}

	public static String identifier(SchematronETY sch) {
		String s = sch.getTemplateIdRoot();
		if(sch.getSystem() != null) s += String.format("|%s", sch.getSystem());
		return s;
	}

	public static String identifier(String root, String system) {
		String s = root;
		if(system != null) s += String.format("|%s", system);
		return s;
	}

	public ISchematronResource getSchematronResource() {
		return schematronResourceSCH;
	}

	public Date getDataUltimoAggiornamento() {
		return dataUltimoAggiornamento;
	}

	public String getTemplateIdRoot() {
		return templateIdRoot;
	}

	public String getVersion() {
		return version;
	}

	public String getSystem() {
		return system;
	}

	public static ConcurrentHashMap<String,SchematronValidatorSingleton> getMapInstance() {
		return mapInstance;
	}
}
