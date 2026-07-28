package com.microgo.dashboard_service.domain;

import tools.jackson.databind.JsonNode;

public record DashboardProjection(
        String sourceTable,
        JsonNode data
) {
}
