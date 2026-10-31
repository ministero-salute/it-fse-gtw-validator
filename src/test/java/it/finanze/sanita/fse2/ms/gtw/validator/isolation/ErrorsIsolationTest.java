package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import it.finanze.sanita.fse2.ms.gtw.validator.controller.handler.ControllerExceptionHandler;
import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.*;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ControllerExceptionHandler.class, ErrorsIsolationTest.Tracing.class})
class ErrorsIsolationTest {
    @org.springframework.context.annotation.Profile("ISOLATION")
    @Configuration static class Tracing { @Bean Tracer tracer() { return OpenTelemetry.noop().getTracer("isolation"); } }
    @Autowired private ControllerExceptionHandler handler;
    @org.springframework.context.annotation.Profile("ISOLATION")
    @RestController static class FailingEndpoint {
        @GetMapping("/missing") Object missing() { throw new NoRecordFoundException("schema missing"); }
        @GetMapping("/generic") Object generic() { throw new BusinessException("database offline"); }
        @GetMapping("/remote") Object remote() { throw new ServerResponseException("mapping", "/transform", "remote failed", HttpStatus.BAD_GATEWAY, 502, "detail"); }
    }

    @Test void dispatchesMissingRecordsToBadRequestAndOtherFailuresToServerError() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new FailingEndpoint()).setControllerAdvice(handler).build();
        mvc.perform(get("/missing")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value(400))
                .andExpect(jsonPath("$.error.message").value("schema missing"));
        mvc.perform(get("/generic")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.error.code").value(500))
                .andExpect(jsonPath("$.error.message").value("database offline"));
        mvc.perform(get("/remote")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.error.message").value("remote failed"));
    }
}
