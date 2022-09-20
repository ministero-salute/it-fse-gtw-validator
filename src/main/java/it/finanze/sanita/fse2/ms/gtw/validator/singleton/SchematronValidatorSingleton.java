package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SchematronValidatorSingleton {

	private static Map<String,SchematronValidatorSingleton> mapInstance;
	
	private static SchematronValidatorSingleton instance;

	private SchematronResourceXSLT schematronResource;

	private String templateIdRoot;
	
	private String templateIdExtension;
	
	private Date dataUltimoAggiornamento;
	
	public static SchematronValidatorSingleton getInstance(final boolean forceUpdate, final SchematronETY inSchematronETY,String requestURL) {
		if (mapInstance != null && !mapInstance.isEmpty()) {
			instance = mapInstance.get(inSchematronETY.getTemplateIdRoot());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance == null  || CollectionUtils.isEmpty(mapInstance) || Boolean.TRUE.equals(forceUpdate);

		synchronized(SchematronValidatorSingleton.class) {
			if (getInstanceCondition) {
				String schematronAsString = new String(inSchematronETY.getContentSchematron().getData());
				String schematronWithReplacesUrl = schematronAsString.replace("###PLACEHOLDER_URL###", requestURL);
				try (ByteArrayInputStream schematronBytes = new ByteArrayInputStream(schematronWithReplacesUrl.getBytes());) {
					
					IReadableResource readableResource = new ReadableResourceInputStream(StringUtility.generateUUID(), schematronBytes);
					SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
					instance = new SchematronValidatorSingleton(inSchematronETY.getTemplateIdRoot(), 
						inSchematronETY.getTemplateIdExtension(), inSchematronETY.getLastUpdateDate(), schematronResourceXslt);
	
					mapInstance.put(instance.getTemplateIdRoot(), instance);
				} catch (Exception e) {
					log.error("Error encountered while updating schematron singleton", e);
					throw new BusinessException("Error encountered while updating schematron singleton", e);
				}
				
			}
		}

		return instance;
	}

	private SchematronValidatorSingleton(final String inTemplateIdRoot,final String inTemplateIdExtension,
			final Date inDataUltimoAggiornamento,final SchematronResourceXSLT inSchematronResource) {
		templateIdRoot = inTemplateIdRoot;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
		schematronResource = inSchematronResource;
		templateIdExtension = inTemplateIdExtension;
	}


	public ISchematronResource getSchematronResource() {
		return schematronResource;
	}

	public Date getDataUltimoAggiornamento() {
		return dataUltimoAggiornamento;
	}
	
	public String getTemplateIdRoot() {
		return templateIdRoot;
	}
	
	public String getTemplateIdExtension() {
		return templateIdExtension;
	}

	public static Map<String,SchematronValidatorSingleton> getMapInstance() {
		return mapInstance;
	}
}
