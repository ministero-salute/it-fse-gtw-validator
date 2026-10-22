package it.finanze.sanita.fse2.ms.gtw.validator.client;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.TransformResDTO;

/**
 * Interface of fhir-mapping-engine Client.
 */
public interface IFhirMappingClient {

	/**
	 * Calls convertCDAToBundle executing the given validations on the generated bundle.
	 * 
	 * @param resource		cda with engine and structure-map identifiers
	 * @param validations	validations to be executed
	 * @return				transformation result including validation outcomes
	 */
	TransformResDTO callConvertCdaInBundle(FhirResourceDTO resource, List<String> validations);

}
