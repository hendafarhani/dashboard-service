package com.microgo.dashboard_service.domain;

import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record OutboxEventEnvelope(
        Long eventId,
        String eventType,
        OffsetDateTime eventTimestamp,
        String rideRequestIdentifier,
        String requesterId,
        String riderId,
        String rideStatus,
        JsonNode payload
) {
}
