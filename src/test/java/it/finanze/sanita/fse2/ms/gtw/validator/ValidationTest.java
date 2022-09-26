package it.finanze.sanita.fse2.ms.gtw.validator;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.BDDMockito.when; 
import static org.mockito.BDDMockito.any; 
import static org.mockito.BDDMockito.given; 
import static org.mockito.BDDMockito.eq; 
import static org.mockito.BDDMockito.anyString; 

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import com.helger.schematron.ISchematronResource;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.SchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class ValidationTest extends AbstractTest {

	@Autowired
	IValidationFacadeSRV validationSRV;
	
	
	@MockBean
	private SchematronRepo schematronRepo; 
	
	@MockBean
	private CDAHelper cdaHelper; 
	

	@BeforeEach
	void setup() {
		clearConfigurationItems();
		insertSchema();
		insertSchematron();
		saveDictionaryFiles();
	}

	@Test
	void shouldReturnValidWhenCDAIsValid() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		String version = "1.3";
		
		log.info("Testing with version {}", version);
		CDAValidationDTO firstResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.VALID, firstResult.getStatus(), "The validation should have been completed correctly");
		
		CDAValidationDTO secondResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(firstResult.getStatus(), secondResult.getStatus(), "Repeating validation should have not changed the result");

		
		log.info("Testing with version {}", version);
		
		CDAValidationDTO out = validationSRV.validateSyntactic(cda, "2.0.0");
		assertNotNull(out.getMessage()); 
	}

	@Test
	void shouldReturnNotValidWhenCDAIsInvalid() {

		final String cda = "<realmCode code=\"1\"/>";
		String version = "1.3";
		
		log.info("Testing with version {}", version);
		CDAValidationDTO firstResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, firstResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getMessage());
	
		CDAValidationDTO secondResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, secondResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getMessage());
	}

	@Test
	void shouldThrowBusinessExceptionWhenSchemaisNull() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		final String version = "3.0.0";
		
		CDAValidationDTO res = validationSRV.validateSyntactic(cda, version);
		assertEquals(String.format("Schema with version %s not found on database.", version), res.getMessage());
	}
 

	@Test
	void shouldReturnWhenCDAVocabularyIsInvalid() {
		final String cda = new String(getFileFromInternalResources(
			"Files/cda_ok/Esempio_CDA_001.xml"
		), StandardCharsets.UTF_8);
		String version = "1.3";

		log.info("Testing with version {}", version);
		VocabularyResultDTO res = validationSRV.validateVocabularies(cda);
		assertFalse(res.getValid(), "The vocabulary validation should be falsy");

		res = validationSRV.validateVocabularies(cda);
		assertFalse(res.getValid(), "Repeating vocabulary validation should be falsy");
	}
	
	@Test
	void validateSemanticTest() throws Exception {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "schematronLDO"
				+ File.separator + "OK" + File.separator + "CDA2_Lettera_Dimissione_Ospedaliera_v2.2.xml"), StandardCharsets.UTF_8);
		String version = "1.3";
		
		SchematronETY ety = new SchematronETY(); 
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematronLDO" + File.separator + "sch" + File.separator +"schematronFSE_LDO_V3.5.sch");

		ety.setNameSchematron("Lettera di dimissione ospedaliera"); 
		ety.setContentSchematron(new Binary(BsonBinarySubType.BINARY, schematron));
		ety.setTemplateIdRoot("2.16.840.1.113883.2.9.10.1.5"); 
		ety.setTemplateIdExtension("1.2"); 
		
		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(cda); 
	
		
		 when(schematronRepo.findByTemplateIdRoot(anyString()))
			.thenReturn(ety); 
		
		/* given(CDAHelper.validateXMLViaSchematronFull(any(), any()))
			.willReturn(null); */ 
		
		log.info("Testing with version {}", version);
		//SchematronValidationResultDTO firstResult = validationSRV.validateSemantic(cda, infoDTO); 
		
		assertDoesNotThrow(() -> validationSRV.validateSemantic(cda, infoDTO)); 
	} 
	
	@Test
	void validateSemanticExceptionTest() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		String version = "1.3";
		
		ExtractedInfoDTO infoDTO = CDAHelper.extractInfo(cda); 
		
		
		log.info("Testing with version {}", version);
		//SchematronValidationResultDTO firstResult = validationSRV.validateSemantic(cda, infoDTO); 
		
		assertThrows(BusinessException.class, () -> validationSRV.validateSemantic(cda, infoDTO)); 
	} 

}
