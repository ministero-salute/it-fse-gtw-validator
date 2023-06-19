/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.config.audit;


import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.AuditETY;

import javax.servlet.http.HttpServletRequest;

public interface AuditFilter {
    boolean match(HttpServletRequest req);
    AuditETY apply(String uri, HttpServletRequest req, Object body) throws Exception;
}
