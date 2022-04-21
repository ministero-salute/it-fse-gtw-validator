package it.finanze.sanita.fse2.ms.gtw.validator.cda;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronFailedAssertionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CDAHelper {

	public static Map<String, List<String>> extractTerminology(String cda) {
        org.jsoup.nodes.Document docT = Jsoup.parse(cda);
        Elements terms = docT.select("[codeSystem]"); 
        
        Map<String, List<String>> terminology = new HashMap<>();
        
        for (Element t:terms) {
        	String system = t.attr("codeSystem");
        	List<String> codes = terminology.get(system);
        	if (codes == null) {
        		codes = new ArrayList<>();
        	}
        	String code = t.attr("code");
        	codes.add(code);
        	terminology.put(system, codes);
        }
        return terminology;
	}
	
	public static SchematronInfoDTO extractSchematronInfo(final String cda) {
		SchematronInfoDTO out = null;
		try {
			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			Element systemIdentifier = docT.select("code").get(0);
			String code = systemIdentifier.attr("code");
			String codesystem = systemIdentifier.attr("codesystem");
			String templateIdExtension = docT.select("templateid").get(0).attr("extension");
			
			out = new SchematronInfoDTO(code, codesystem , templateIdExtension);
		} catch(Exception ex) {
			log.error("Error while extracting info for schematron ", ex);
			throw new BusinessException("Error while extracting info for schematron ", ex);
		}
		return out;
	}
	 
	public static SchematronValidationResultDTO validateXMLViaXSLTSchematronFull(ISchematronResource aResSCH , final byte[] xml) throws Exception{
		boolean validST = aResSCH.isValidSchematron();
		boolean validXML = true;
		List<SchematronFailedAssertionDTO> failedAssertions = new ArrayList<>();
		if (validST) {
			Long start = new Date().getTime();
			SchematronOutputType type = aResSCH.applySchematronValidationToSVRL(new StreamSource(new ByteArrayInputStream(xml)));
			List<Object> failedAsserts = type.getActivePatternAndFiredRuleAndFailedAssert();
			Long delta = new Date().getTime() - start;
			log.info("TIME : " + delta);        
			for (Object object : failedAsserts) {
				if (object instanceof FailedAssert) {
					validXML = false;
					FailedAssert failedAssert = (FailedAssert) object;
					SchematronFailedAssertionDTO failedAssertion = SchematronFailedAssertionDTO.builder().location(failedAssert.getLocation()).test(failedAssert.getTest()).text(failedAssert.getText().getContent().toString()).build();
					failedAssertions.add(failedAssertion);
				}
			}
		}
		return SchematronValidationResultDTO.builder().validSchematron(validST).validXML(validXML).failedAssertions(failedAssertions).build();
	}

//	
//	public static void main(String[] args) throws Exception {
//		byte[] xml = FileUtility.getFileFromInternalResources("Esempio CDA2_Referto Medicina di Laboratorio v6_OK.xml");
//		byte[] schematron = FileUtility.getFileFromInternalResources("schematronFSE.sch.xsl");
//		validateXMLViaXSLTSchematronFull(schematron, xml);
//	}
	
	public static SchematronValidationResultDTO validateXMLViaXSLTSchematronFull(SchematronResourceXSLT schematronResourceXslt , final byte[] xml) throws Exception{
 		boolean validST = schematronResourceXslt.isValidSchematron();
		boolean validXML = true;
		List<SchematronFailedAssertionDTO> failedAssertions = new ArrayList<>();
		if (validST) {
			Long start = new Date().getTime();
			SchematronOutputType type = schematronResourceXslt.applySchematronValidationToSVRL(new StreamSource(new ByteArrayInputStream(xml)));
			List<Object> failedAsserts = type.getActivePatternAndFiredRuleAndFailedAssert();
			Long delta = new Date().getTime() - start;
			log.info("TIME : " + delta);        
			for (Object object : failedAsserts) {
				if (object instanceof FailedAssert) {
					validXML = false;
					FailedAssert failedAssert = (FailedAssert) object;
					SchematronFailedAssertionDTO failedAssertion = SchematronFailedAssertionDTO.builder().location(failedAssert.getLocation()).test(failedAssert.getTest()).text(failedAssert.getText().getContent().toString()).build();
					failedAssertions.add(failedAssertion);
				}
			}
		}
		return SchematronValidationResultDTO.builder().validSchematron(validST).validXML(validXML).failedAssertions(failedAssertions).build();
	}
}
