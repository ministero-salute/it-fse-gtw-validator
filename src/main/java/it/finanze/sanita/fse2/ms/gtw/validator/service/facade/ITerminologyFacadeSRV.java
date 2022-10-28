/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.service.facade;

import java.io.Serializable;

public interface ITerminologyFacadeSRV extends Serializable {

	boolean existBySystemAndCode(String system, String code);
}
