package it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver;

import java.io.ByteArrayInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;

public class ClasspathResourceURIResolver implements URIResolver {

	private ISchematronRepo schematronRepo;
	
	public ClasspathResourceURIResolver(final ISchematronRepo inSchematronRepo) {
		if(schematronRepo==null) {
			schematronRepo = inSchematronRepo;
		}
	}
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		Source source = null;
		String nameFile = StringUtility.getFilename(href);
		SchematronETY schematronETY = schematronRepo.findByName(nameFile);
		if(schematronETY!=null) {
			source = new StreamSource(new ByteArrayInputStream(schematronETY.getContentSchematron().getData()), nameFile); 
		}
		return source;
	}    
}