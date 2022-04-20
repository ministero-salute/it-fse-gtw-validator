package it.sanita.fse.validator.singleton;

import java.io.ByteArrayInputStream;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import it.sanita.fse.validator.repository.entity.SchematronETY;
import it.sanita.fse.validator.repository.mongo.ISchematronRepo;
import it.sanita.fse.validator.xmlresolver.ClasspathResourceURIResolver;


public final class SchematronValidatorSingleton {

	private static SchematronValidatorSingleton instance;

	private SchematronResourceXSLT schematronResource;

	private String cdaCode;
	
	private String cdaCodeSystem;
	
	private String templateIdExtension;
	
	public static SchematronValidatorSingleton getInstance(final SchematronETY inSchematronETY,final ISchematronRepo schematronRepo) {
		if(instance==null || !instance.getCdaCode().equals(inSchematronETY.getCdaCode()) || 
				!instance.getCdaCodeSystem().equals(inSchematronETY.getCdaCodeSystem()) || 
				!instance.getTemplateIdExtension().equals(inSchematronETY.getTemplateIdExtension())) {
			
			IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(inSchematronETY.getContentSchematron().getData()));
			SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
			schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
			instance = new SchematronValidatorSingleton(inSchematronETY.getCdaCode(),
					inSchematronETY.getCdaCodeSystem(),inSchematronETY.getTemplateIdExtension(), schematronResourceXslt);
		}  
		return instance;
	}

	private SchematronValidatorSingleton(final String inCdaCode, final String inCdaCodeSystem ,
			final String inTemplateIdExtension , final SchematronResourceXSLT inSchematronResource) {
		cdaCode = inCdaCode;
		cdaCodeSystem = inCdaCodeSystem;
		templateIdExtension = inTemplateIdExtension;
		schematronResource = inSchematronResource;
	}


	public ISchematronResource getSchematronResource() {
		return schematronResource;
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
