package com.autovalor.api.dto.adminDTO;

public record AdminStatsResponse(
        long totalUsers,
        long totalAdmins,
        long totalListings,
        long availableListings,
        long reservedListings,
        long soldListings,
        long hiddenListings,
        long totalFavorites,
        long totalContactMessages
) {
}
