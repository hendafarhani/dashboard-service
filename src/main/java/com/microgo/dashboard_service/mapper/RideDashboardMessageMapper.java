package com.microgo.dashboard_service.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.domain.DashboardProjection;
import com.microgo.dashboard_service.domain.ResolvedDashboardEvent;
import com.microgo.dashboard_service.domain.RideDashboardMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RideDashboardMessageMapper {

    public RideDashboardMessage map(ResolvedDashboardEvent resolvedEvent, DashboardProjection projection) {
        EventOutboxEntity outboxEvent = resolvedEvent.outboxEvent();
        JsonNode projectionData = projection.data();
        JsonNode payload = resolvedEvent.payload();
        String resolvedDriverIdentifier = firstNonBlank(
                outboxEvent.getDriverIdentifier(),
                textOrNull(projectionData, "driverIdentifier"),
                textOrNull(projectionData, "acceptedDriverIdentifier"),
                textOrNull(projectionData, "providerIdentifier"),
                textOrNull(projectionData, "acceptedRiderIdentifier"),
                textOrNull(payload, "driverIdentifier"),
                textOrNull(payload, "riderId")
        );
        String resolvedDriverDisplayId = firstNonBlank(
                textOrNull(projectionData, "driverDisplayId"),
                textOrNull(payload, "driverDisplayId")
        );

        return new RideDashboardMessage(
                outboxEvent.getId(),
                outboxEvent.getEventType(),
                outboxEvent.getRideRequestIdentifier(),
                outboxEvent.getRequesterId(),
                resolvedDriverIdentifier,
                resolvedDriverIdentifier,
                resolvedDriverDisplayId,
                payload.path("rideStatus").asText(null),
                projection.sourceTable(),
                payload,
                projectionData
        );
    }

    private String textOrNull(JsonNode node, String fieldName) {
        if (node == null) {
            return null;
        }
        String value = node.path(fieldName).asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
