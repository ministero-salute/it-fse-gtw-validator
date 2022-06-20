package it.finanze.sanita.fse2.ms.gtw.validator.utility;

import it.finanze.sanita.fse2.ms.gtw.validator.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ErrorLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.ElasticLoggerHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = { Constants.ComponentScan.BASE })
@ActiveProfiles(Constants.Profile.TEST)
public class ElasticLoggerTest {

    @Autowired
    private ElasticLoggerHelper log;

    @Test
    void write() {
        log.debug("Test", OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, new Date(), new Date());
        log.info("Test", OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, new Date(), new Date());
        log.error("Test", OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, new Date(), new Date(), ErrorLogEnum.KO_VAL);
        log.trace("Test", OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, new Date(), new Date());
        log.warn("Test", OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, new Date(), new Date());
    }
}
