/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeSystemVersionDTO {
	
	private String codeSystem;
	private String codeSystemVersion;
	
    @Override
    public boolean equals(Object obj) { 
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        CodeSystemVersionDTO dto = (CodeSystemVersionDTO) obj;
                
        if (this.codeSystem == null && dto.codeSystem != null) return false;
        if (dto.codeSystem == null && this.codeSystem != null) return false;
        
        if (this.codeSystemVersion == null && dto.codeSystemVersion != null) return false;
        if (dto.codeSystemVersion == null && this.codeSystemVersion != null) return false;		
        		
        return this.codeSystem.equals(dto.codeSystem) && this.codeSystemVersion.equals(dto.codeSystemVersion);
    }
 
    @Override
    public int hashCode() {
    	int hashCode = 0;
    	hashCode += this.codeSystem == null ? 0 : this.codeSystem.hashCode();
    	hashCode += this.codeSystemVersion == null ? 0 : this.codeSystemVersion.hashCode();
    	return hashCode;
    }
}