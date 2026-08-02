package com.microgo.dashboard_service.kafka.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This service's own Kafka identity and tuning.
 *
 * <p>Topic names deliberately live in {@link KafkaTopicProperties} instead.
 * Everything here must DIFFER from other services - Kafka delivers each message
 * to exactly one member of a consumer group, so sharing consumerGroupId with
 * another service would make Kafka see one application with two instances and
 * split the stream between them.
 */
@ConfigurationProperties(prefix = "dashboard.service")
public record DashboardServiceProperties(
        String consumerGroupId,
        String listenerId,
        Integer eventTopicPartitions,
        Integer ackTopicPartitions,
        Short replicationFactor
) {

    public DashboardServiceProperties {
        eventTopicPartitions = eventTopicPartitions == null ? 3 : eventTopicPartitions;
        ackTopicPartitions = ackTopicPartitions == null ? 3 : ackTopicPartitions;
        replicationFactor = replicationFactor == null ? 1 : replicationFactor;
    }
}
