
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
package it.finanze.sanita.fse2.ms.gtw.validator.cda;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.Getter;

@Getter
public class ValidationResult implements ErrorHandler {

	private List<String> warnings;

	private List<String> errors;

	private List<String> fatals;

	public ValidationResult(){
		warnings = new ArrayList<>();
		errors   = new ArrayList<>();
		fatals   = new ArrayList<>();
	}


	public boolean isSuccess() {
		if(errors != null && !errors.isEmpty()) return false;
		if(fatals != null && !fatals.isEmpty()) return false;
		return true;
	}

	public int getWarningsCount() {
		if(warnings == null) return 0;
		return warnings.size();
	}

	public int getErrorsCount() {
		if(errors == null) return 0;
		return errors.size();
	}

	public int getFatalsCount() {
		if(fatals == null) return 0;
		return fatals.size();
	}

	public void clear() {
		if(warnings == null) warnings = new ArrayList<>();
		if(errors == null) errors = new ArrayList<>();
		if(fatals == null) fatals = new ArrayList<>();

		warnings.clear();
		errors.clear();
		fatals.clear();
	}

	public void addWarning(String text) {
		if(text == null || text.length() == 0) return;
		if(warnings == null) warnings = new ArrayList<>();
		warnings.add(text);
	}

	public void addError(String text) {
		if(text == null || text.length() == 0) return;
		if(errors == null) errors = new ArrayList<>();
		errors.add(text);
	}

	public void addFatal(String text) {
		if(text == null || text.length() == 0) return;
		if(fatals == null) fatals = new ArrayList<>();
		fatals.add(text);
	}

	@Override
	public 
	void warning(SAXParseException e) throws SAXException {
		addWarning(e.getLineNumber() + "," + e.getColumnNumber() + " " + e.getMessage());
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		addError(e.getLineNumber() + "," + e.getColumnNumber() + " " + e.getMessage());
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		addFatal(e.getLineNumber() + "," + e.getColumnNumber() + " " + e.getMessage());
	}
}
