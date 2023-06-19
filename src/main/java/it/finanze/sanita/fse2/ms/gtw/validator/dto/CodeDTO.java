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
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CodeDTO {

	private String code;
	private String codeSystem;
	private String version;
	
    @Override
    public boolean equals(Object obj) { 
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        CodeDTO dto = (CodeDTO) obj;
                
        if (this.code == null && dto.code != null) return false;
        if (dto.code == null && this.code != null) return false;
        
        if (this.codeSystem == null && dto.codeSystem != null) return false;
        if (dto.codeSystem == null && this.codeSystem != null) return false;
        
        if (this.version == null && dto.version != null) return false;
        if (dto.version == null && this.version != null) return false;		
        		
        if (this.code == null && codeSystem == null && this.version == null) return true;
        if (this.code != null && codeSystem == null && this.version == null) return this.code.equals(dto.code);
        if (this.code != null && codeSystem != null && this.version == null) return this.code.equals(dto.code) && this.codeSystem.equals(dto.codeSystem);
        
        if(this.code != null && this.codeSystem != null)
	        return 
	        		this.code.equals(dto.code) && 
	        		this.codeSystem.equals(dto.codeSystem) &&
	        		this.version.equals(dto.version);
        else
        	return false;
    }
 
    @Override
    public int hashCode() {
    	int hashCode = 0;
    	hashCode += this.code == null ? 0 : this.code.hashCode();
    	hashCode += this.codeSystem == null ? 0 : this.codeSystem.hashCode();
    	hashCode += this.version == null ? 0 : this.version.hashCode();
    	return hashCode;
    }
    
    public CodeSystemVersionDTO getCodeSystemVersion() {
    	return new CodeSystemVersionDTO(codeSystem, version);
    }
    
    @Override
    public String toString() {
    	String code = isEmpty(this.code) ? "?" : this.code;
    	String codeSystem = isEmpty(this.codeSystem) ? "?" : this.codeSystem;
    	if (isEmpty(this.version)) return code + " [" + codeSystem + "]";
    	return code + " [" + codeSystem + " v" + version + "]";
    }
    
}