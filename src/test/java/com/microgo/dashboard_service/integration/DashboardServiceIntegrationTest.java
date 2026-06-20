package com.microgo.dashboard_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgo.dashboard_service.entity.EventOutboxEntity;
import com.microgo.dashboard_service.entity.RideRequestDriverOfferEntity;
import com.microgo.dashboard_service.entity.RideRequestEntity;
import com.microgo.dashboard_service.domain.DashboardAckMessage;
import com.microgo.dashboard_service.domain.RideDashboardMessage;
import com.microgo.dashboard_service.repository.EventOutboxRepository;
import com.microgo.dashboard_service.repository.RideRequestDriverOfferRepository;
import com.microgo.dashboard_service.repository.RideRequestRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@Testcontainers(disabledWithoutDocker = true)
class DashboardServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @LocalServerPort
    private int port;

    @Autowired
    private EventOutboxRepository eventOutboxRepository;

    @Autowired
    private RideRequestRepository rideRequestRepository;

    @Autowired
    private RideRequestDriverOfferRepository rideRequestDriverOfferRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${dashboard.service.event-topic}")
    private String eventTopic;

    @Value("${dashboard.service.ack-topic}")
    private String ackTopic;

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void cleanState() {
        eventOutboxRepository.deleteAll();
        rideRequestRepository.deleteAll();
    }

    @Test
    void consumesRideEventStreamsWebSocketPayloadAndPublishesAck() throws Exception {
        RideRequestEntity rideRequest = new RideRequestEntity();
        rideRequest.setId(42L);
        rideRequest.setIdentifier("ride-dashboard-1");
        rideRequest.setStatus("ACCEPTED");
        rideRequest.setAcceptedRiderIdentifier("rider-dashboard-1");
        rideRequest.setAcceptedDriverDisplayId("DRV-RIDER-DASHBOARD-1");
        rideRequest.setAcceptedAt(OffsetDateTime.parse("2026-06-18T10:01:00Z"));
        rideRequestRepository.save(rideRequest);

        EventOutboxEntity outboxEvent = new EventOutboxEntity();
        outboxEvent.setId(101L);
        outboxEvent.setEventType("REQUEST_ACCEPTED");
        outboxEvent.setRideRequestId(42L);
        outboxEvent.setRideRequestIdentifier("ride-dashboard-1");
        outboxEvent.setRequesterId("user-dashboard-1");
        outboxEvent.setRiderId(null);
        outboxEvent.setPayload("""
                {"eventType":"REQUEST_ACCEPTED","rideStatus":"ACCEPTED","rideRequestId":42,"rideRequestIdentifier":"ride-dashboard-1","requesterId":"user-dashboard-1","driverIdentifier":"rider-dashboard-1","riderId":"rider-dashboard-1"}
                """);
        outboxEvent.setCreatedAt(OffsetDateTime.parse("2026-06-18T10:00:00Z"));
        outboxEvent.setUpdatedAt(OffsetDateTime.parse("2026-06-18T10:00:00Z"));
        eventOutboxRepository.save(outboxEvent);

        BlockingQueue<RideDashboardMessage> receivedMessages = new LinkedBlockingQueue<>();
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        )));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession session = stompClient
                .connectAsync("http://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        session.subscribe("/topic/ride-requests/ride-dashboard-1", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RideDashboardMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((RideDashboardMessage) payload);
            }
        });

        kafkaTemplate.send(eventTopic, "ride-dashboard-1", """
                {"eventId":101,"eventType":"REQUEST_ACCEPTED","eventTimestamp":"2026-06-18T10:00:00Z","rideRequestIdentifier":"ride-dashboard-1","requesterId":"user-dashboard-1","riderId":"rider-dashboard-1","rideStatus":"ACCEPTED","payload":{"rideStatus":"ACCEPTED"}}
                """).get(10, TimeUnit.SECONDS);

        RideDashboardMessage received = receivedMessages.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> ackRecord = consumeOneAck("101");

        assertThat(received).isNotNull();
        assertThat(received.eventId()).isEqualTo(101L);
        assertThat(received.riderId()).isEqualTo("rider-dashboard-1");
        assertThat(received.providerIdentifier()).isEqualTo("rider-dashboard-1");
        assertThat(received.driverDisplayId()).isEqualTo("DRV-RIDER-DASHBOARD-1");
        assertThat(received.sourceTable()).isEqualTo("RIDE_REQUEST");
        assertThat(received.data().get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(received.data().get("acceptedRiderIdentifier").asText()).isEqualTo("rider-dashboard-1");
        assertThat(received.data().get("providerIdentifier").asText()).isEqualTo("rider-dashboard-1");

        DashboardAckMessage ackMessage = objectMapper.readValue(ackRecord.value(), DashboardAckMessage.class);
        assertThat(ackMessage.eventId()).isEqualTo(101L);
        assertThat(ackMessage.status()).isEqualTo("WEBSOCKET_PUBLISHED");
        assertThat(ackMessage.service()).isEqualTo("dashboard-service");

        session.disconnect();
        stompClient.stop();
    }

    @Test
    void readsDriverOfferProjectionFromCanonicalDriverTable() {
        jdbcTemplate.execute("""
                create table if not exists driver (
                    id bigint primary key,
                    identifier varchar(255),
                    driver_identifier varchar(255),
                    driver_display_id varchar(255)
                )
                """);
        jdbcTemplate.update(
                "insert into driver (id, identifier, driver_identifier, driver_display_id) values (?, ?, ?, ?)",
                7L,
                "legacy-rider-7",
                "driver-7",
                "DRV-DRIVER-7");

        RideRequestDriverOfferEntity offer = new RideRequestDriverOfferEntity();
        offer.setId(70L);
        offer.setRideRequestId(42L);
        offer.setRiderId(7L);
        offer.setNotificationRound(1);
        offer.setNotifiedAt(OffsetDateTime.parse("2026-06-18T10:00:00Z"));
        offer.setStatus("NOTIFIED");
        rideRequestDriverOfferRepository.saveAndFlush(offer);

        var projection = rideRequestDriverOfferRepository
                .findProjectionByRideRequestIdAndRiderIdentifier(42L, "driver-7");

        assertThat(projection).isPresent();
        assertThat(projection.orElseThrow().getRiderIdentifier()).isEqualTo("legacy-rider-7");
        assertThat(projection.orElseThrow().getDriverIdentifier()).isEqualTo("driver-7");
        assertThat(projection.orElseThrow().getDriverDisplayId()).isEqualTo("DRV-DRIVER-7");
        assertThat(projection.orElseThrow().getNotifiedAt())
                .isEqualTo(OffsetDateTime.parse("2026-06-18T10:00:00Z").toLocalDateTime());
    }

    private ConsumerRecord<String, String> consumeOneAck(String expectedKey) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties())) {
            consumer.subscribe(List.of(ackTopic));
            long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500)).records(ackTopic)) {
                    if (expectedKey.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        throw new AssertionError("Expected acknowledgement for key " + expectedKey);
    }

    private Properties consumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dashboard-ack-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }
}
