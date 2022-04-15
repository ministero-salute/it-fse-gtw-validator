package it.sanita.fse.validator.singleton;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;

import it.sanita.fse.validator.repository.entity.SchematronETY;


public final class SchematronValidatorSingleton {

	private static SchematronValidatorSingleton instance;

	private static ISchematronResource schematronResource;

	private String cdaCode;
	
	private String cdaCodeSystem;
	
	private String templateIdExtension;
	
	public static SchematronValidatorSingleton getInstance(final SchematronETY inSchematronETY) {
		if(instance==null || !instance.getCdaCode().equals(inSchematronETY.getCdaCode()) || 
				!instance.getCdaCodeSystem().equals(inSchematronETY.getCdaCodeSystem()) || 
				!instance.getTemplateIdExtension().equals(inSchematronETY.getTemplateIdExtension())) {
			
			schematronResource = SchematronResourcePure.fromByteArray(inSchematronETY.getContentSchematron().getData());
			instance = new SchematronValidatorSingleton(inSchematronETY.getCdaCode(),
					inSchematronETY.getCdaCodeSystem(),inSchematronETY.getTemplateIdExtension(), schematronResource);
		}  
		return instance;
	}

	private SchematronValidatorSingleton(final String inCdaCode, final String inCdaCodeSystem ,
			final String inTemplateIdExtension , final ISchematronResource inSchematronResource) {
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
