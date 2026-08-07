package com.microgo.dashboard_service.kafka.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
