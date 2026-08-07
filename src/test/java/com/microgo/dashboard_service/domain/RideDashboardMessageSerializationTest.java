package com.microgo.dashboard_service.domain;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RideDashboardMessageSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void writesJsonNodeFieldsAsJsonTrees() {
        String json = serializeThroughBrokerConverter(sampleMessage());
        JsonNode wire = objectMapper.readTree(json);

        assertThat(wire.path("data").path("status").asString(null)).isEqualTo("ACCEPTED");
        assertThat(wire.path("data").path("acceptedRiderIdentifier").asString(null)).isEqualTo("rider-7");
        assertThat(wire.path("payload").path("rideStatus").asString(null)).isEqualTo("ACCEPTED");
    }

    @Test
    void doesNotLeakJsonNodeInternalsOntoTheWire() {
        String json = serializeThroughBrokerConverter(sampleMessage());

        assertThat(json)
                .doesNotContain("nodeType")
                .doesNotContain("containerNode")
                .doesNotContain("valueNode")
                .doesNotContain("missingNode");
    }

    @Test
    void keepsScalarFieldsIntact() {
        JsonNode wire = objectMapper.readTree(serializeThroughBrokerConverter(sampleMessage()));

        assertThat(wire.path("eventId").asLong(0L)).isEqualTo(7L);
        assertThat(wire.path("rideRequestIdentifier").asString(null)).isEqualTo("ride-7");
        assertThat(wire.path("sourceTable").asString(null)).isEqualTo("RIDE_REQUEST");
    }

    private RideDashboardMessage sampleMessage() {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("rideStatus", "ACCEPTED");

        ObjectNode data = objectMapper.createObjectNode();
        data.put("status", "ACCEPTED");
        data.put("acceptedRiderIdentifier", "rider-7");

        return new RideDashboardMessage(
                7L,
                "REQUEST_ACCEPTED",
                "ride-7",
                "user-7",
                "rider-7",
                "rider-7",
                "DRV-RIDER-7",
                "ACCEPTED",
                "RIDE_REQUEST",
                payload,
                data
        );
    }

    /** Serializes exactly the way the STOMP message broker does for an outbound frame. */
    private String serializeThroughBrokerConverter(RideDashboardMessage message) {
        Message<?> frame = new JacksonJsonMessageConverter()
                .toMessage(message, new MessageHeaderAccessor().getMessageHeaders());
        assertThat(frame).isNotNull();
        return new String((byte[]) frame.getPayload(), StandardCharsets.UTF_8);
    }
}
