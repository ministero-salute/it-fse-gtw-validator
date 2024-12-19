
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
package it.finanze.sanita.fse2.ms.gtw.validator.client.impl;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.client.routes.ConfigClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.WhoIsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.*;

/**
 * Implementation of gtw-config Client.
 */
@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private ConfigClientRoutes routes;

    @Autowired
    private RestTemplate client;

    @Autowired
    private ProfileUtility profiles;

    @Override
	public ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type) {
		return client.getForObject(routes.getConfigItems(type), ConfigItemDTO.class);
	}
    
    @Override
    public String getGatewayName() {
        String gatewayName;
        try {
            log.debug("Config Client - Calling Config Client to get Gateway Name");
            final String endpoint = routes.whois();

            final boolean isTestEnvironment = profiles.isDevOrDockerProfile() || profiles.isTestProfile();

            // Check if the endpoint is reachable
            if (isTestEnvironment && !isReachable()) {
                log.warn("Config Client - Config Client is not reachable, mocking for testing purpose");
                return Constants.Config.MOCKED_GATEWAY_NAME;
            }

            ResponseEntity<WhoIsResponseDTO> response = client.getForEntity(endpoint, WhoIsResponseDTO.class);
            WhoIsResponseDTO body = response.getBody();

            if (body != null) {
                if (response.getStatusCode().is2xxSuccessful()) {
                    gatewayName = body.getGatewayName();
                } else {
                    log.error("Config Client - Error calling Config Client to get Gateway Name");
                    throw new BusinessException("The Config Client has returned an error");
                }
            } else {
                throw new BusinessException("The Config Client has returned an error - The body is null");
            }
        } catch (HttpStatusCodeException clientException) {
            String msg = "Errore durante l'invocazione dell' API /config/whois. Il sistema ha restituito un " + clientException.getStatusCode();
            throw new ServerResponseException("config", "/config/whois", msg, clientException.getStatusCode(), clientException.getRawStatusCode(), clientException.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Error encountered while retrieving Gateway name", e);
            throw e;
        }
        return gatewayName;
    }

    private boolean isReachable() {
        boolean out;
        try {
            client.getForEntity(routes.status(), String.class);
            out = true;
        } catch (ResourceAccessException clientException) {
            out = false;
        }
        return out;
    }

    
	@Override
	public String getProps(String props, String previous, ConfigItemTypeEnum ms) {
        String out = previous;
        ConfigItemTypeEnum src = ms;
	    // Check if gtw-config is available and get props
        if (isReachable()) {
            // Try to get the specific one
            out = client.getForObject(routes.getConfigItem(ms, props), String.class);
            // If the props don't exist
            if (out == null) {
                // Retrieve the generic one
                out = client.getForObject(routes.getConfigItem(GENERIC, props), String.class);
                // Set where has been retrieved from
                src = GENERIC;
            }
        }
        if(out == null || !out.equals(previous)) {
            log.info("[GTW-CFG] {} set as {} (previously: {}) from {}", props, out, previous, src);
        }
	    return out;
	}
}
