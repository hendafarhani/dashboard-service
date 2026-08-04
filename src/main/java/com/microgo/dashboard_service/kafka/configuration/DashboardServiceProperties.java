package com.microgo.dashboard_service.kafka.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * This service's own Kafka identity and tuning.
 *
 * <p>Topic names deliberately live in {@link KafkaTopicProperties} instead.
 * Everything here must DIFFER from other services - Kafka delivers each message
 * to exactly one member of a consumer group, so sharing consumerGroupId with
 * another service would make Kafka see one application with two instances and
 * split the stream between them.
 *
 * <p>No component carries a Java default, deliberately. A default for a key that
 * centralized-config always sets is a second source of truth: the partition
 * counts and replication factor were written as {@code 3, 3, 1} here and as
 * {@code 3, 3, 1} in dashboard-service.properties - two repositories, nothing
 * comparing them. Worse, a default silently absorbs a deleted or misspelled key:
 * the service starts on the fallback and the config file has quietly stopped
 * being authoritative, with no error to notice.
 *
 * <p>With the defaults gone the value must come from configuration, a missing
 * key fails at startup naming the key, and the key must also appear in
 * src/test/resources/application.properties for the context to start - which
 * puts it on both sides of centralized-config's check-config-drift.py and brings
 * it under that check for free.
 */
@Validated
@ConfigurationProperties(prefix = "dashboard.service")
public record DashboardServiceProperties(

        @NotNull(message = "dashboard.service.consumer-group-id must be set")
        String consumerGroupId,

        @NotNull(message = "dashboard.service.listener-id must be set")
        String listenerId,

        @NotNull(message = "dashboard.service.event-topic-partitions must be set")
        Integer eventTopicPartitions,

        @NotNull(message = "dashboard.service.ack-topic-partitions must be set")
        Integer ackTopicPartitions,

        @NotNull(message = "dashboard.service.replication-factor must be set")
        Short replicationFactor
) {
}
