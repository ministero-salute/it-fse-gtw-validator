package it.sanita.fse.validator.singleton;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;

import it.sanita.fse.validator.repository.entity.SchematronETY;


public final class SchematronValidatorSingleton {

	private static SchematronValidatorSingleton instance;
	
	private String version;
	
	private static ISchematronResource schematronResource;
	
	public static SchematronValidatorSingleton getInstance(final String inVersion ,final SchematronETY inSchematronETY) {
		if(instance==null) {
			schematronResource = SchematronResourcePure.fromByteArray(inSchematronETY.getContentSchematron().getData());
			instance = new SchematronValidatorSingleton(inVersion,schematronResource);
		} else if(!instance.getVersion().equals(inVersion)) {
			schematronResource = SchematronResourcePure.fromByteArray(inSchematronETY.getContentSchematron().getData());
			instance = new SchematronValidatorSingleton(inVersion,schematronResource);
		}
		return instance;
	}
	
	private SchematronValidatorSingleton(final String inVersion, final ISchematronResource inSchematronResource) {
		version = inVersion;
		schematronResource = inSchematronResource;
	}
	
	public String getVersion() {
		return version;
	}
	
	public ISchematronResource getSchematronResource() {
		return schematronResource;
	}
}

//
//public static SchematronValidationResultDTO validateXMLViaXSLTSchematronFull(byte[] buf/*final String schematronInternalPath*/, final byte[] xml) throws Exception{

//	boolean validST = aResSCH.isValidSchematron();
//	boolean validXML = true;
//	List<SchematronFailedAssertionDTO> failedAssertions = new ArrayList<>();
//	if (validST) {
//
//		Long start = new Date().getTime();
//
//
//		SchematronOutputType type = aResSCH.applySchematronValidationToSVRL(new StreamSource(new ByteArrayInputStream(xml)));
//		List<Object> failedAsserts = type.getActivePatternAndFiredRuleAndFailedAssert();
//
//		Long delta = new Date().getTime() - start;
//		System.out.println("TIME" + delta);        
//
//		for (Object object : failedAsserts) {
//			if (object instanceof FailedAssert) {
//				validXML = false;
//				FailedAssert failedAssert = (FailedAssert) object;
//				SchematronFailedAssertionDTO failedAssertion = SchematronFailedAssertionDTO.builder().location(failedAssert.getLocation()).test(failedAssert.getTest()).text(failedAssert.getText().getContent().toString()).build();
//				failedAssertions.add(failedAssertion);
//			}
//		}
//	}
//	return SchematronValidationResultDTO.builder().validSchematron(validST).validXML(validXML).failedAssertions(failedAssertions).build();
//}
