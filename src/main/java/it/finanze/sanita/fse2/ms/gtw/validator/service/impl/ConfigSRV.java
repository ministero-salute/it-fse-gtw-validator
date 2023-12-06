package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_EDS_STRATEGY;
import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_CONTROL_LOG_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.GENERIC;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.VALIDATOR;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ConfigSRV implements IConfigSRV {

	private static final long DELTA_MS = 300_000L;

	private long lastUpdate;

	private final Map<String, Pair<Long, Object>> props;

	@Autowired
	private IConfigClient client;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}

	@PostConstruct
	public void postConstruct() {
		for(ConfigItemTypeEnum en : ConfigItemTypeEnum.values()) {
			for(Entry<String, String> el : client.getConfigurationItems(en).getItems().entrySet()) {
				props.put(el.getKey(), Pair.of(new Date().getTime(), el.getValue()));
			}
		}
	}

	private void refreshAuditEnable() {
		Boolean previous = props.get(PROPS_NAME_AUDIT_ENABLED)!=null ? (Boolean)props.get(PROPS_NAME_AUDIT_ENABLED).getValue() : null;
		Boolean prop = (Boolean)client.getProps(VALIDATOR, PROPS_NAME_AUDIT_ENABLED,previous);
		props.put(PROPS_NAME_AUDIT_ENABLED, Pair.of(new Date().getTime(), prop));
	}

	@Override
	public Boolean isAuditEnable() {
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized(ConfigSRV.class) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refreshAuditEnable();	
				}
			}
		}
		return (Boolean)props.get(PROPS_NAME_AUDIT_ENABLED).getValue();
	}

	private void refreshEdsStrategy() {
		String previous = props.get(PROPS_NAME_EDS_STRATEGY)!=null ? (String)props.get(PROPS_NAME_EDS_STRATEGY).getValue() : null;
		String prop = (String)client.getProps(GENERIC, PROPS_NAME_EDS_STRATEGY,previous);
		props.put(PROPS_NAME_EDS_STRATEGY, Pair.of(new Date().getTime(), prop));
	}


	@Override
	public String getEdsStrategy() {
		Pair<Long, Object> pair = props.getOrDefault(PROPS_NAME_EDS_STRATEGY,Pair.of(0L, null));
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_EDS_STRATEGY) {
				refreshEdsStrategy();
				verifyEdsStrategy(pair);
			}
		}
		return (String) props.get(PROPS_NAME_EDS_STRATEGY).getValue();
	}


	private void verifyEdsStrategy(Pair<Long, Object> pair) {
		String previous = (String) pair.getValue();
		String current = (String) props.get(PROPS_NAME_EDS_STRATEGY).getValue();
		if(!previous.equals(current)) {
			log.info("[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})",PROPS_NAME_EDS_STRATEGY,current,previous);
		}
	}

	private void refreshControlLogPersistenceEnable() {
		Boolean previous = props.get(PROPS_NAME_CONTROL_LOG_ENABLED)!=null ? (Boolean)props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getValue() : null;
		Boolean controlLogEnabled = (Boolean)client.getProps(GENERIC, PROPS_NAME_CONTROL_LOG_ENABLED,previous);
		props.put(PROPS_NAME_CONTROL_LOG_ENABLED, Pair.of(new Date().getTime(), controlLogEnabled));

	}


	@Override
	public Boolean isControlLogPersistenceEnable() {
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized(ConfigSRV.class) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refreshControlLogPersistenceEnable();	
				}
			}
		}
		return (Boolean)props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getValue();
	}
}
