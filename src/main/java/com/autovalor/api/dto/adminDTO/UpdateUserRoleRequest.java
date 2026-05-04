package com.autovalor.api.dto.adminDTO;

import com.autovalor.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull Role role
) {
}
