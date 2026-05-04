package com.autovalor.api.dto.favoriteDTO;

import com.autovalor.api.dto.listingDTO.ListingResponse;
import java.time.OffsetDateTime;

public record FavoriteResponse(
        Long id,
        Long listingId,
        ListingResponse listing,
        OffsetDateTime createdAt
) {
}
