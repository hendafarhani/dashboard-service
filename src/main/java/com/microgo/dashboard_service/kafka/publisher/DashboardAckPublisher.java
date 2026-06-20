package com.microgo.dashboard_service.kafka.publisher;

public interface DashboardAckPublisher {

    void publishWebsocketPublished(Long eventId);
}
