package it.finanze.sanita.fse2.ms.gtw.validator.config.audit.exclusions;

import javax.servlet.http.HttpServletRequest;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.ms.gtw.validator.config.audit.AuditExclusion;

@Component
public class SingletonExclusion implements AuditExclusion {
 
    @Override
    public boolean verify(String uri, HttpServletRequest req) {
        boolean skip = false;
        // Skip check if uri is null or swagger not enabled
        if(uri != null) {
            skip = uri.startsWith("/v1/singletons");
        }
        return skip;
    }
}
