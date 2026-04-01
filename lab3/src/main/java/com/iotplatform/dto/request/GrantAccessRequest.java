package com.iotplatform.dto.request;

import com.iotplatform.model.enums.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantAccessRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Permission is required")
    private Permission permission;
}