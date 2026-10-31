package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.security.auth.callback.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.mockito.MockedStatic;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;

import it.finanze.sanita.fse2.ms.gtw.validator.config.*;
import it.finanze.sanita.fse2.ms.gtw.validator.config.health.KafkaHealthIndicator;
import it.finanze.sanita.fse2.ms.gtw.validator.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.config.kafka.oauth2.*;
import it.finanze.sanita.fse2.ms.gtw.validator.config.properties.PropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.filter.interceptor.LogInterceptor;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.CollectionNaming;
import it.finanze.sanita.fse2.ms.gtw.validator.scheduler.UpdateSingletonScheduler;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IUpdateSingletonSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({WebCFG.class, PropertiesCFG.class, UpdateSingletonScheduler.class,
        ConfigurationIsolationTest.Health.class})
@TestPropertySource(properties = {"vocabulary.find-specific-error=true", "vocabulary.find-system-code-independence=false",
        "kafka.bootstrap-servers=unused:9092", "kafka.properties.security.protocol=SASL_SSL",
        "kafka.properties.sasl.mechanism=OAUTHBEARER", "kafka.properties.sasl.jaas.config=jaas",
        "kafka.properties.ssl.truststore.location=truststore", "kafka.properties.ssl.truststore.password=secret"})
class ConfigurationIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @Configuration static class Health {
        @Bean KafkaHealthIndicator health() { return new KafkaHealthIndicator(); }
        // Plain factory objects let these tests exercise both branches of their public methods,
        // rather than Spring's cached @Bean method interception.
        @Bean CollectionNaming collections() { return new CollectionNaming(); }
        @Bean KafkaPropertiesCFG kafkaProperties() { return new KafkaPropertiesCFG(); }
    }
    @Autowired private CollectionNaming collections;
    @Autowired private WebCFG web;
    @Autowired private PropertiesCFG properties;
    @Autowired private UpdateSingletonScheduler scheduler;
    @Autowired private KafkaPropertiesCFG kafka;
    @Autowired private KafkaHealthIndicator health;
    @MockitoBean private ProfileUtility profiles;
    @MockitoBean private IConfigSRV config;
    @MockitoBean private LogInterceptor interceptor;
    @MockitoBean private IUpdateSingletonSRV updates;
    @MockitoBean private AdminClient admin;

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void collectionNamesRespectTestPrefix(boolean test) {
        when(profiles.isTestProfile()).thenReturn(test);
        String prefix = test ? Constants.Profile.TEST_PREFIX : "";
        assertEquals(prefix + Constants.Collections.DICTIONARY, collections.getDictionaryCollection());
        assertEquals(prefix + Constants.Collections.SCHEMA, collections.getSchemaCollection());
        assertEquals(prefix + Constants.Collections.SCHEMATRON, collections.getSchematronCollection());
        assertEquals(prefix + Constants.Collections.ENGINES, collections.getEngineCollection());
        assertEquals(prefix + Constants.Collections.TERMINOLOGY, collections.getTerminologyCollection());
        assertEquals(prefix + Constants.Collections.AUDIT, collections.getAudit());
    }

    @Test void auditInterceptorRegistrationAndVocabularyFlags() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        web.addInterceptors(registry); verifyNoInteractions(registry);
        when(config.isAuditEnable()).thenReturn(true);
        web.addInterceptors(registry); verify(registry).addInterceptor(interceptor);
        assertTrue(properties.isFindSpecificErrorVocabulary());
        assertFalse(properties.isFindSystemAndCodesIndependence());
        scheduler.schedulingTask(); verify(updates).updateSingletonInstance();
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void adminClientConfigurationOmitsCredentialsInDevelopment(boolean development) {
        when(profiles.isDevOrDockerProfile()).thenReturn(development);
        try (MockedStatic<AdminClient> factory = mockStatic(AdminClient.class)) {
            factory.when(() -> AdminClient.create(any(Properties.class))).thenReturn(admin);
            assertSame(admin, kafka.client());
            factory.verify(() -> AdminClient.create(argThat((Properties props) ->
                    "unused:9092".equals(props.getProperty("bootstrap.servers"))
                    && (development ? !props.containsKey("security.protocol")
                    : "SASL_SSL".equals(props.getProperty("security.protocol"))
                    && "secret".equals(props.getProperty("ssl.truststore.password"))))));
        }
    }

    @Test void kafkaHealthReportsSuccessFailureAndRestoresInterruption() throws Exception {
        ListTopicsResult result = mock(ListTopicsResult.class);
        @SuppressWarnings("unchecked") KafkaFuture<Collection<TopicListing>> future = mock(KafkaFuture.class);
        when(admin.listTopics()).thenReturn(result); when(result.listings()).thenReturn(future);
        when(future.get()).thenReturn(List.of());
        assertEquals("UP", health.health().getStatus().getCode());
        when(future.get()).thenThrow(new ExecutionException(new RuntimeException("offline")));
        assertEquals("DOWN", health.health().getStatus().getCode());
        doThrow(new InterruptedException("interrupted")).when(future).get();
        try {
            assertEquals("DOWN", health.health().getStatus().getCode());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally { Thread.interrupted(); }
    }

    @Test void profileUtilityUsesActualSpringEnvironment() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            MockEnvironment environment = new MockEnvironment(); context.setEnvironment(environment);
            context.register(ProfileUtility.class); context.refresh();
            ProfileUtility utility = context.getBean(ProfileUtility.class);
            assertFalse(utility.isTestProfile()); assertFalse(utility.isDevOrDockerProfile());
            environment.setActiveProfiles("test"); assertTrue(utility.isTestProfile());
            environment.setActiveProfiles("dev"); assertTrue(utility.isDevOrDockerProfile());
            environment.setActiveProfiles("docker"); assertTrue(utility.isDevOrDockerProfile());
            environment.setActiveProfiles("ISOLATION"); assertFalse(utility.isTestProfile()); assertFalse(utility.isDevOrDockerProfile());
        }
    }

    @Test void openApiCustomizersConstrainSchemasMarkSandboxAndAddDefaultErrors() {
        OpenAPI api = new OpenAPI().components(new Components().addSchemas("Example", new Schema<>()))
                .servers(List.of(new Server().url("http://localhost"), new Server().url("https://production")))
                .paths(new Paths().addPathItem("/validate", new PathItem().post(new Operation().responses(new ApiResponses()))));
        OpenApiCFG configuration = new OpenApiCFG();
        configuration.openApiCustomiser().customise(api);
        configuration.customerGlobalHeaderOpenApiCustomiser().customise(api);
        assertEquals(false, api.getComponents().getSchemas().get("Example").getAdditionalProperties());
        assertEquals(true, api.getServers().get(0).getExtensions().get("x-sandbox"));
        assertNull(api.getServers().get(1).getExtensions());
        assertEquals("#/components/schemas/ErrorResponseDTO", api.getPaths().get("/validate").getPost().getResponses()
                .get("default").getContent().get("application/problem+json").getSchema().get$ref());
    }

    @Test void oauthTokenExposesTokenAndExpiryAndCallbackRejectsUnsupportedOrMissingCertificate() throws Exception {
        OAuthBearerTokenImp token = new OAuthBearerTokenImp("token", new Date(123));
        assertEquals("token", token.value()); assertEquals(123, token.lifetimeMs());
        assertNull(token.scope()); assertNull(token.principalName()); assertNull(token.startTimeMs());
        CustomAuthenticateCallbackHandler handler = new CustomAuthenticateCallbackHandler();
        handler.configure(Map.of("bootstrap.servers", "unused:9092", "kafka.oauth.tenantId", "tenant",
                "kafka.oauth.appId", "app", "kafka.oauth.pfxPathName", "/nonexistent-isolation-certificate.pfx", "kafka.oauth.pwd", "secret"),
                "OAUTHBEARER", List.of());
        assertThrows(UnsupportedCallbackException.class, () -> handler.handle(new Callback[]{new NameCallback("name")}));
        assertThrows(BusinessException.class, () -> handler.handle(new Callback[]{new OAuthBearerTokenCallback()}));
        handler.close();
    }
}
