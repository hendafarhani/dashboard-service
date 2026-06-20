package com.microgo.dashboard_service.domain;

import com.fasterxml.jackson.databind.JsonNode;

public record RideDashboardMessage(
        Long eventId,
        String eventType,
        String rideRequestIdentifier,
        String requesterId,
        String riderId,
        String providerIdentifier,
        String driverDisplayId,
        String rideStatus,
        String sourceTable,
        JsonNode payload,
        JsonNode data
) {
}
