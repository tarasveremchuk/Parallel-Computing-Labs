package com.iotplatform.dto.request;

import com.iotplatform.model.enums.DeviceType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceRequest {

    @Size(min = 2, max = 100, message = "Device name must be between 2 and 100 characters")
    private String name;

    private DeviceType type;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 50, message = "Firmware version must not exceed 50 characters")
    private String firmwareVersion;
}