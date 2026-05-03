package com.autovalor.common;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        Map<String, String> details
) {
    public ApiError(int status, String error) {
        this(Instant.now(), status, error, Map.of());
    }

    public ApiError(int status, String error, Map<String, String> details) {
        this(Instant.now(), status, error, details);
    }
}
