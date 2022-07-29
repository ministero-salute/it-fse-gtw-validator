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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
	public void updateSingletonInstance(final String requestURL) {
		updateSchemaSingleton();
		updateSchematronSingleton(requestURL);
	}

	private void updateSchemaSingleton() {
		Map<String,SchemaValidatorSingleton> mapSchema = SchemaValidatorSingleton.getMapInstance();
		if(mapSchema!=null && !mapSchema.isEmpty()) {
			for(Entry<String, SchemaValidatorSingleton> map : mapSchema.entrySet()) {
				String searchKey = map.getKey();
				SchemaETY father = schemaRepo.findFatherXsd(searchKey);
				if (father == null) {
					log.warn("No schema found on DB... singleton map will be reset");
					mapSchema.remove(searchKey);
				} else {
					log.info("Father schema found on DB... check update time");
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
	}
	
	private void updateSchematronSingleton(final String requestUrl) {
		Map<String,SchematronValidatorSingleton> mapSchema = SchematronValidatorSingleton.getMapInstance();
		if (mapSchema != null && !mapSchema.isEmpty()) {
			for(Entry<String, SchematronValidatorSingleton> map : mapSchema.entrySet()) {
				String searchKey = map.getKey();
				SchematronETY schematron = schematronRepo.findByTemplateIdRoot(searchKey);
				if (schematron == null) {
					log.warn("No schematron found on DB... singleton map will be reset");
					mapSchema.remove(searchKey);
				} else {
					boolean isDifferent = checkDataUltimoAggiornamento(map.getValue().getDataUltimoAggiornamento(), schematron.getLastUpdateDate());

					if(Boolean.TRUE.equals(isDifferent)) {
						SchematronValidatorSingleton.getInstance(true,schematron, requestUrl);
					}
				}
			}
		}
	}
	
	boolean checkDataUltimoAggiornamento(Date dataInstanza, Date dataETY) {
		if (dataInstanza != null && dataETY != null) {
			return !dataInstanza.equals(dataETY);
		}
		return false;
	}

}
