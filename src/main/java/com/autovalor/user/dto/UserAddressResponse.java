package com.autovalor.user.dto;

import com.autovalor.user.User;

public record UserAddressResponse(
        String country,
        String city,
        String address,
        Double latitude,
        Double longitude
) {
    public static UserAddressResponse from(User user) {
        return new UserAddressResponse(
                user.getAddressCountry(),
                user.getAddressCity(),
                user.getAddressLine(),
                user.getAddressLatitude(),
                user.getAddressLongitude()
        );
    }
}
