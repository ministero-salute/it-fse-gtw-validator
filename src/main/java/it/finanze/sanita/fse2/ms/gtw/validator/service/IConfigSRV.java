package it.finanze.sanita.fse2.ms.gtw.validator.service;

public interface IConfigSRV {

	String getEdsStrategy();

	Boolean isAuditEnable();
	
	Boolean isControlLogPersistenceEnable();
	
}
