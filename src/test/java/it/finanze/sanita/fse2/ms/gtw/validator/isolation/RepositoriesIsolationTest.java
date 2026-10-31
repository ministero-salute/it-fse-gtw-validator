package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.mockito.ArgumentCaptor;

import it.finanze.sanita.fse2.ms.gtw.validator.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.*;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.ms.gtw.validator.repository.mongo.impl.*;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({SchemaRepo.class, SchematronRepo.class, DictionaryRepo.class,
        TerminologyRepo.class, EngineRepo.class, AuditRepo.class})
class RepositoriesIsolationTest {
    @Autowired private SchemaRepo schemas;
    @Autowired private SchematronRepo schematrons;
    @Autowired private DictionaryRepo dictionaries;
    @Autowired private TerminologyRepo terms;
    @Autowired private EngineRepo engines;
    @Autowired private AuditRepo audits;
    @MockitoBean private MongoTemplate mongo;

    static Stream<Arguments> schemaQueries() {
        return Stream.of(
                Arguments.of("children", new Document("root_schema", false).append("type_id_extension", "v1").append("deleted", false), new Document()),
                Arguments.of("father", new Document("root_schema", true).append("type_id_extension", "v1").append("deleted", false), new Document()),
                Arguments.of("fatherLast", new Document("root_schema", true).append("deleted", false), new Document()),
                Arguments.of("name", new Document("name_schema", "CDA.xsd").append("type_id_extension", "v1").append("deleted", false), new Document()),
                Arguments.of("version", new Document("type_id_extension", "v1").append("deleted", false), new Document()),
                Arguments.of("latest", new Document("type_id_extension", "v1").append("deleted", false), new Document("last_update_date", -1)),
                Arguments.of("updated", new Document("type_id_extension", "v1").append("last_update_date", new Document("$gt", new Date(1))).append("deleted", false), new Document("last_update_date", -1)));
    }

    private Object schemaLookup(String operation) {
        return switch (operation) {
            case "children" -> schemas.findChildrenXsd("v1");
            case "father" -> schemas.findFatherXsd("v1");
            case "fatherLast" -> schemas.findFatherLastVersionXsd();
            case "name" -> schemas.findByNameAndVersion("CDA.xsd", "v1");
            case "version" -> schemas.findByVersion("v1");
            case "latest" -> schemas.findGtLastUpdate("v1");
            case "updated" -> schemas.findByExtensionAndLastUpdateDate("v1", new Date(1));
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private boolean multiple(String operation) {
        return List.of("children", "version", "updated").contains(operation);
    }

    @ParameterizedTest
    @MethodSource("schemaQueries")
    void buildsSchemaQueriesAndReturnsMongoResults(String operation, Document expected, Document sort) {
        SchemaETY entity = new SchemaETY();
        when(mongo.find(any(Query.class), eq(SchemaETY.class))).thenReturn(List.of(entity));
        when(mongo.findOne(any(Query.class), eq(SchemaETY.class))).thenReturn(entity);
        assertEquals(multiple(operation) ? List.of(entity) : entity, schemaLookup(operation));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        if (multiple(operation)) verify(mongo).find(query.capture(), eq(SchemaETY.class));
        else verify(mongo).findOne(query.capture(), eq(SchemaETY.class));
        assertEquals(expected, query.getValue().getQueryObject());
        assertEquals(sort, new Document(query.getValue().getSortObject()));
    }

    @ParameterizedTest
    @MethodSource("schemaQueries")
    void wrapsEverySchemaLookupFailure(String operation, Document ignored, Document ignoredSort) {
        RuntimeException failure = new RuntimeException("Mongo unavailable");
        when(mongo.find(any(Query.class), eq(SchemaETY.class))).thenThrow(failure);
        when(mongo.findOne(any(Query.class), eq(SchemaETY.class))).thenThrow(failure);
        BusinessException wrapped = assertThrows(BusinessException.class, () -> schemaLookup(operation));
        if (!operation.equals("version")) assertSame(failure, wrapped.getCause());
        else assertTrue(wrapped.getMessage().contains("Mongo unavailable"));
    }

    @Test
    void filtersDeletedDictionaries() {
        DictionaryETY entity = new DictionaryETY();
        when(mongo.find(any(Query.class), eq(DictionaryETY.class))).thenReturn(List.of(entity));
        assertEquals(List.of(entity), dictionaries.getCodeSystems());
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).find(query.capture(), eq(DictionaryETY.class));
        assertEquals(new Document("deleted", false), query.getValue().getQueryObject());
    }

    @Test
    void sortsSchematronsAndFiltersByMinimumVersion() {
        SchematronETY entity = new SchematronETY();
        when(mongo.findOne(any(Query.class), eq(SchematronETY.class))).thenReturn(entity);
        assertSame(entity, schematrons.findByRootAndSystem("root", "system"));
        assertSame(entity, schematrons.findGreaterOne("root", "system", "1"));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo, times(2)).findOne(query.capture(), eq(SchematronETY.class));
        assertEquals(new Document("template_id_root", "root").append("system", "system").append("deleted", false),
                query.getAllValues().get(0).getQueryObject());
        assertEquals(new Document("$gt", "1"), query.getAllValues().get(1).getQueryObject().get("version"));
        query.getAllValues().forEach(q -> assertEquals(new Document("version", -1), q.getSortObject()));
    }

