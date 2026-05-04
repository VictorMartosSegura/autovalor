package com.autovalor.api.controller.rest;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name:autovalor}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "application", applicationName,
                "timestamp", OffsetDateTime.now()
        );
    }
}
