package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;


public final class SchematronValidatorSingleton {

	private static Map<String,SchematronValidatorSingleton> mapInstance;
	
	private static SchematronValidatorSingleton instance;

	private SchematronResourceSCH schematronResource;

	private String templateIdRoot;
	
	private String templateIdExtension;
	
	private Date dataUltimoAggiornamento;
	
	public static SchematronValidatorSingleton getInstance(final boolean forceUpdate,final SchematronETY inSchematronETY,final IDictionaryRepo dictionaryRepo) {
		if (mapInstance != null && !mapInstance.isEmpty()) {
			instance = mapInstance.get(inSchematronETY.getTemplateIdRoot());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance == null || Boolean.TRUE.equals(forceUpdate);

		synchronized(SchematronValidatorSingleton.class) {
			if (getInstanceCondition) {
				IReadableResource readableResource = new ReadableResourceInputStream(StringUtility.generateUUID() ,
						new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData()));
				SchematronResourceSCH schematronResourceXslt = new SchematronResourceSCH(readableResource);
				schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(dictionaryRepo));
				instance = new SchematronValidatorSingleton(inSchematronETY.getTemplateIdRoot(),inSchematronETY.getTemplateIdExtension(),
						inSchematronETY.getLastUpdateDate(), schematronResourceXslt);

				mapInstance.put(instance.getTemplateIdRoot(), instance);
			}
		}

		return instance;
	}

	private SchematronValidatorSingleton(final String inTemplateIdRoot,final String inTemplateIdExtension,
			final Date inDataUltimoAggiornamento,final SchematronResourceSCH inSchematronResource) {
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
