package com.autovalor.api.dto.chatDTO;

import java.time.OffsetDateTime;

public record ConversationResponse(
        Long id,
        Long listingId,
        String listingTitle,
        Long otherUserId,
        String otherUserName,
        String otherUserEmail,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        long unreadCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