    @Test
    void returnsCodesWithOptionalVersionAndPreservesEmptyResults() {
        TerminologyETY entity = new TerminologyETY();
        entity.setCode("A");
        when(mongo.find(any(Query.class), eq(TerminologyETY.class))).thenReturn(List.of(entity));
        assertEquals(List.of("A"), terms.findAllCodesExists("system", List.of("A")));
        assertEquals(List.of("A"), terms.findAllCodesExistsForVersion("system", null, List.of("A")));
        assertEquals(List.of("A"), terms.findAllCodesExistsForVersion("system", "v1", List.of("A")));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo, times(3)).find(query.capture(), eq(TerminologyETY.class));
        Document expected = new Document("system", "system").append("code", new Document("$in", List.of("A"))).append("deleted", false);
        assertEquals(expected, query.getAllValues().get(0).getQueryObject());
        assertEquals(expected, query.getAllValues().get(1).getQueryObject());
        assertEquals(new Document(expected).append("version", "v1"), query.getAllValues().get(2).getQueryObject());
        when(mongo.find(any(Query.class), eq(TerminologyETY.class))).thenReturn(List.of());
        assertTrue(terms.findAllCodesExists("system", List.of("A")).isEmpty());
        assertTrue(terms.findAllCodesExistsForVersion("system", "v1", List.of("A")).isEmpty());
    }

    @Test
    void buildsExistenceQueries() {
        when(mongo.exists(any(Query.class), eq(TerminologyETY.class))).thenReturn(true);
        assertTrue(terms.allCodesExists("system", List.of("A")));
        assertTrue(terms.existBySystemAndCode("system", "A"));
        assertTrue(terms.existBySystemAndNotCodes("system", List.of("A")));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo, times(3)).exists(query.capture(), eq(TerminologyETY.class));
        assertEquals(new Document("system", "system").append("code", new Document("$in", List.of("A"))).append("deleted", false), query.getAllValues().get(0).getQueryObject());
        assertEquals(new Document("system", "system").append("code", "A").append("deleted", false), query.getAllValues().get(1).getQueryObject());
        assertEquals(new Document("system", "system").append("code", new Document("$not", new Document("$in", List.of("A")))), query.getAllValues().get(2).getQueryObject());
    }

    @Test
    void wrapsDictionarySchematronAndTerminologyFailures() {
        RuntimeException failure = new RuntimeException("Mongo unavailable");
        when(mongo.find(any(Query.class), eq(DictionaryETY.class))).thenThrow(failure);
        when(mongo.findOne(any(Query.class), eq(SchematronETY.class))).thenThrow(failure);
        when(mongo.find(any(Query.class), eq(TerminologyETY.class))).thenThrow(failure);
        when(mongo.exists(any(Query.class), eq(TerminologyETY.class))).thenThrow(failure);
        List<Supplier<?>> calls = List.of(dictionaries::getCodeSystems,
                () -> schematrons.findByRootAndSystem("root", "system"),
                () -> schematrons.findGreaterOne("root", "system", "1"),
                () -> terms.allCodesExists("system", List.of("A")),
                () -> terms.findAllCodesExists("system", List.of("A")),
                () -> terms.findAllCodesExistsForVersion("system", "v1", List.of("A")),
                () -> terms.existBySystemAndCode("system", "A"),
                () -> terms.existBySystemAndNotCodes("system", List.of("A")));
        calls.forEach(call -> assertSame(failure, assertThrows(BusinessException.class, call::get).getCause()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrievesLatestAvailableEngineAndWrapsFailure() {
        EngineETY entity = new EngineETY();
        when(mongo.aggregate(any(TypedAggregation.class), eq(EngineETY.class)))
                .thenReturn(new AggregationResults<>(List.of(entity), new Document()));
        assertSame(entity, engines.getLatestEngine());
        ArgumentCaptor<TypedAggregation> query = ArgumentCaptor.forClass(TypedAggregation.class);
        verify(mongo).aggregate(query.capture(), eq(EngineETY.class));
        String pipeline = query.getValue().toString();
        assertTrue(pipeline.contains("last_sync"));
        assertTrue(pipeline.contains("available"));
        assertTrue(pipeline.contains("$limit"));
        RuntimeException failure = new RuntimeException("Mongo unavailable");
        when(mongo.aggregate(any(TypedAggregation.class), eq(EngineETY.class))).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, engines::getLatestEngine).getCause());
    }

    @Test
    void insertsAuditAndWrapsFailure() {
        AuditETY audit = new AuditETY();
        audits.save(audit);
        verify(mongo).insert(audit);
        RuntimeException failure = new RuntimeException("Mongo unavailable");
        when(mongo.insert(audit)).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, () -> audits.save(audit)).getCause());
    }
}
