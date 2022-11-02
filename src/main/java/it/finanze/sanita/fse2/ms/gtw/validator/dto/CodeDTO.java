/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
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
        		
        return 
        		this.code.equals(dto.code) && 
        		this.codeSystem.equals(dto.codeSystem) &&
        		this.version.equals(dto.version);
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
    	return code + " [" + codeSystem + " v" + version + "]";
    }
    
}