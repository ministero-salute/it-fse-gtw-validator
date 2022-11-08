/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CodeSystemVersionDTO {
	
	private String codeSystem;
	private String version;
	
    @Override
    public boolean equals(Object obj) { 
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        CodeSystemVersionDTO dto = (CodeSystemVersionDTO) obj;
                
        if (this.codeSystem == null && dto.codeSystem != null) return false;
        if (dto.codeSystem == null && this.codeSystem != null) return false;
        
        if (this.version == null && dto.version != null) return false;
        if (dto.version == null && this.version != null) return false;		
        		
        if (this.codeSystem == null && this.version == null) return true;
        if (this.version == null) return this.codeSystem.equals(dto.codeSystem);
        
        return this.codeSystem.equals(dto.codeSystem) && this.version.equals(dto.version);
    }
 
    @Override
    public int hashCode() {
    	int hashCode = 0;
    	hashCode += this.codeSystem == null ? 0 : this.codeSystem.hashCode();
    	hashCode += this.version == null ? 0 : this.version.hashCode();
    	return hashCode;
    }
    
    @Override
    public String toString() {
    	if (version == null) return codeSystem;
    	return codeSystem + " v" + version;
    }
}