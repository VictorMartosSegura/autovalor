package com.autovalor.api.dto.chatDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "Message content is required")
        @Size(max = 2000, message = "Message content is too long")
        String content
) {
}
