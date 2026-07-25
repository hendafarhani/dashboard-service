package com.microgo.dashboard_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // Smoke test: verify the context boots without reaching Kafka. No broker
        // runs here, so keep the listener stopped and skip admin topic creation
        // to avoid ~30s of connection retries against a nonexistent broker.
        "dashboard.service.listener.auto-startup=false",
        "dashboard.service.admin.auto-create-topics=false"
})
class DashboardServiceApplicationIT {

    @Test
    void contextLoads() {
    }
}
