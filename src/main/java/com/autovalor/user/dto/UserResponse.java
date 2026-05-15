package com.autovalor.user.dto;

import com.autovalor.user.Role;
import com.autovalor.user.User;
import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt,
        String addressCountry,
        String addressCity,
        String addressLine,
        Double addressLatitude,
        Double addressLongitude
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getAddressCountry(),
                user.getAddressCity(),
                user.getAddressLine(),
                user.getAddressLatitude(),
                user.getAddressLongitude()
        );
    }
}
