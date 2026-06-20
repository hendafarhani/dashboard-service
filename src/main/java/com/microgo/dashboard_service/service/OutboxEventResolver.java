package com.microgo.dashboard_service.service;

import com.microgo.dashboard_service.domain.ResolvedDashboardEvent;

import java.util.Optional;

public interface OutboxEventResolver {

    Optional<ResolvedDashboardEvent> resolve(String message);
}
