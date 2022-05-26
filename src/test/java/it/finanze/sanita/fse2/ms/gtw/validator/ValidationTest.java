package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.CDAValidationDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.CDAValidationStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
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

	@BeforeEach
	void setup() {
		clearConfigurationItems();
		insertSchema();
		insertSchematron();
	}

	@Test
	void shouldReturnValidWhenCDAIsValid() {

		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
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

		final String cda = new String(FileUtility.getFileFromInternalResources("Files" + File.separator + "cda.xml"), StandardCharsets.UTF_8);
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
}
