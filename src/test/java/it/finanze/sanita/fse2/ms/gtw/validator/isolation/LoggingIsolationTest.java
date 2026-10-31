package it.finanze.sanita.fse2.ms.gtw.validator.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.*;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.finanze.sanita.fse2.ms.gtw.validator.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.validator.enums.*;
import it.finanze.sanita.fse2.ms.gtw.validator.logging.*;
import it.finanze.sanita.fse2.ms.gtw.validator.service.IConfigSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(LoggerHelper.class)
@TestPropertySource(properties = {"log.kafka-log.enable=true", "spring.application.name=validator-isolation"})
class LoggingIsolationTest {
    @Autowired private LoggerHelper logger;
    @MockitoBean private IConfigClient configClient;
    @MockitoBean private IConfigSRV config;

    @Test void structuredLogsIncludeWorkflowOperationSeverityAndCachedGateway() throws Exception {
        when(config.isControlLogPersistenceEnable()).thenReturn(true);
        when(configClient.getGatewayName()).thenReturn("gateway");
        Logger target = (Logger) org.slf4j.LoggerFactory.getLogger("kafka-logger");
        Level previous = target.getLevel();
        ListAppender<ILoggingEvent> captured = new ListAppender<>();
        captured.start(); target.addAppender(captured); target.setLevel(Level.TRACE);
        try {
            Date start = new Date(1);
            logger.trace("workflow", "trace", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
            logger.debug("workflow", "debug", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
            logger.info("workflow", "info", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
            logger.warn("workflow", "warn", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.WARN, start, WarnLogEnum.INVALID_CODE);
            logger.error("workflow", "error", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.KO, start, ErrorLogEnum.INVALID_VERSION);
            assertEquals(5, captured.list.size());
            for (ILoggingEvent event : captured.list) {
                var json = new ObjectMapper().readTree(event.getFormattedMessage());
                assertEquals("workflow", json.get("workflow_instance_id").asText());
                assertEquals("gateway", json.get("gateway_name").asText());
                assertEquals("validator-isolation", json.get("microservice_name").asText());
                assertTrue(json.hasNonNull("op_timestamp_start"));
            }
            verify(configClient, times(1)).getGatewayName();
            assertEquals(WarnLogEnum.INVALID_CODE.getCode(), new ObjectMapper().readTree(captured.list.get(3).getFormattedMessage()).get("op_warning").asText());
            assertEquals(ErrorLogEnum.INVALID_VERSION.getCode(), new ObjectMapper().readTree(captured.list.get(4).getFormattedMessage()).get("op_error").asText());
        } finally { target.detachAppender(captured); target.setLevel(previous); captured.stop(); }
    }

    @Test void disabledPersistenceSkipsAllStructuredLoggingWork() {
        Date start = new Date();
        logger.trace("w", "m", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
        logger.debug("w", "m", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
        logger.info("w", "m", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.OK, start);
        logger.warn("w", "m", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.WARN, start, WarnLogEnum.INVALID_CODE);
        logger.error("w", "m", OperationLogEnum.TERMINOLOGY_VALIDATION, ResultLogEnum.KO, start, ErrorLogEnum.INVALID_VERSION);
        verifyNoInteractions(configClient);
    }

    private static class TestAppender extends KafkaAppender<ILoggingEvent> {
        final Producer<byte[], byte[]> producer;
        int creations;
        TestAppender(Producer<byte[], byte[]> producer) { this.producer = producer; }
        @Override protected Producer<byte[], byte[]> createProducer() { creations++; return producer; }
    }

    @SuppressWarnings("unchecked")
    private TestAppender configured(Producer<byte[], byte[]> producer) {
        TestAppender appender = new TestAppender(producer);
        appender.setContext(new LoggerContext());
        appender.setName("isolation");
        appender.addProducerConfig("bootstrap.servers=unused:9092");
        appender.addProducerConfig("invalid-property-without-equals");
        appender.setTopic("logs");
        Encoder<ILoggingEvent> encoder = mock(Encoder.class);
        when(encoder.encode(any())).thenReturn(new byte[]{1, 2});
        appender.setEncoder(encoder);
        return appender;
    }

    private LoggingEvent event(String name) {
        LoggingEvent event = new LoggingEvent();
        event.setLoggerName(name); event.setTimeStamp(123L);
        return event;
    }

    @Test void appenderValidatesPrerequisitesBeforeStarting() {
        KafkaAppender<ILoggingEvent> appender = new KafkaAppender<>();
        appender.setContext(new LoggerContext());
        appender.start();
        assertFalse(appender.isStarted());
        assertFalse(appender.getContext().getStatusManager().getCopyOfStatusList().isEmpty());
    }

    @Test @SuppressWarnings("unchecked")
    void lazilyCreatesProducerEncodesRecordAndClosesOnStop() {
        Producer<byte[], byte[]> producer = mock(Producer.class);
        TestAppender appender = configured(producer);
        appender.setPartition(-1); appender.start();
        assertEquals(0, appender.creations);
        assertTrue(appender.isAppendTimestamp());
        appender.doAppend(event("application"));
        verify(producer).send(argThat(r -> r.topic().equals("logs") && r.partition() == null
                && r.timestamp().equals(123L) && java.util.Arrays.equals(r.value(), new byte[]{1, 2})), any(Callback.class));
        appender.doAppend(event("application"));
        assertEquals(1, appender.creations);
        appender.stop(); verify(producer).close();
    }

    @Test @SuppressWarnings("unchecked")
    void defersKafkaInternalEventsAndUsesConfiguredKeyPartitionAndDelivery() {
        Producer<byte[], byte[]> producer = mock(Producer.class);
        TestAppender appender = configured(producer);
        appender.setPartition(2); appender.setAppendTimestamp(false);
        appender.setKeyingStrategy(e -> new byte[]{9});
        DeliveryStrategy strategy = mock(DeliveryStrategy.class);
        appender.setDeliveryStrategy(strategy); appender.start();
        ILoggingEvent deferred = event("org.apache.kafka.clients.NetworkClient");
        appender.doAppend(deferred); verifyNoInteractions(strategy);
        ILoggingEvent normal = event("application"); appender.doAppend(normal);
        verify(strategy).send(eq(producer), argThat(r -> r.timestamp() == null && r.partition() == 2
                && java.util.Arrays.equals((byte[]) r.key(), new byte[]{9})), eq(deferred), any());
        verify(strategy).send(eq(producer), any(), eq(normal), any());
        appender.stop();
    }

    @Test void failedProducerFallsBackToAttachedAppenderAndSupportsDetachment() {
        TestAppender appender = configured(null);
        ListAppender<ILoggingEvent> fallback = new ListAppender<>();
        fallback.setName("fallback"); fallback.start();
        appender.addAppender(fallback);
        assertSame(fallback, appender.getAppender("fallback"));
        assertTrue(appender.isAttached(fallback));
        assertTrue(appender.iteratorForAppenders().hasNext());
        appender.start();
        ILoggingEvent event = event("application"); appender.doAppend(event);
        assertEquals(java.util.List.of(event), fallback.list);
        assertTrue(appender.detachAppender("fallback"));
        appender.addAppender(fallback); assertTrue(appender.detachAppender(fallback));
        appender.addAppender(fallback); appender.detachAndStopAllAppenders();
        assertFalse(fallback.isStarted()); appender.stop();
    }

    @Test @SuppressWarnings("unchecked")
    void shutdownFailureDoesNotEscape() {
        Producer<byte[], byte[]> producer = mock(Producer.class);
        doThrow(new KafkaException("shutdown")).when(producer).close();
        TestAppender appender = configured(producer); appender.start(); appender.doAppend(event("application"));
        assertDoesNotThrow(appender::stop);
    }

    @Test void hostnameStrategyUsesStableHostKeyAndNoKeyStrategyReturnsNull() {
        HostNameKeyingStrategy strategy = new HostNameKeyingStrategy();
        ContextBase context = mock(ContextBase.class);
        when(context.getStatusManager()).thenReturn(new BasicStatusManager());
        strategy.setContext(context); assertNull(strategy.createKey("event"));
        strategy.setContext(context);
        when(context.getProperty(CoreConstants.HOSTNAME_KEY)).thenReturn("test-host"); strategy.setContext(context);
        assertArrayEquals(ByteBuffer.allocate(4).putInt("test-host".hashCode()).array(), strategy.createKey("event"));
        strategy.start(); assertTrue(strategy.isStarted()); strategy.stop();
        assertNull(new NoKeyKeyingStrategy().createKey("event"));
    }

    @Test @SuppressWarnings("unchecked")
    void asynchronousDeliveryReportsCallbackAndImmediateFailures() {
        Producer<String, String> producer = mock(Producer.class);
        ProducerRecord<String, String> record = new ProducerRecord<>("topic", "payload");
        FailedDeliveryCallback<String> callback = mock(FailedDeliveryCallback.class);
        AsynchronousDeliveryStrategy strategy = new AsynchronousDeliveryStrategy();
        doAnswer(inv -> { ((Callback) inv.getArgument(1)).onCompletion(null, null); return null; }).when(producer).send(eq(record), any());
        assertTrue(strategy.send(producer, record, "event", callback)); verifyNoInteractions(callback);
        RuntimeException failure = new RuntimeException("send failed");
        doAnswer(inv -> { ((Callback) inv.getArgument(1)).onCompletion(null, failure); return null; }).when(producer).send(eq(record), any());
        assertTrue(strategy.send(producer, record, "event", callback)); verify(callback).onFailedDelivery("event", failure);
        org.apache.kafka.common.errors.TimeoutException timeout = new org.apache.kafka.common.errors.TimeoutException("timeout");
        when(producer.send(eq(record), any())).thenThrow(timeout);
        assertFalse(strategy.send(producer, record, "event", callback)); verify(callback).onFailedDelivery("event", timeout);
    }

    @ParameterizedTest @ValueSource(longs = {-1, 0, 10}) @SuppressWarnings("unchecked")
    void blockingDeliveryHonorsTimeout(long timeout) throws Exception {
        Producer<String, String> producer = mock(Producer.class);
        Future<RecordMetadata> future = mock(Future.class);
        ProducerRecord<String, String> record = new ProducerRecord<>("topic", "payload");
        when(producer.send(record)).thenReturn(future);
        BlockingDeliveryStrategy strategy = new BlockingDeliveryStrategy(); strategy.setTimeout(timeout);
        assertEquals(timeout, strategy.getTimeout());
        assertTrue(strategy.send(producer, record, "event", (e, failure) -> fail(failure)));
        if (timeout > 0) verify(future).get(timeout, TimeUnit.MILLISECONDS);
        else if (timeout == 0) verify(future).get();
        else verifyNoInteractions(future);
    }

    @Test @SuppressWarnings("unchecked")
    void blockingDeliveryDistinguishesInterruptionAndDeliveryFailure() throws Exception {
        Producer<String, String> producer = mock(Producer.class);
        Future<RecordMetadata> future = mock(Future.class);
        ProducerRecord<String, String> record = new ProducerRecord<>("topic", "payload");
        FailedDeliveryCallback<String> callback = mock(FailedDeliveryCallback.class);
        when(producer.send(record)).thenReturn(future);
        when(future.get()).thenThrow(new InterruptedException("interrupted"));
        BlockingDeliveryStrategy strategy = new BlockingDeliveryStrategy();
        assertFalse(strategy.send(producer, record, "event", callback)); verifyNoInteractions(callback);
        ExecutionException failure = new ExecutionException(new RuntimeException("offline"));
        doThrow(failure).when(future).get();
        assertFalse(strategy.send(producer, record, "event", callback)); verify(callback).onFailedDelivery("event", failure);
    }
}
