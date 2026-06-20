package com.microgo.dashboard_service.service;

import com.microgo.dashboard_service.domain.RideDashboardMessage;

public interface DashboardStreamingService {

    String RIDE_REQUESTS_DESTINATION_PREFIX = "/topic/ride-requests";

    void streamRideEvent(RideDashboardMessage message);
}
