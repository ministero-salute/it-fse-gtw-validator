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

	private String cdaCode;

	private String cdaCodeSystem;

	private String templateIdExtension;
	
	private Date dataUltimoAggiornamento;

	public static SchematronValidatorSingleton getInstance(final SchematronETY inSchematronETY,final ISchematronRepo schematronRepo) {
		if(mapInstance!=null) {
			instance = mapInstance.get(inSchematronETY.getTemplateIdExtension());
		} else {
			mapInstance = new HashMap<>();
		}
		
		boolean getInstanceCondition = instance==null || !instance.getCdaCode().equals(inSchematronETY.getCdaCode()) || 
				!instance.getCdaCodeSystem().equals(inSchematronETY.getCdaCodeSystem()) || 
				!instance.getTemplateIdExtension().equals(inSchematronETY.getTemplateIdExtension()) || 
				!instance.getDataUltimoAggiornamento().equals(inSchematronETY.getDataUltimoAggiornamento());

		if(getInstanceCondition) {
			synchronized(SchematronValidatorSingleton.class) {
				if (getInstanceCondition) {
					IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData()));
					SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
					schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
					instance = new SchematronValidatorSingleton(inSchematronETY.getCdaCode(),
							inSchematronETY.getCdaCodeSystem(),inSchematronETY.getTemplateIdExtension(),
							inSchematronETY.getDataUltimoAggiornamento(), schematronResourceXslt);
					
					mapInstance.put(instance.getTemplateIdExtension(), instance);
				}
			}
		}  
		return instance;
	}

	private SchematronValidatorSingleton(final String inCdaCode, final String inCdaCodeSystem ,
			final String inTemplateIdExtension ,final Date inDataUltimoAggiornamento, final SchematronResourceXSLT inSchematronResource) {
		cdaCode = inCdaCode;
		cdaCodeSystem = inCdaCodeSystem;
		templateIdExtension = inTemplateIdExtension;
		dataUltimoAggiornamento = inDataUltimoAggiornamento;
		schematronResource = inSchematronResource;
	}


	public ISchematronResource getSchematronResource() {
		return schematronResource;
	}

	private Date getDataUltimoAggiornamento() {
		return dataUltimoAggiornamento;
	}
	
	private String getCdaCode() {
		return cdaCode;
	}

	private String getCdaCodeSystem() {
		return cdaCodeSystem;
	}

	private String getTemplateIdExtension() {
		return templateIdExtension;
	}
}
