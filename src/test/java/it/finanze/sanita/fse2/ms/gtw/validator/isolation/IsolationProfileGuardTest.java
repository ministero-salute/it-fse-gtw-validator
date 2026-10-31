package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsolationProfileGuardTest {
    @Test
    void rawScanCannotImportIsolationFixturesWithoutTheirProfile() {
        String isolationPackage = getClass().getPackageName();
        for (String profile : new String[] {"default", "TEST"}) {
            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles(profile);
                int infrastructureBeans = context.getBeanDefinitionCount();
                context.scan(isolationPackage);
                context.refresh();
                assertEquals(infrastructureBeans, context.getBeanDefinitionCount());
            }
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles("ISOLATION");
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.setEnvironment(environment);
        assertEquals(6, scanner.findCandidateComponents(isolationPackage).size());
    }
}
