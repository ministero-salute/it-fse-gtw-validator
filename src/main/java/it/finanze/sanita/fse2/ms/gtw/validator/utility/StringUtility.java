package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import java.nio.file.Paths;
import java.util.UUID;

import com.google.gson.Gson;

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
		return Paths.get(completePath).getFileName().toString();
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Transformation from Object to Json.
	 * 
	 * @param obj	object to transform
	 * @return		json
	 */
	public static String toJSON(final Object obj) {
		return new Gson().toJson(obj);
	}

	public static String sanitizeCDA(String cda) {
		return cda.replaceAll("<!DOCTYPE[^<>]*(?:<!ENTITY[^<>]*>[^<>]*)+>", "");
	}
}
