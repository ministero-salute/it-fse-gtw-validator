/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.CollectionUtils;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SchematronValidatorSingleton {

	private static ConcurrentHashMap<String,SchematronValidatorSingleton> mapInstance;

	private static SchematronValidatorSingleton instance;

	private SchematronResourceSCH schematronResourceSCH;

	private String templateIdRoot;

	private String version;

	private Date dataUltimoAggiornamento;

	public static SchematronValidatorSingleton getInstance(final boolean forceUpdate, final SchematronETY inSchematronETY) {
		if (mapInstance != null && !mapInstance.isEmpty()) {
			instance = mapInstance.get(inSchematronETY.getTemplateIdRoot());
		} else {
			mapInstance = new ConcurrentHashMap<>();
		}

		boolean getInstanceCondition = instance == null  || CollectionUtils.isEmpty(mapInstance) || Boolean.TRUE.equals(forceUpdate);

		synchronized(SchematronValidatorSingleton.class) {
			if (getInstanceCondition) {
				try (ByteArrayInputStream schematronBytes = new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData());) {

					IReadableResource readableResource = new ReadableResourceInputStream(StringUtility.generateUUID(), schematronBytes);
					SchematronResourceSCH schematronResourceSCH = new SchematronResourceSCH(readableResource);
					instance = new SchematronValidatorSingleton(inSchematronETY.getTemplateIdRoot(), 
							inSchematronETY.getVersion(), inSchematronETY.getLastUpdateDate(), schematronResourceSCH);

					mapInstance.put(instance.getTemplateIdRoot(), instance);
				} catch (Exception e) {
					log.error("Error encountered while updating schematron singleton", e);
					throw new BusinessException("Error encountered while updating schematron singleton", e);
				}

			}
		}

		return instance;
	}

	private SchematronValidatorSingleton(final String inTemplateIdRoot,final String inVersion,
			final Date inDataUltimoAggiornamento,final SchematronResourceSCH inSchematronResource) {
		templateIdRoot = inTemplateIdRoot;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
		schematronResourceSCH = inSchematronResource;
		version = inVersion;
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

	public static ConcurrentHashMap<String,SchematronValidatorSingleton> getMapInstance() {
		return mapInstance;
	}
}
