package com.autovalor.api.dto.chatDTO;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime readAt,
        boolean sentByCurrentUser
) {
}
