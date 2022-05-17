package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;


public final class SchematronValidatorSingleton {

	private static Map<String,SchematronValidatorSingleton> mapInstance;
	
	private static volatile SchematronValidatorSingleton instance;

	private SchematronResourceXSLT schematronResource;

	private String templateIdRoot;
	
	private String templateIdExtension;
	
	private Date dataUltimoAggiornamento;
	
	public static SchematronValidatorSingleton getInstance(final boolean forceUpdate,final SchematronETY inSchematronETY,final ISchematronRepo schematronRepo) {
		if(mapInstance!=null) {
			instance = mapInstance.get(inSchematronETY.getTemplateIdRoot());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance==null || Boolean.TRUE.equals(forceUpdate);

		if(getInstanceCondition) {
			synchronized(SchematronValidatorSingleton.class) {
				if (getInstanceCondition) {
					IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData()));
					SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
					schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
					instance = new SchematronValidatorSingleton(inSchematronETY.getTemplateIdRoot(),inSchematronETY.getTemplateIdExtension(),
							inSchematronETY.getDataUltimoAggiornamento(), schematronResourceXslt);
					
					mapInstance.put(instance.getTemplateIdRoot(), instance);
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
