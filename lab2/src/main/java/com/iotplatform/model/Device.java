package com.iotplatform.model;

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
public class Device {

    private UUID id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String location;
    private String firmwareVersion;
    private LocalDateTime registeredAt;
    private LocalDateTime lastSeenAt;
}