package com.autovalor.api.dto.errorDTO;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        return new ApiErrorResponse(
                status,
                error,
                message,
                path,
                OffsetDateTime.now(),
                fieldErrors
        );
    }
}
