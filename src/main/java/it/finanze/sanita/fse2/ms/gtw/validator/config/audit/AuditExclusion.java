package it.finanze.sanita.fse2.ms.gtw.validator.config.audit;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface AuditExclusion {
    boolean verify(String uri, HttpServletRequest req);
}
