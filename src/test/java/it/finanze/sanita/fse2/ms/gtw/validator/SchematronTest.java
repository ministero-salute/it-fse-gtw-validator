package it.finanze.sanita.fse2.ms.gtw.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.io.resource.inmemory.ReadableResourceInputStream;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import it.finanze.sanita.fse2.ms.gtw.validator.cda.CDAHelper;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.FileUtility;
import it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver.ClasspathResourceURIResolver;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class SchematronTest {

	@Autowired
	private ISchematronRepo schematronRepo;
	
	@Test
	@DisplayName("CDA OK")
	void cdaOK() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematron" + File.separator + "schematronFSE.sch.xsl");
		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
		
		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ok" + File.separator + "Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
		assertEquals(0, resultDTO.getFailedAssertions().size());
		assertEquals(true, resultDTO.getValidSchematron());
		assertEquals(true, resultDTO.getValidXML());
	}

	@Test
	@DisplayName("CDA KO")
	void cdaKO() throws Exception {
		byte[] schematron = FileUtility.getFileFromInternalResources("Files" + File.separator + "schematron" + File.separator + "schematronFSE.sch.xsl");
		IReadableResource readableResource = new ReadableResourceInputStream(new ByteArrayInputStream(schematron));
		SchematronResourceXSLT schematronResourceXslt = new SchematronResourceXSLT(readableResource);
		schematronResourceXslt.setURIResolver(new ClasspathResourceURIResolver(schematronRepo));
		
		byte[] cdaOK = FileUtility.getFileFromInternalResources("Files" + File.separator + "cda_ko" + File.separator + "CDA2_Referto Medicina di Laboratorio Errore 6.xml");
		SchematronValidationResultDTO resultDTO = CDAHelper.validateXMLViaXSLTSchematronFull(schematronResourceXslt, cdaOK);
		assertEquals(true, resultDTO.getValidSchematron());
		assertEquals(false, resultDTO.getValidXML());
	}

	 
}
