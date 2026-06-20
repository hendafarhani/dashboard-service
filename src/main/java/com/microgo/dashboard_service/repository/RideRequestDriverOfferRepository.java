package com.microgo.dashboard_service.repository;

import com.microgo.dashboard_service.entity.RideRequestDriverOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RideRequestDriverOfferRepository extends JpaRepository<RideRequestDriverOfferEntity, Long> {

    @Query(value = """
            select offer.id as id,
                   offer.ride_request_id as rideRequestId,
                   offer.notification_round as notificationRound,
                   offer.notified_at as notifiedAt,
                   offer.status as status,
                   offer.responded_at as respondedAt,
                   rider.identifier as riderIdentifier,
                   coalesce(rider.driver_identifier, rider.identifier) as driverIdentifier,
                   rider.driver_display_id as driverDisplayId
            from ride_request_driver_offer offer
            join driver rider on rider.id = offer.rider_id
            where offer.ride_request_id = :rideRequestId
              and coalesce(rider.driver_identifier, rider.identifier) = :riderIdentifier
            """, nativeQuery = true)
    Optional<RideRequestDriverOfferProjection> findProjectionByRideRequestIdAndRiderIdentifier(
            @Param("rideRequestId") Long rideRequestId,
            @Param("riderIdentifier") String riderIdentifier
    );
}
