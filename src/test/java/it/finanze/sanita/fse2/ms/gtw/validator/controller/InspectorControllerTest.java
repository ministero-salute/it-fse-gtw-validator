package it.finanze.sanita.fse2.ms.gtw.validator.controller;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.SingletonInspectorResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class InspectorControllerTest {

    @Autowired
    private ISingletonInspectorCTL controller;

    @Test
    void singletons() {
        SingletonInspectorResponseDTO res = controller.getSingletons(new MockHttpServletRequest());
        assertNotNull(res);
        assertEquals(res.getResult().getSchemas().size(), 0);
        assertEquals(res.getResult().getSchematrons().size(), 0);
    }

}
