package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

public interface IUpdateSingletonSRV extends Serializable {

	void updateSingletonInstance(String requestURL);
}
