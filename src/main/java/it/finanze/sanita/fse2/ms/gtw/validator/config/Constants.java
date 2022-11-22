/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.config;

/**
 * 
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.finanze.sanita.fse2.ms.gtw.validator";


		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.finanze.sanita.fse2.ms.gtw.validator.config.mongo";


		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}

	public static final class Collections {

		public static final String DICTIONARY = "dictionary";

		public static final String SCHEMA = "schema";

		public static final String SCHEMATRON = "schematron";

		public static final String TERMINOLOGY = "terminology";

		public static final String XSL_TRANSFORM = "xsl_transform";

		public static final String TRANSFORM = "transform";
  
		public static final String CODE_SYSTEM= "code_system";


		private Collections() {

		}
	}


	public static final class Profile {
		public static final String TEST = "test";

		public static final String TEST_PREFIX = "test_";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class Logs {
		public static final String ERR_NOT_ALL_CODES_FOUND = "Not all codes for system {} are present on Mongo";
		public static final String ERR_VOCABULARY_VALIDATION = "Error while executing validation on vocabularies for system %s";

		private Logs() {}
	}

	public static final class App {
		public static final String SYSTEM_KEY = "system";
		public static final String CODE_KEY = "code";
		public static final String CODE_SYSTEM_KEY = "codeSystem";
		public static final String CODE_SYSTEM_VERSION_KEY = "codeSystemVersion";
		public static final String WHITELIST_KEY = "whitelist";
		public static final String VALUE_KEY = "value";

		private App() {}
	}

	/**
	 *	Constants.
	 */
	private Constants() {

	}
}
