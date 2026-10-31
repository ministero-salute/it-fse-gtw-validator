package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.mock.web.*;
import org.springframework.http.MediaType;
import org.springframework.http.server.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.mockito.ArgumentCaptor;

import it.finanze.sanita.fse2.ms.gtw.validator.adapter.CustomResponseBodyAdviceAdapter;
import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.*;
import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.exclusions.*;
import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.filter.AnyNonTermsReqFilter;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.filter.CapturingFilter;
import it.finanze.sanita.fse2.ms.gtw.validator.filter.interceptor.LogInterceptor;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.AuditETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.IAuditRepo;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({AuditManager.class, AnyNonTermsReqFilter.class, SingletonExclusion.class, SwaggerExclusion.class,
        ActuatorExclusion.class, CustomResponseBodyAdviceAdapter.class, LogInterceptor.class, AuditIsolationTest.Properties.class})
class AuditIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @Configuration static class Properties {
        @Bean WebEndpointProperties endpoints() {
            WebEndpointProperties props = new WebEndpointProperties();
            props.setBasePath(""); props.getExposure().setInclude(Set.of("health", "info"));
            props.getPathMapping().put("health", "status"); return props;
        }
        @Bean SwaggerUiConfigProperties swagger() { return new SwaggerUiConfigProperties(); }
        @Bean SpringDocConfigProperties docs() { return new SpringDocConfigProperties(); }
    }
    @Autowired private AuditManager manager;
    @Autowired private AnyNonTermsReqFilter filter;
    @Autowired private SingletonExclusion singleton;
    @Autowired private SwaggerExclusion swagger;
    @Autowired private ActuatorExclusion actuator;
    @Autowired private SwaggerUiConfigProperties swaggerProperties;
    @Autowired private CustomResponseBodyAdviceAdapter advice;
    @Autowired private LogInterceptor interceptor;
    @MockitoBean private ConfigSRV config;
    @MockitoBean private IAuditRepo repository;

    private ContentCachingRequestWrapper request(String uri, String content) throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", uri);
        raw.setContent(content.getBytes(StandardCharsets.UTF_8));
        raw.setAttribute("START_TIME", new Date(1));
        ContentCachingRequestWrapper cached = new ContentCachingRequestWrapper(raw);
        cached.getInputStream().readAllBytes();
        return cached;
    }

    @Test void capturesRequestAndPersistsDecodedAuditWithResponse() throws Exception {
        var request = request("/v1/document%20validation", "{\"cda\":\"example\"}");
        Object response = Map.of("valid", true);
        manager.process(request, response);
        ArgumentCaptor<AuditETY> saved = ArgumentCaptor.forClass(AuditETY.class);
        verify(repository).save(saved.capture());
        AuditETY audit = saved.getValue();
        assertEquals("/v1/document validation", audit.getServizio());
        assertEquals(new Date(1), audit.getStart_time());
        assertNotNull(audit.getEnd_time());
        assertEquals(Map.of("cda", "example"), audit.getRequest());
        assertSame(response, audit.getResponse());
    }

    @Test void skipsExcludedPathsTerminologyAndBenchmarkPayload() throws Exception {
        manager.process(request("/v1/singletons", "{}"), Map.of());
        manager.process(request("/v1/validate-terminology", "{}"), Map.of());
        manager.process(request("/v1/validate", "{\"cda\":\"<!--CDA_BENCHMARK_TEST-->\"}"), Map.of());
        verifyNoInteractions(repository);
    }

    @Test void malformedJsonAndRepositoryFailuresDoNotBreakResponse() throws Exception {
        assertDoesNotThrow(() -> manager.process(request("/v1/validate", "not json"), Map.of()));
        verifyNoInteractions(repository);
        doThrow(new RuntimeException("offline")).when(repository).save(any());
        assertDoesNotThrow(() -> manager.process(request("/v1/validate", "{}"), Map.of()));
        verify(repository).save(any());
    }

    @Test void uncachedRequestUsesDiagnosticFallback() throws Exception {
        AuditETY audit = filter.apply("/v1/validate", new MockHttpServletRequest(), "response");
        assertEquals(Map.of("message", "Unable to deserialize request body"), audit.getRequest());
        assertEquals("response", audit.getResponse());
    }

    @Test void exclusionsRespectConfiguredPathsAndNullUris() {
        var request = new MockHttpServletRequest();
        assertFalse(singleton.verify(null, request));
        assertTrue(singleton.verify("/v1/singletons", request));
        assertFalse(singleton.verify("/v1/validate", request));
        assertFalse(actuator.verify(null, request));
        assertTrue(actuator.verify("/status", request));
        assertTrue(actuator.verify("/info", request));
        assertFalse(actuator.verify("/v1/validate", request));
        assertFalse(swagger.verify(null, request));
        assertTrue(swagger.verify("/swagger-ui.html", request));
        assertTrue(swagger.verify("/v3/api-docs", request));
        assertFalse(swagger.verify("/v1/validate", request));
        swaggerProperties.setEnabled(false);
        try { assertFalse(swagger.verify("/swagger-ui.html", request)); }
        finally { swaggerProperties.setEnabled(true); }
    }

    @Test void capturingFilterMakesConsumedBodyAvailableDownstream() throws Exception {
        MockHttpServletRequest raw = new MockHttpServletRequest("POST", "/v1/validate");
        raw.setContent("payload".getBytes(StandardCharsets.UTF_8));
        new CapturingFilter().doFilter(raw, new MockHttpServletResponse(), (req, res) -> {
            assertInstanceOf(ContentCachingRequestWrapper.class, req);
            assertEquals("payload", new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("payload", new String(((ContentCachingRequestWrapper) req).getContentAsByteArray(), StandardCharsets.UTF_8));
        });
    }

    @Test void interceptorSetsStartTimeOnlyWhenAuditEnabled() {
        var request = new MockHttpServletRequest(); var response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, this));
        assertNull(request.getAttribute("START_TIME"));
        when(config.isAuditEnable()).thenReturn(true);
        assertTrue(interceptor.preHandle(request, response, this));
        assertInstanceOf(Date.class, request.getAttribute("START_TIME"));
    }

    @Test void responseAdvicePersistsOnlyEnabledServletRequestsAndPreservesBody() throws Exception {
        var request = request("/v1/validate", "{}");
        var servletRequest = new ServletServerHttpRequest(request);
        var response = new ServletServerHttpResponse(new MockHttpServletResponse());
        Object body = Map.of("valid", true);
        assertTrue(advice.supports(null, null));
        assertSame(body, advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, servletRequest, response));
        verifyNoInteractions(repository);
        when(config.isAuditEnable()).thenReturn(true);
        assertSame(body, advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, servletRequest, response));
        verify(repository).save(any());
        clearInvocations(config);
        assertSame(body, advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null,
                mock(ServerHttpRequest.class), mock(ServerHttpResponse.class)));
        verifyNoInteractions(config);
        when(config.isAuditEnable()).thenThrow(new IllegalStateException("config offline"));
        assertThrows(BusinessException.class, () -> advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON, null, servletRequest, response));
    }
}
