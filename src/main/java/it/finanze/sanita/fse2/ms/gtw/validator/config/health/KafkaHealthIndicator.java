package it.finanze.sanita.fse2.ms.gtw.validator.config.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Override
    public Health health() {
    	Properties configProperties = new Properties();
    	configProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        try(AdminClient adminClient = AdminClient.create(configProperties)) {
            adminClient.listTopics().listings().get();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}