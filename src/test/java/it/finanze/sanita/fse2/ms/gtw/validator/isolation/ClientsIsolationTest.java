package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import it.finanze.sanita.fse2.ms.gtw.validator.client.impl.*;
import it.finanze.sanita.fse2.ms.gtw.validator.client.routes.*;
import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.client.FhirResourceDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.*;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ConfigClient.class, ConfigClientRoutes.class, FhirMappingClient.class,
        FhirMappingClientRoutes.class, ClientsIsolationTest.Http.class})
@TestPropertySource(properties = {"ms.url.gtw-config=http://config", "ms.url.gtw-fhir-mapping-engine=http://mapping"})
class ClientsIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @Configuration static class Http { @Bean RestTemplate restTemplate() { return new RestTemplate(); } }
    @Autowired private RestTemplate http;
    @Autowired private ConfigClient config;
    @Autowired private ConfigClientRoutes configRoutes;
    @Autowired private FhirMappingClient mapping;
    @Autowired private FhirMappingClientRoutes mappingRoutes;
    @MockitoBean private ProfileUtility profiles;
    private MockRestServiceServer server;
    @BeforeEach void server() { server = MockRestServiceServer.bindTo(http).build(); }

    @Test void retrievesConfigurationAndGatewayName() {
        server.expect(requestTo("http://config/v1/config-items?type=VALIDATOR"))
                .andRespond(withSuccess("{\"configurationItems\":[]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://config/v1/whois"))
                .andRespond(withSuccess("{\"gatewayName\":\"gateway\"}", MediaType.APPLICATION_JSON));
        assertTrue(config.getConfigurationItems(ConfigItemTypeEnum.VALIDATOR).getConfigurationItems().isEmpty());
        assertEquals("gateway", config.getGatewayName());
        assertEquals("cfg", configRoutes.microservice());
        assertEquals("[CFG]", configRoutes.identifier());
        assertEquals("fhir-mapping-engine", mappingRoutes.microservice());
        assertEquals("[FHIR-MAPPING]", mappingRoutes.identifier());
        server.verify();
    }

    @Test void unreachableDevelopmentGatewayUsesDocumentedFallback() {
        when(profiles.isDevOrDockerProfile()).thenReturn(true);
        server.expect(requestTo("http://config/status")).andRespond(request -> { throw new ResourceAccessException("offline"); });
        assertEquals(Constants.Config.MOCKED_GATEWAY_NAME, config.getGatewayName());
        server.verify();
    }

    @Test void reachableTestGatewayStillFetchesIdentity() {
        when(profiles.isTestProfile()).thenReturn(true);
        server.expect(requestTo("http://config/status")).andRespond(withSuccess());
        server.expect(requestTo("http://config/v1/whois")).andRespond(withSuccess("{\"gatewayName\":\"real\"}", MediaType.APPLICATION_JSON));
        assertEquals("real", config.getGatewayName());
        server.verify();
    }

    @Test void wrapsGatewayHttpErrors() {
        server.expect(requestTo("http://config/v1/whois")).andRespond(withServerError());
        assertThrows(ServerResponseException.class, config::getGatewayName);
        server.verify();
    }

    @Test void rejectsEmptyAndNonSuccessfulGatewayResponses() {
        server.expect(requestTo("http://config/v1/whois")).andRespond(withSuccess());
        assertThrows(BusinessException.class, config::getGatewayName);
        server.verify();
        server.reset();
        server.expect(requestTo("http://config/v1/whois")).andRespond(withStatus(HttpStatus.FOUND)
                .contentType(MediaType.APPLICATION_JSON).body("{\"gatewayName\":\"ignored\"}"));
        assertThrows(BusinessException.class, config::getGatewayName);
        server.verify();
    }

    @Test void propertiesFallBackToGenericThenRetainPreviousWhenOffline() {
        server.expect(requestTo("http://config/status")).andRespond(withSuccess());
        server.expect(requestTo("http://config/v1/config-items/props?type=VALIDATOR&props=audit-enabled")).andRespond(withSuccess());
        server.expect(requestTo("http://config/v1/config-items/props?type=GENERIC&props=audit-enabled")).andRespond(withSuccess("true", MediaType.TEXT_PLAIN));
        assertEquals("true", config.getProps("audit-enabled", "false", ConfigItemTypeEnum.VALIDATOR));
        server.verify();
        server.reset();
        server.expect(requestTo("http://config/status")).andRespond(request -> { throw new ResourceAccessException("offline"); });
        assertEquals("true", config.getProps("audit-enabled", "true", ConfigItemTypeEnum.VALIDATOR));
        server.verify();
    }

    @Test void propertiesUseSpecificValueAndPermitMissingGenericValue() {
        server.expect(requestTo("http://config/status")).andRespond(withSuccess());
        server.expect(requestTo(configRoutes.getConfigItem(ConfigItemTypeEnum.VALIDATOR, "key"))).andRespond(withSuccess("specific", MediaType.TEXT_PLAIN));
        assertEquals("specific", config.getProps("key", "old", ConfigItemTypeEnum.VALIDATOR));
        server.verify(); server.reset();
        server.expect(requestTo("http://config/status")).andRespond(withSuccess());
        server.expect(requestTo(configRoutes.getConfigItem(ConfigItemTypeEnum.VALIDATOR, "key"))).andRespond(withSuccess());
        server.expect(requestTo(configRoutes.getConfigItem(ConfigItemTypeEnum.GENERIC, "key"))).andRespond(withSuccess());
        assertNull(config.getProps("key", "old", ConfigItemTypeEnum.VALIDATOR));
        server.verify();
    }

    @Test void mappingSerializesJsonAndRepeatedValidationQueryParameters() {
        server.expect(requestTo("http://mapping/v1/documents/transform?validations=NORMATIVE_R4&validations=TRAVERSABILITY&includeRemovalReport=true"))
                .andExpect(method(HttpMethod.POST)).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"cda\":\"<value/>\",\"engineId\":\"engine\",\"objectId\":\"map\"}", false))
                .andRespond(withSuccess("{\"normalizationIssues\":[]}", MediaType.APPLICATION_JSON));
        assertNotNull(mapping.callConvertCdaInBundle(new FhirResourceDTO("<value/>", "map", "engine"), List.of("NORMATIVE_R4", "TRAVERSABILITY"), true));
        server.verify();
    }

    @Test void mappingWrapsHttpFailureAndOmitsUnrequestedOptions() {
        assertEquals("http://mapping/v1/documents/transform", mappingRoutes.transform(List.of(), false).toString());
        server.expect(requestTo(mappingRoutes.transform(null, false))).andRespond(withBadRequest());
        assertThrows(ServerResponseException.class, () -> mapping.callConvertCdaInBundle(new FhirResourceDTO("cda", "map", "engine"), null, false));
        server.verify();
    }
}
