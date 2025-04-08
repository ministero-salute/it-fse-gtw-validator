package it.finanze.sanita.fse2.ms.gtw.validator.cfg;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_AUDIT_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.PROPS_NAME_CONTROL_LOG_ENABLED;
import static it.finanze.sanita.fse2.ms.gtw.validator.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import it.finanze.sanita.fse2.ms.gtw.validator.adapter.CustomResponseBodyAdviceAdapter;
import it.finanze.sanita.fse2.ms.gtw.validator.config.WebCFG;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class ConfigTest extends AbstractConfig {

    // Exclude from running by mocking
    @MockitoBean
    private WebCFG web;
    @MockitoBean
    private CustomResponseBodyAdviceAdapter adapter;

    private static final List<Pair<String, String>> DEFAULT_PROPS = Arrays.asList(
        Pair.of(PROPS_NAME_AUDIT_ENABLED, "false"),
        Pair.of(PROPS_NAME_CONTROL_LOG_ENABLED, "false")
    );

    @Test
    void testCacheProps() {
        testCacheProps(DEFAULT_PROPS.get(0), () -> assertFalse(config.isAuditEnable()));
        testCacheProps(DEFAULT_PROPS.get(1), () -> assertFalse(config.isControlLogPersistenceEnable()));
    }

    @Test
    void testRefreshProps() {
        testRefreshProps(DEFAULT_PROPS.get(0), "true", () -> assertTrue(config.isAuditEnable()));
        testRefreshProps(DEFAULT_PROPS.get(1), "true", () -> assertTrue(config.isControlLogPersistenceEnable()));
    }

    @Test
    void testIntegrityProps() {
        testIntegrityCheck();
    }

    @Override
    public List<Pair<String, String>> defaults() {
        return DEFAULT_PROPS;
    }
}
