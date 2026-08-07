package com.microgo.dashboard_service.service;

import com.microgo.dashboard_service.configuration.WebSocketConfiguration;
import com.microgo.dashboard_service.domain.RideDashboardMessage;

public interface DashboardStreamingService {

    /**
     * Derived from {@link WebSocketConfiguration#TOPIC_PREFIX} on purpose.
     *
     * <p>That constant is the prefix the STOMP broker is registered to serve. This
     * is the prefix messages are addressed to. When the two were spelled out
     * independently, changing one left the service publishing to a destination the
     * broker no longer served - messages stopped arriving with nothing thrown and
     * every test still green.
     */
    String RIDE_REQUESTS_DESTINATION_PREFIX = WebSocketConfiguration.TOPIC_PREFIX + "/ride-requests";

    void streamRideEvent(RideDashboardMessage message);
}
