package com.iotplatform.auth.dto.response;

import com.iotplatform.auth.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String token;
    private String username;
    private String email;
    private UserRole role;
}