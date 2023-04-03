/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Paths;
import java.util.UUID;

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
	 * Transformation from Json to Object.
	 * 
	 * @param <T>	Generic type of return
	 * @param json	json
	 * @param cls	Object class to return
	 * @return		object
	 */
	public static <T> T fromJSON(final String json, final Class<T> cls) throws JsonProcessingException {
		return new ObjectMapper().readValue(json, cls);
	}

	/**
	 * Transformation from Object to Json.
	 * 
	 * @param obj	object to transform
	 * @return		json
	 */

	public static String toJSON(final Object obj) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(obj);
	}
 
}
