package com.autovalor.api.dto.adminDTO;

import com.autovalor.user.Role;
import com.autovalor.user.User;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Instant createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
