/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.validator.controller;

import javax.validation.ValidationException;

import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;

/**
 *	Validation class.
 */
public final class Validation {
	
	/**
	 * Empty constructor.
	 */
	private Validation() {
	}

	/**
	 * Asserts that an object or a list of object is not {@code null}.
	 * 
	 * @param objs	List of objects to validate.
	 */
	public static void notNull(final Object... objs) {
		boolean notValid = false;
		for (final Object obj:objs) {
			if (obj == null) {
				notValid = true;
			} else if (obj instanceof String) {
				String checkString = (String)obj;
				checkString = checkString.trim();
				if(StringUtility.isNullOrEmpty(checkString)) {
					notValid = true;
				}
			}
			if (notValid) {
				throw new ValidationException("Violazione vincolo not null.");
			}
		}
	}

	public static void mustBeTrue(Boolean securityCheck, String msg) {
		if (securityCheck==null || !securityCheck) {
			throw new ValidationException(msg);
		}
	}

}
