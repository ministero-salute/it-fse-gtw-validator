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

import it.finanze.sanita.fse2.ms.gtw.validator.dto.ExtractedInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronFailedAssertionDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronValidationResultDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CDAHelper {

	private CDAHelper(){}

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
	
	public static ExtractedInfoDTO extractInfo(final String cda) {
		ExtractedInfoDTO out = null;
		try {
			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			
			//Schematron = root
			String templateIdSchematron = docT.select("templateid").get(0).attr("root");
			//Schemaversion = extension 
			String schemaVersion = docT.select("typeid").get(0).attr("extension");
			out = new ExtractedInfoDTO(templateIdSchematron, schemaVersion);
		} catch(Exception ex) {
			log.error("Error while extracting info for schematron ", ex);
			throw new BusinessException("Error while extracting info for schematron ", ex);
		}
		return out;
	}
	 
	public static SchematronValidationResultDTO validateXMLViaSchematronFull(ISchematronResource aResSCH , final byte[] xml) throws Exception{
		List<SchematronFailedAssertionDTO> failedAssertions = new ArrayList<>();
		boolean validST = aResSCH.isValidSchematron();
		boolean validXML = true;
		if (validST) {
			Long start = new Date().getTime();
			
			SchematronOutputType type = null;
			try (ByteArrayInputStream iStream = new ByteArrayInputStream(xml)){
				type = aResSCH.applySchematronValidationToSVRL(new StreamSource(iStream));
			}
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
		
		return new SchematronValidationResultDTO(validST,validXML,failedAssertions);
	}
	 
}
