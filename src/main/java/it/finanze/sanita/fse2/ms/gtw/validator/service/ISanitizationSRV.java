package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

public interface ISanitizationSRV extends Serializable {
	
	String sanitizeCda(String cda);
	
}