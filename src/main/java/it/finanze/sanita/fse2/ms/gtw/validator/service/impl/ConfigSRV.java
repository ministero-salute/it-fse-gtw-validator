package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.EdsStrategyEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_EDS_STRATEGY;

@Service
@Slf4j
public class ConfigSRV implements IConfigSRV {

	private static final Long DELTA_MS = 300000L;

	private final Map<String, Pair<Long, Object>> props;

	@Autowired
	private IConfigClient client;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}

	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		refreshAuditEnabled();
		refreshEdsStrategy();
		runningConfiguration();
	}

	private void refreshEdsStrategy() {
		String strategy = client.getEDSStrategy();
		props.put(PROPS_NAME_EDS_STRATEGY, Pair.of(new Date().getTime(), strategy));
	}

	private void refreshAuditEnabled() {
		boolean audit = client.isAuditEnabled();
		props.put(PROPS_NAME_AUDIT_ENABLED, Pair.of(new Date().getTime(), audit));
	}

	private void runningConfiguration() {
		props.forEach((id, pair) -> log.info("[GTW-CONFIG] key: {} | value: {}", id, pair.getValue()));
	}

	@Override
	public Boolean isAuditEnable() {
		Pair<Long, Object> pair = props.getOrDefault(
			PROPS_NAME_AUDIT_ENABLED,
			Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_AUDIT_ENABLED) {
				refreshAuditEnabled();
				verifyAuditEnabled(pair);
			}
		}
		return (Boolean) props.get(PROPS_NAME_AUDIT_ENABLED).getValue();
	}

	@Override
	public String getEdsStrategy() {
		Pair<Long, Object> pair = props.getOrDefault(
			PROPS_NAME_EDS_STRATEGY,
			Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_EDS_STRATEGY) {
				refreshEdsStrategy();
				verifyEdsStrategy(pair);
			}
		}
		return (String) props.get(PROPS_NAME_EDS_STRATEGY).getValue();
	}

	@Override
	public boolean isNoEds() {
		String out = getEdsStrategy();
		return !StringUtility.isNullOrEmpty(out) && EdsStrategyEnum.NO_EDS.name().equalsIgnoreCase(out);
	}

	private void verifyAuditEnabled(Pair<Long, Object> pair) {
		Boolean previous = (Boolean) pair.getValue();
		Boolean current = (Boolean) props.get(PROPS_NAME_AUDIT_ENABLED).getValue();
		if(!previous.equals(current)) {
			log.info(
				"[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})",
				PROPS_NAME_AUDIT_ENABLED,
				current,
				previous
			);
		}
	}

	private void verifyEdsStrategy(Pair<Long, Object> pair) {
		String previous = (String) pair.getValue();
		String current = (String) props.get(PROPS_NAME_EDS_STRATEGY).getValue();
		if(!previous.equals(current)) {
			log.info(
				"[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})",
				PROPS_NAME_EDS_STRATEGY,
				current,
				previous
			);
		}
	}
}
