package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public final class StringUtility {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private StringUtility() {
		// Constructor intentionally empty.
	}

	/**
	 * Returns {@code true} if the String passed as parameter is null or empty.
	 * 
	 * @param str	String to validate.
	 * @return		{@code true} if the String passed as parameter is null or empty.
	 */
	public static boolean isNullOrEmpty(final String str) {
		return str == null || str.isEmpty();
	}
	
	/**
	 * Get filename from complete path.
	 * 
	 * @param completePath	path
	 * @return				filename
	 */
	public static String getFilename(final String completePath) {
		String output;
		try {
			Path path = Paths.get(completePath);
			output = path.getFileName().toString(); 
		} catch(Exception ex) {
			log.error("Error to get filename from complete path " , ex);
			throw new BusinessException("Error to get filename from complete path " , ex);
		}
		return output;
	}
 }
