package com.microgo.dashboard_service.service;

import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.enums.RideRequestEventType;
import com.microgo.dashboard_service.domain.DashboardProjection;

public interface EventProjectionRouter {

    DashboardProjection route(RideRequestEventType eventType, EventOutboxEntity outboxEvent);
}
