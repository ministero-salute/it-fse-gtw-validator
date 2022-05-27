package it.finanze.sanita.fse2.ms.gtw.validator.enums;

public enum ErrorLogEnum implements ILogEnum {

	KO_VAL("KO-VAL", "Errore nella validazione del CDA"),
	KO_UPD_SCHEMA("KO-UPD-SCHEMA", "Errore nell'aggiornamento dello Schema"),
	KO_UPD_SCHEMATRON("KO-UPD-SCHEMATRON", "Errore nell'aggiornamento dello Schematron"),
	KO_INVALID_DATA("KO-INV-DATA", "Errore: dati di input non validi"),
	KO_MONGO_DB("KO-MONGO-DB", "Errore nella chiamata a MongoDB"),
	KO_MONGO_DB_NOT_FOUND("KO-MONGO-DB-NOT-FOUND", "Elemento non trovato sul MongoDB"),
	KO_REDIS("KO-REDIS", "Errore nella chiamata a Redis"),
	KO_REDIS_NOT_FOUND("KO-REDIS-NOT-FOUND", "Elemento non trovato in cache"); 

	private String code;
	
	public String getCode() {
		return code;
	}

	private String description;

	private ErrorLogEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}

	public String getDescription() {
		return description;
	}

}

