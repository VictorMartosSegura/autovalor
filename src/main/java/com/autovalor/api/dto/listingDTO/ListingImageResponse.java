package com.autovalor.api.dto.listingDTO;

import com.autovalor.api.model.ListingImage;
import java.time.OffsetDateTime;

public record ListingImageResponse(
        Long id,
        Long listingId,
        String fileName,
        String url,
        String contentType,
        Long sizeBytes,
        OffsetDateTime createdAt
) {
    public static ListingImageResponse from(ListingImage image) {
        return new ListingImageResponse(
                image.getId(),
                image.getListing().getId(),
                image.getFileName(),
                image.getUrl(),
                image.getContentType(),
                image.getSizeBytes(),
                image.getCreatedAt()
        );
    }
}
