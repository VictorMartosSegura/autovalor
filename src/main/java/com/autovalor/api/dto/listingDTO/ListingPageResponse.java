package com.autovalor.api.dto.listingDTO;

import java.util.List;

public record ListingPageResponse(
        List<ListingResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
