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
package it.finanze.sanita.fse2.ms.gtw.validator.service.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO.ConfigDataItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_CONTROL_LOG_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO.ConfigDataItemDTO;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.VALIDATOR;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.priority;


@Service
@Slf4j
public class ConfigSRV implements IConfigSRV {

	private final Map<String, Pair<Long, String>> props;

	@Autowired
	private IConfigClient client;

	@Autowired
	private ProfileUtility profiles;

    @Value("${ms.config.refresh-rate:900000}")
	private Long refreshRate;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}

	@PostConstruct
	public void postConstruct() {
		if(!profiles.isTestProfile()) {
			init();
		} else {
			log.info("Skipping gtw-config initialization due to test profile");
		}
	}

	private void init(){
		for(ConfigItemTypeEnum en : priority()) {
			log.info("[GTW-CFG] Retrieving {} properties ...", en.name());
			ConfigItemDTO items = client.getConfigurationItems(en);
			List<ConfigDataItemDTO> opts = items.getConfigurationItems();
			for(ConfigDataItemDTO opt : opts) {
				opt.getItems().forEach((key, value) -> {
					log.info("[GTW-CFG] Property {} is set as {}", key, value);
					props.put(key, Pair.of(new Date().getTime(), value));
				});
			}
			if(opts.isEmpty()) log.info("[GTW-CFG] No props were found");
		}
		integrity();
	}

	@Override
	public Boolean isAuditEnable() {
		long lastUpdate = props.get(PROPS_NAME_AUDIT_ENABLED).getKey();
		if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
			synchronized(Locks.AUDIT_ENABLED) {
				if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
					refresh(PROPS_NAME_AUDIT_ENABLED);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_AUDIT_ENABLED).getValue()
		);
	}

	@Override
	public Boolean isControlLogPersistenceEnable() {
		long lastUpdate = props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getKey();
		if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
			synchronized(Locks.CONTROL_LOG_ENABLED) {
				if (new Date().getTime() - lastUpdate >= getRefreshRate()) {
					refresh(PROPS_NAME_CONTROL_LOG_ENABLED);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_CONTROL_LOG_ENABLED).getValue()
		);
	}

	@Override
	public long getRefreshRate() {
        return this.refreshRate;
	}

	private void refresh(String name) {
		String previous = props.getOrDefault(name, Pair.of(0L, null)).getValue();
		String prop = client.getProps(name, previous, VALIDATOR);
		props.put(name, Pair.of(new Date().getTime(), prop));
	}

	private void integrity() {
		String err = "Missing props {} from validator";
		String[] out = new String[]{
			PROPS_NAME_CONTROL_LOG_ENABLED,
			PROPS_NAME_AUDIT_ENABLED
		};
		for (String prop : out) {
			if(!props.containsKey(prop)) throw new IllegalStateException(err.replace("{}", prop));
		}
	}

	private static final class Locks {
		public static final Object CONTROL_LOG_ENABLED = new Object();
		public static final Object AUDIT_ENABLED = new Object();

	}

}
