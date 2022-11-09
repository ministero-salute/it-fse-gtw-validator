/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.XslTransformETY;

/**
 * Interface of repository that consent to fetch and elaborate data from xsl_transform.
 * 
 */
public interface IXslTransformRepo {
    
    /**
     * Returns the last version of xslt file saved with {@code templateId}.
     * 
     * @param templateId Identifier of xslt file.
     * @return The xslt file to execute transformations.
     */
    XslTransformETY getXsltByTemplateId(String templateId); 
}
