package it.sanita.fse.validator.dto.response;

public enum RawValidationEnum {

	OK("00", "OK"),
	SYNTAX_ERROR("01", "Errore di sintassi"),
	VOCABULARY_ERROR("02", "Errore dovuto alle terminologie utilizzate"),
	SEMANTIC_ERROR("03", "Errore semantico");

	private String code;
	private String description;

	private RawValidationEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

}