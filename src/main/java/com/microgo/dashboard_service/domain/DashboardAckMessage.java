package com.microgo.dashboard_service.domain;

import java.time.OffsetDateTime;

public record DashboardAckMessage(
        Long eventId,
        String status,
        OffsetDateTime processedAt,
        String service
) {
}
