package com.autovalor.api.dto.favoriteDTO;

public record FavoriteStatusResponse(
        Long listingId,
        boolean favorite,
        long favoritesCount
) {
}
