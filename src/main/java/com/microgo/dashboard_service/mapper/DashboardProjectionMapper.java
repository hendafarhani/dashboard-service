package com.microgo.dashboard_service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microgo.dashboard_service.entity.RideRequestEntity;
import com.microgo.dashboard_service.domain.DashboardProjection;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardProjectionMapper {

    private static final String RIDE_REQUEST_TABLE = "RIDE_REQUEST";
    private static final String RIDE_REQUEST_DRIVER_OFFER_TABLE = "RIDE_REQUEST_DRIVER_OFFER";

    private final ObjectMapper objectMapper;

    public DashboardProjection mapRideRequestProjection(RideRequestEntity rideRequest) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("id", rideRequest.getId());
        data.put("identifier", rideRequest.getIdentifier());
        data.put("status", rideRequest.getStatus());
        putNullable(data, "acceptedRiderIdentifier", rideRequest.getAcceptedDriverIdentifier());
        putNullable(data, "acceptedDriverIdentifier", rideRequest.getAcceptedDriverIdentifier());
        putNullable(data, "providerIdentifier", rideRequest.getAcceptedDriverIdentifier());
        putNullable(data, "driverDisplayId", rideRequest.getAcceptedDriverDisplayId());
        putNullable(data, "acceptedAt", rideRequest.getAcceptedAt());
        return new DashboardProjection(RIDE_REQUEST_TABLE, data);
    }

    public DashboardProjection mapRideRequestOfferProjection(RideRequestDriverOfferProjection offer) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("id", offer.getId());
        data.put("rideRequestId", offer.getRideRequestId());
        data.put("riderIdentifier", offer.getRiderIdentifier());
        putNullable(data, "driverIdentifier", offer.getDriverIdentifier());
        putNullable(data, "providerIdentifier", offer.getDriverIdentifier());
        putNullable(data, "driverDisplayId", offer.getDriverDisplayId());
        data.put("notificationRound", offer.getNotificationRound());
        data.put("status", offer.getStatus());
        putNullable(data, "notifiedAt", offer.getNotifiedAt());
        putNullable(data, "respondedAt", offer.getRespondedAt());
        return new DashboardProjection(RIDE_REQUEST_DRIVER_OFFER_TABLE, data);
    }

    private void putNullable(ObjectNode node, String fieldName, Object value) {
        if (valueIsMissing(value)) {
            node.putNull(fieldName);
            return;
        }
        node.put(fieldName, value.toString());
    }

    private boolean valueIsMissing(Object value) {
        return value == null;
    }
}
