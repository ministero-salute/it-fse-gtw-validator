package it.finanze.sanita.fse2.ms.gtw.validator.service;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class UpdateSingletonSRVTest {

    @Autowired
    private IUpdateSingletonSRV service;

    @Test
    void oneRunScheduleTest() {
        assertDoesNotThrow(() -> service.updateSingletonInstance());
    }

}
