package it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo;

import java.io.Serializable;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.StructureMapETY;



public interface IStructureMapRepo extends Serializable {

	StructureMapETY findMapByTemplateIdRoot(String templateIdRoot);
	
	StructureMapETY findMapByName(String mapName);
	
}
