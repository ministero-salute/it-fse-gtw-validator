/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.WhoIsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.ServerResponseException;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of gtw-config Client.
 */
@Slf4j
@Component
public class ConfigClient implements it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient {

    /**
     * Serial version uid.
     */
    private static final long serialVersionUID = -923144320301638618L;

    /**
     * Config host.
     */
    @Value("${ms.url.gtw-config}")
    private String configHost;

    @Autowired
    private transient RestTemplate restTemplate;

    @Autowired
    private ProfileUtility profileUtility;

    @Override
    public String getGatewayName() {
        String gatewayName = null;
        try {
            log.debug("Config Client - Calling Config Client to get Gateway Name");
            final String endpoint = configHost + Constants.Config.WHOIS_PATH;

            final boolean isTestEnvironment = profileUtility.isDevOrDockerProfile() || profileUtility.isTestProfile();

            // Check if the endpoint is reachable
            if (isTestEnvironment && !isReachable()) {
                log.warn("Config Client - Config Client is not reachable, mocking for testing purpose");
                return Constants.Config.MOCKED_GATEWAY_NAME;
            }

            final ResponseEntity<WhoIsResponseDTO> response = restTemplate.getForEntity(endpoint,
                    WhoIsResponseDTO.class);

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
        try {
            final String endpoint = configHost + Constants.Config.STATUS_PATH;
            restTemplate.getForEntity(endpoint, String.class);
            return true;
        } catch (ResourceAccessException clientException) {
            return false;
        }
    }

}