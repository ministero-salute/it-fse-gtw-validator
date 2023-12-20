package it.finanze.sanita.fse2.ms.gtw.validator.service;

public interface IConfigSRV {
	Boolean isAuditEnable();
	Boolean isControlLogPersistenceEnable();
	long getRefreshRate();
}
