package com.autovalor.api.mapper;

import com.autovalor.api.dto.userDTO.UserResponse;
import com.autovalor.api.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        if (u == null) return null;

        UserResponse dto = new UserResponse();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setName(u.getName());
        return dto;
    }
}
