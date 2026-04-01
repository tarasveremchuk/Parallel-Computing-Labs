package com.iotplatform.dto.response;

import com.iotplatform.model.Device;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
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
public class DeviceResponse {

    private UUID id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String location;
    private String firmwareVersion;
    private UUID ownerId;
    private LocalDateTime registeredAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastSeenAt;

    public static DeviceResponse fromModel(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .status(device.getStatus())
                .location(device.getLocation())
                .firmwareVersion(device.getFirmwareVersion())
                .ownerId(device.getOwnerId())
                .registeredAt(device.getRegisteredAt())
                .updatedAt(device.getUpdatedAt())
                .lastSeenAt(device.getLastSeenAt())
                .build();
    }
}