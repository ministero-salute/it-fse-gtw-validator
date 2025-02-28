/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.cda;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.schematron.svrl.jaxb.SuccessfulReport;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.*;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.SystemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

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
        	String code = t.attr(Constants.App.CODE_KEY);
        	codes.add(code);
        	terminology.put(system, codes);
        }
        return terminology;
	}
	
	public static TerminologyExtractionDTO extractAllCodeSystems(String cda) {
		List<CodeDTO> codes = extractAllCodes(cda);
		return new TerminologyExtractionDTO(codes);
	}

	private static List<CodeDTO> extractAllCodes(String cda) {
		 String codeSystemKey = "[" + Constants.App.CODE_SYSTEM_KEY + "]";
		 return Jsoup
		 	.parse(cda)
		 	.select(codeSystemKey)
		 	.stream()
		 	.map(element -> getCode(element))
		 	.filter(Objects::nonNull)
		 	.collect(Collectors.toList());
	}

	private static CodeDTO getCode(Element element) {
		if (element == null) return null;
		String code = element.attr(Constants.App.CODE_KEY);
		String codeSystem = element.attr(Constants.App.CODE_SYSTEM_KEY);
		String codeSystemVersion = element.attr(Constants.App.CODE_SYSTEM_VERSION_KEY);
		String codeSystemName = element.attr(Constants.App.CODE_SYSTEM_NAME);
		String displayName = element.attr(Constants.App.DISPLAY_NAME_KEY);
		CodeDTO codeDTO = new CodeDTO();
		if (!isEmpty(code)) codeDTO.setCode(code);
		if (!isEmpty(codeSystem)) codeDTO.setCodeSystem(codeSystem);
		if (!isEmpty(codeSystemVersion)) codeDTO.setVersion(codeSystemVersion);
		if (!isEmpty(codeSystemName)) codeDTO.setCodeSystemName(codeSystemName);
		if (!isEmpty(displayName)) codeDTO.setDisplayName(displayName);
		return codeDTO;
	}
	
	public static ExtractedInfoDTO extractInfo(final String cda, String system) {
		ExtractedInfoDTO out = null;
		try {
			org.jsoup.nodes.Document docT = Jsoup.parse(cda);
			
			//Schematron = root
			String templateIdSchematron = docT.select("templateid").get(0).attr("root");
			//Schemaversion = extension 
			String schemaVersion = docT.select("typeid").get(0).attr("extension");
			out = new ExtractedInfoDTO(templateIdSchematron, schemaVersion, SystemTypeEnum.of(system));
		} catch(Exception ex) {
			log.error("Error while extracting info for schematron ", ex);
			throw new BusinessException("Error while extracting info for schematron ", ex);
		}
		return out;
	}
	 
	public static SchematronValidationResultDTO validateXMLViaSchematronFull(ISchematronResource aResSCH , final byte[] xml) throws Exception{
		List<SchematronFailedAssertionDTO> assertions = new ArrayList<>();
		boolean validST = aResSCH.isValidSchematron();
		boolean validXML = true;
		if (validST) {
			List<SchematronFailedAssertionDTO> assertFailed = new ArrayList<>();
			List<SchematronFailedAssertionDTO> assertWarning = new ArrayList<>();
			SchematronOutputType type = null;
			try (ByteArrayInputStream iStream = new ByteArrayInputStream(xml)){
				type = aResSCH.applySchematronValidationToSVRL(new StreamSource(iStream));
			}
			List<Object> asserts = type.getActivePatternAndFiredRuleAndFailedAssert();
			
			for (Object object : asserts) {
				if (object instanceof FailedAssert) {
					validXML = false;
					FailedAssert failedAssert = (FailedAssert) object;
					SchematronFailedAssertionDTO failedAssertion = SchematronFailedAssertionDTO.builder().location(failedAssert.getLocation()).test(failedAssert.getTest()).text(failedAssert.getText().getContent().toString()).build();
					assertFailed.add(failedAssertion);
				} else if(object instanceof SuccessfulReport) {
					SuccessfulReport warningAssert = (SuccessfulReport) object;
					SchematronFailedAssertionDTO warningAssertion = SchematronFailedAssertionDTO.builder().location(warningAssert.getLocation()).test(warningAssert.getTest()).text(warningAssert.getText().getContent().toString()).build();
					assertWarning.add(warningAssertion);
				}
			}
			assertions.addAll(assertFailed);
			assertions.addAll(assertWarning);
		}
		
		return new SchematronValidationResultDTO(validST, validXML, null, assertions);
	}
	 
}
