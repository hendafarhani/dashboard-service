package com.microgo.dashboard_service.kafka.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Topic names, bound from the fleet-wide microgo.topics.* keys defined once in
 * centralized-config/application.properties.
 *
 * A topic name
 * is an agreement with whoever is on the other end of the channel - here,
 * outbox-publisher-service, which produces to the same two topics - so it must
 * be identical in both services. Everything left in DashboardServiceProperties
 * is the opposite: this service's own consumer group id, listener id and
 * partition counts, which must NOT be shared.
 */
@ConfigurationProperties(prefix = "microgo.topics")
public record KafkaTopicProperties(
        String rideRequestEvents,
        String rideRequestEventsAcks
) {
}
