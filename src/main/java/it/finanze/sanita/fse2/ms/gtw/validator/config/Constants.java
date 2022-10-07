package it.finanze.sanita.fse2.ms.gtw.validator.config;

/**
 * 
 * @author vincenzoingenito
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
		public static final String BASE = "it.sanita.fse.validator";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.sanita.fse.validator.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.sanita.fse.validator.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.sanita.fse.validator.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.sanita.fse.validator.config.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.sanita.fse.validator.repository";

		public static final class Collections {

			public static final String DICTIONARY = "dictionary";

			public static final String GTW_DB_RULES = "gtw-db-rules";

			public static final String SCHEMA = "schema";

			public static final String SCHEMATRON = "schematron";

			public static final String TERMINOLOGY = "terminology";

			public static final String XSL_TRANSFORM = "xsl_transform";

			private Collections() {

			}
		}
		
		private ComponentScan() {
			//This method is intentionally left blank.
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
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
