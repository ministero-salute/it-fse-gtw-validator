package it.finanze.sanita.fse2.ms.gtw.validator.service;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

public interface IAuditSRV extends Serializable{
	
	void saveAuditReqRes(HttpServletRequest httpServletRequest, Object body);

}
