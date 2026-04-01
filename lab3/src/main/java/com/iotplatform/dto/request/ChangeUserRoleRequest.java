package com.iotplatform.dto.request;

import com.iotplatform.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;
}