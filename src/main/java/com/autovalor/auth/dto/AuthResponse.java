package com.autovalor.auth.dto;

import com.autovalor.user.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
    public AuthResponse(String token, Long userId, String name, String email, Role role) {
        this(token, "Bearer", userId, name, email, role);
    }
}
