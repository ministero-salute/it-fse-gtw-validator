package it.finanze.sanita.fse2.ms.gtw.validator.cfg;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.validator.utility.ProfileUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.validator.dto.ConfigItemDTO.ConfigDataItemDTO;
import static it.finanze.sanita.fse2.ms.gtw.validator.enums.ConfigItemTypeEnum.GENERIC;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public abstract class AbstractConfig {

    @SpyBean
    protected ConfigSRV config;
    @MockBean
    private IConfigClient client;
    @SpyBean
    private ProfileUtility profiles;

    public abstract List<Pair<String, String>> defaults();

    protected void testCacheProps(Pair<String, String> prop, Runnable fn) {
        // Set default props
        setup(prop);
        // Check it returns the cached-value
        fn.run();
        // Verify client has not been invoked
        verify(client, never()).getProps(eq(prop.getKey()), any(), any());
    }

    protected void testRefreshProps(Pair<String, String> prop, String newValue, Runnable fn) {
        // Set default props
        setup(prop);
        // Mock new answer
        when(client.getProps(eq(prop.getKey()), any(), any())).thenReturn(newValue);
        // Force refresh
        doReturn(0L).when(config).getRefreshRate();
        // Check it returns the new-value
        fn.run();
        // Verify client has been invoked
        verify(client, times(1)).getProps(eq(prop.getKey()), any(), any());
    }

    protected void testIntegrityCheck() {
        // Forget one props
        List<Pair<String, String>> broken = new ArrayList<>(defaults());
        broken.remove(0);
        // Expect broken
        assertThrows(IllegalStateException.class, () -> setup(broken));
    }

    @SafeVarargs
    private final void setup(Pair<String, String>... keys) {
        setup(defaults(), keys);
    }

    @SafeVarargs
    private final void setup(List<Pair<String, String>> defaultProps, Pair<String, String>... keys) {
        ConfigItemDTO req = request();
        Map<String, String> generics = req.getConfigurationItems().get(0).getItems();
        // Load generics
        for (Pair<String, String> def : defaultProps) {
            generics.put(def.getKey(), def.getValue());
        }
        // Apply specific
        for (Pair<String, String> props : keys) {
            generics.put(props.getKey(), props.getValue());
        }
        when(client.getConfigurationItems(any())).thenReturn(req);
        doReturn(false).when(profiles).isTestProfile();
        config.postConstruct();
    }

    private ConfigItemDTO request() {
        ConfigItemDTO items = new ConfigItemDTO();
        List<ConfigDataItemDTO> values = new ArrayList<>();

        ConfigDataItemDTO generic = new ConfigDataItemDTO();
        generic.setKey(GENERIC.name());
        generic.setItems(new HashMap<>());

        values.add(generic);
        items.setConfigurationItems(values);

        return items;
    }

}
