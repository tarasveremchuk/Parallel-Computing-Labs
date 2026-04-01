package com.iotplatform.dto.response;

import com.iotplatform.model.DeviceAccess;
import com.iotplatform.model.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAccessResponse {

    private UUID id;
    private UUID deviceId;
    private UUID userId;
    private String username;
    private Permission permission;
    private UUID grantedBy;
    private LocalDateTime grantedAt;

    public static DeviceAccessResponse fromModel(DeviceAccess access, String username) {
        return DeviceAccessResponse.builder()
                .id(access.getId())
                .deviceId(access.getDeviceId())
                .userId(access.getUserId())
                .username(username)
                .permission(access.getPermission())
                .grantedBy(access.getGrantedBy())
                .grantedAt(access.getGrantedAt())
                .build();
    }
}