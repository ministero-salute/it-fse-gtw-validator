package it.finanze.sanita.fse2.ms.gtw.validator.controller.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.validator.controller.ISingletonInspectorCTL;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchemaSingletonInfo;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SchematronSingletonInfo;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.SingletonInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.dto.response.SingletonInspectorResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchemaValidatorSingleton;
import it.finanze.sanita.fse2.ms.gtw.validator.singleton.SchematronValidatorSingleton;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *	Singleton Inspector controller.
 */
@Slf4j
@RestController
public class SingletonInspectorCTL extends AbstractCTL implements ISingletonInspectorCTL {
    
    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 5923354311703725437L;

	@Override
    public SingletonInspectorResponseDTO getSingletons(HttpServletRequest request) {

        Map<String, SchemaValidatorSingleton> schemaInstance = SchemaValidatorSingleton.getMapInstance();
        Map<String, SchematronValidatorSingleton> schematronInstance = SchematronValidatorSingleton.getMapInstance();

        List<SchematronSingletonInfo> schematrons = new ArrayList<>();
        List<SchemaSingletonInfo> schemas = new ArrayList<>();

        if (schemaInstance != null) {
            for (Map.Entry<String, SchemaValidatorSingleton> entry : schemaInstance.entrySet()) {
                schemas.add(SchemaSingletonInfo.builder()
                        .version(entry.getValue().getVersion())
                        .dataUltimoAggiornamento(entry.getValue().getDataUltimoAggiornamento())
                        .build());
            }
        }

        if (schematronInstance != null) {
            for (Map.Entry<String, SchematronValidatorSingleton> entry : schematronInstance.entrySet()) {
                schematrons.add(SchematronSingletonInfo.builder()
                        .templateIdExtension(entry.getValue().getTemplateIdExtension())
                        .dataUltimoAggiornamento(entry.getValue().getDataUltimoAggiornamento())
                        .build());
            }
        }

        SingletonInfoDTO out = SingletonInfoDTO.builder().schemas(schemas).schematrons(schematrons).build();

        return new SingletonInspectorResponseDTO(getLogTraceInfo(), out);

    }
    
    
}
