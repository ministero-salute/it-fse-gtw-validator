package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchemaETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.SchematronETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IDictionaryRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchemaRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.ISchematronRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;

@Service
public class UpdateSingletonSRV implements IUpdateSingletonSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4694648997894072254L;

	@Autowired
	private ISchemaRepo schemaRepo;
	
	@Autowired
	private ISchematronRepo schematronRepo;
	
	@Autowired
	private IDictionaryRepo dictionaryRepo;
	
	@Override
	public void updateSingletonInstance() {
		updateSchemaSingleton();
		
		updateSchematronSingleton();
	}

	private void updateSchemaSingleton() {
		Map<String,SchemaValidatorSingleton> mapSchema = SchemaValidatorSingleton.getMapInstance();
		if(mapSchema!=null && !mapSchema.isEmpty()) {
			for(Entry<String, SchemaValidatorSingleton> map : mapSchema.entrySet()) {
				SchemaETY father = schemaRepo.findFatherXsd(map.getKey());
				boolean isDifferent = checkDataUltimoAggiornamento(map.getValue().getDataUltimoAggiornamento(), father.getLastUpdateDate());
				if(Boolean.FALSE.equals(isDifferent)) {
					List<SchemaETY> children = schemaRepo.findChildrenXsd(map.getKey());
					for(SchemaETY ety : children) {
						isDifferent = checkDataUltimoAggiornamento(map.getValue().getDataUltimoAggiornamento(), ety.getLastUpdateDate());
						if(Boolean.TRUE.equals(isDifferent)) {
							break;
						}
					}
				}
				
				if(Boolean.TRUE.equals(isDifferent)) {
					 SchemaValidatorSingleton.getInstance(true, father, schemaRepo);
				}
			}
		}
	}
	
	private void updateSchematronSingleton() {
		Map<String,SchematronValidatorSingleton> mapSchema = SchematronValidatorSingleton.getMapInstance();
		if(mapSchema!=null && !mapSchema.isEmpty()) {
			for(Entry<String, SchematronValidatorSingleton> map : mapSchema.entrySet()) {
				SchematronETY schematron = schematronRepo.findByTemplateIdRoot(map.getKey());
				boolean isDifferent = checkDataUltimoAggiornamento(map.getValue().getDataUltimoAggiornamento(), schematron.getLastUpdateDate()); 
				
				if(Boolean.TRUE.equals(isDifferent)) {
					SchematronValidatorSingleton.getInstance(true,schematron, dictionaryRepo);
				}
			}
		}
	}
	
	boolean checkDataUltimoAggiornamento(Date dataInstanza, Date dataETY) {
		return !dataInstanza.equals(dataETY);
	}

}
