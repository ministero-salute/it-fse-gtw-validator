package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static it.finanze.sanita.fse2.ms.gtw.validator.client.routes.base.ClientRoutes.Config.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO.ConfigDataItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;

@Tag("ISOLATION")
@ExtendWith(MockitoExtension.class)
class ConfigServiceIsolationTest {
    @InjectMocks private ConfigSRV service;
    @Mock private IConfigClient client;
    @Mock private ProfileUtility profiles;

    @BeforeEach
    void refreshRate() {
        ReflectionTestUtils.setField(service, "refreshRate", 900000L);
    }

    private ConfigItemDTO items(Map<String, String> values) {
        ConfigDataItemDTO data = new ConfigDataItemDTO();
        data.setItems(values);
        ConfigItemDTO result = new ConfigItemDTO();
        result.setConfigurationItems(values.isEmpty() ? List.of() : List.of(data));
        return result;
    }

    private void initialize() {
        when(client.getConfigurationItems(ConfigItemTypeEnum.GENERIC)).thenReturn(items(Map.of(
                PROPS_NAME_AUDIT_ENABLED, "true", PROPS_NAME_CONTROL_LOG_ENABLED, "true")));
        when(client.getConfigurationItems(ConfigItemTypeEnum.VALIDATOR)).thenReturn(items(Map.of(PROPS_NAME_AUDIT_ENABLED, "false")));
        service.postConstruct();
    }

    @Test
    void validatorConfigurationOverridesGenericAndReadsAreCached() {
        initialize();
        assertFalse(service.isAuditEnable());
        assertTrue(service.isControlLogPersistenceEnable());
        assertEquals(900000L, service.getRefreshRate());
        verify(client, never()).getProps(anyString(), anyString(), any());
    }

    @Test
    void refreshesExpiredPropertiesUsingPreviousValues() {
        initialize();
        ReflectionTestUtils.setField(service, "refreshRate", 0L);
        when(client.getProps(PROPS_NAME_AUDIT_ENABLED, "false", ConfigItemTypeEnum.VALIDATOR)).thenReturn("true");
        when(client.getProps(PROPS_NAME_CONTROL_LOG_ENABLED, "true", ConfigItemTypeEnum.VALIDATOR)).thenReturn("false");
        assertTrue(service.isAuditEnable());
        assertFalse(service.isControlLogPersistenceEnable());
        verify(client).getProps(PROPS_NAME_AUDIT_ENABLED, "false", ConfigItemTypeEnum.VALIDATOR);
        verify(client).getProps(PROPS_NAME_CONTROL_LOG_ENABLED, "true", ConfigItemTypeEnum.VALIDATOR);
    }

    @Test
    void testProfileSkipsRemoteInitialization() {
        when(profiles.isTestProfile()).thenReturn(true);
        service.postConstruct();
        verifyNoInteractions(client);
    }

    @Test
    void rejectsIncompleteConfiguration() {
        when(client.getConfigurationItems(any())).thenReturn(items(Map.of()));
        assertTrue(assertThrows(IllegalStateException.class, service::postConstruct).getMessage().contains("Missing props"));
    }

    @Test
    void propagatesInitializationFailure() {
        RuntimeException failure = new RuntimeException("config unavailable");
        when(client.getConfigurationItems(ConfigItemTypeEnum.GENERIC)).thenThrow(failure);
        assertSame(failure, assertThrows(RuntimeException.class, service::postConstruct));
    }
}
