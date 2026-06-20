package com.microgo.dashboard_service.kafka.configuration;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@EnableKafka
@Configuration
public class KafkaAdminConfiguration {

    @Bean
    public KafkaAdmin kafkaAdmin(@Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    @Bean
    public NewTopic dashboardEventsTopic(DashboardServiceProperties properties) {
        return new NewTopic(properties.eventTopic(), properties.eventTopicPartitions(), properties.replicationFactor());
    }

    @Bean
    public NewTopic dashboardAckTopic(DashboardServiceProperties properties) {
        return new NewTopic(properties.ackTopic(), properties.ackTopicPartitions(), properties.replicationFactor());
    }
}
