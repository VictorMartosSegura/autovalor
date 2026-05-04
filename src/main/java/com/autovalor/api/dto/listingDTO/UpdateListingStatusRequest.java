package com.autovalor.api.dto.listingDTO;

import com.autovalor.api.model.ListingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateListingStatusRequest(
        @NotNull ListingStatus status
) {
}
