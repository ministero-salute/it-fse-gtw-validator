package it.finanze.sanita.fse2.ms.gtw.validator.xmlresolver;

import java.io.ByteArrayInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.DictionaryETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;

public class ClasspathResourceURIResolver implements URIResolver {

	private IDictionaryRepo dictionaryRepo;
	
	public ClasspathResourceURIResolver(final IDictionaryRepo inDictionaryRepo) {
		if(dictionaryRepo==null) {
			dictionaryRepo = inDictionaryRepo;
		}
	}
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		Source source = null;
		String nameFile = StringUtility.getFilename(href);
		DictionaryETY dictionaryETY = dictionaryRepo.findByFilename(nameFile);
		if(dictionaryETY!=null) {
			source = new StreamSource(new ByteArrayInputStream(dictionaryETY.getContentFile().getData()), nameFile); 
		}
		return source;
	}    
}