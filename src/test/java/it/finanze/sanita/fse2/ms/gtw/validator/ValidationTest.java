package it.finanze.sanita.fse2.ms.gtw.validator;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.VocabularyResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.service.facade.IValidationFacadeSRV;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility.getFileFromInternalResources;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
class ValidationTest extends AbstractTest {

	@Autowired
	IValidationFacadeSRV validationSRV;

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
		assertNotNull(out.getNoRecordFound()); 
	}

	@Test
	void shouldReturnNotValidWhenCDAIsInvalid() {

		final String cda = "<realmCode code=\"1\"/>";
		String version = "1.3";
		
		log.info("Testing with version {}", version);
		CDAValidationDTO firstResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, firstResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getNoRecordFound());
	
		CDAValidationDTO secondResult = validationSRV.validateSyntactic(cda, version);
		assertEquals(CDAValidationStatusEnum.NOT_VALID, secondResult.getStatus(), "The validation should have been completed correctly and result as Invalid");
		assertNull(firstResult.getNoRecordFound());
	}

	@Test
	void shouldThrowBusinessExceptionWhenSchemaisNull() {

		final String cda = new String(getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
		final String version = "3.0.0";
		
		CDAValidationDTO res = validationSRV.validateSyntactic(cda, version);
		assertEquals(String.format("Schema with version %s not found on database.", version), res.getNoRecordFound());
	}

	@Test
	void shouldThrowBusinessExceptionWhenCDAIsNotXML() {

		final String cda = "invalid cda";
		final String version = "1.3";
		
		assertThrows(BusinessException.class, () -> validationSRV.validateSyntactic(cda, version));
	}

	@Test
	void shouldReturnWhenCDASemanticIsInvalid() {
		final String cda = new String(getFileFromInternalResources(
			"Files\\cda_ok\\Esempio CDA_001.xml"
		), StandardCharsets.UTF_8);
		String version = "1.3";

		log.info("Testing with version {}", version);
		SchematronValidationResultDTO res = validationSRV.validateSemantic(cda, CDAHelper.extractInfo(cda));
		assertTrue(res.getValidXML(), "The xml validation should have been completed correctly");
		assertFalse(res.getValidSchematron(), "The schematron validation should be falsy");

		res = validationSRV.validateSemantic(cda, CDAHelper.extractInfo(cda));
		assertTrue(res.getValidXML(), "Repeating xml validation should have been completed correctly");
		assertFalse(res.getValidSchematron(), "Repeating the schematron validation should be falsy");
	}

	@Test
	void shouldReturnWhenCDAVocabularyIsInvalid() {
		final String cda = new String(getFileFromInternalResources(
			"Files\\cda_ok\\Esempio CDA_001.xml"
		), StandardCharsets.UTF_8);
		String version = "1.3";

		log.info("Testing with version {}", version);
		VocabularyResultDTO res = validationSRV.validateVocabularies(cda);
		assertFalse(res.getValid(), "The vocabulary validation should be falsy");

		res = validationSRV.validateVocabularies(cda);
		assertFalse(res.getValid(), "Repeating vocabulary validation should be falsy");
	}
}
