package it.finanze.sanita.fse2.ms.gtw.validator.enums;

public enum OperationLogEnum implements ILogEnum {

	VAL_CDA2("VAL-CDA2", "Validazione CDA2"),
	UPD_SCHEMA("UPD_SCHEMA", "Aggiornamento Schema"),
	UPD_SCHEMATRON("UPD_SCHEMATRON", "Aggiornamento Schematron"),
	REDIS("REDIS", "Salvataggio/Query su Redis"); 

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private OperationLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

