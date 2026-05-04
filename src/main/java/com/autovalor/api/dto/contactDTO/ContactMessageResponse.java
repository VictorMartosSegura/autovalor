package com.autovalor.api.dto.contactDTO;

import com.autovalor.api.model.ContactMessage;
import java.time.OffsetDateTime;

public record ContactMessageResponse(
        Long id,
        Long listingId,
        String listingTitle,
        Long senderUserId,
        String contactName,
        String contactEmail,
        String contactPhone,
        String message,
        OffsetDateTime createdAt
) {
    public static ContactMessageResponse from(ContactMessage contactMessage) {
        Long senderUserId = contactMessage.getSenderUser() == null
                ? null
                : contactMessage.getSenderUser().getId();

        return new ContactMessageResponse(
                contactMessage.getId(),
                contactMessage.getListing().getId(),
                contactMessage.getListing().getTitle(),
                senderUserId,
                contactMessage.getContactName(),
                contactMessage.getContactEmail(),
                contactMessage.getContactPhone(),
                contactMessage.getMessage(),
                contactMessage.getCreatedAt()
        );
    }
}
