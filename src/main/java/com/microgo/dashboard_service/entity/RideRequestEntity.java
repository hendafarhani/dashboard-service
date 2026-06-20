package com.microgo.dashboard_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "RIDE_REQUEST")
public class RideRequestEntity {

    @Id
    private Long id;

    @Column(name = "identifier", unique = true)
    private String identifier;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "accepted_rider_identifier")
    private String acceptedDriverIdentifier;

    @Column(name = "accepted_driver_display_id")
    private String acceptedDriverDisplayId;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    public String getAcceptedRiderIdentifier() {
        return acceptedDriverIdentifier;
    }

    public void setAcceptedRiderIdentifier(String acceptedRiderIdentifier) {
        this.acceptedDriverIdentifier = acceptedRiderIdentifier;
    }
}
