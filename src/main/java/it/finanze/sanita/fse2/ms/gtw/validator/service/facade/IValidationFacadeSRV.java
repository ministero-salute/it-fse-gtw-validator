package it.finanze.sanita.fse2.ms.gtw.validator.service.facade;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;

/**
 * Interface of Validation Service.
 * 
 * @author Simone Lungarella
 */
public interface IValidationFacadeSRV {

    /**
     * Execute validation on the given terminology.
     * 
     * @param cda CDA to validate.
     * @return The result of the validation.
     */
    boolean validateVocabularies(String cda);
    
    /**
     * Execute validation on the given terminology and version.
     * 
     * @param cda 	  CDA to validate.
     * @param version Schema version.
     * @return The result of the validation.
     */
    CDAValidationDTO validateSyntactic(String cda, String version);
    
    /**
     * Execute validation on the given terminology and version.
     * 
     * @param cda 	  CDA to validate.
     * @param version Schematron version.
     * @return The result of the validation.
     */
    SchematronValidationResultDTO validateSemantic(String cda,SchematronETY schematronETY);
    
    SchematronETY findSchematron(SchematronInfoDTO schematronInfoDTO) ;
}
