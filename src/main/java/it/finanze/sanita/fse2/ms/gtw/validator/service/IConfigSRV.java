package it.finanze.sanita.fse2.ms.gtw.validator.service;

public interface IConfigSRV {

	String getEdsStrategy();

	boolean isNoEds();
	
	boolean isNoFhirEds();
}
