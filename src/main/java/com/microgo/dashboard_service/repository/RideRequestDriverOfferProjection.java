package com.microgo.dashboard_service.repository;

import java.time.LocalDateTime;

public interface RideRequestDriverOfferProjection {

    Long getId();

    Long getRideRequestId();

    Integer getNotificationRound();

    LocalDateTime getNotifiedAt();

    String getStatus();

    LocalDateTime getRespondedAt();

    String getRiderIdentifier();

    String getDriverIdentifier();

    String getDriverDisplayId();
}
