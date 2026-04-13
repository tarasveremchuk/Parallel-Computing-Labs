package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.PagedResponse;
import com.iotplatform.model.DeviceCommand;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.CommandType;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DeviceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/v1/commands")
@RequiredArgsConstructor
@Tag(name = "Device Commands", description = "Send commands to IoT devices")
public class DeviceCommandController {

    private final DeviceCommandService commandService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Send command to device (ADMIN, OPERATOR)")
    public ResponseEntity<ApiResponse<DeviceCommand>> sendCommand(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        DeviceCommand command = commandService.sendCommand(
                UUID.fromString(body.get("deviceId")),
                CommandType.valueOf(body.get("commandType")),
                body.getOrDefault("payload", null),
                user.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(command));
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge command (simulate device response)")
    public ResponseEntity<ApiResponse<DeviceCommand>> acknowledge(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String response = body != null ? body.getOrDefault("response", "OK") : "OK";
        DeviceCommand command = commandService.acknowledge(id, response);
        return ResponseEntity.ok(ApiResponse.ok(command, "Command acknowledged"));
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Mark command as failed")
    public ResponseEntity<ApiResponse<DeviceCommand>> fail(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "Unknown error") : "Unknown error";
        DeviceCommand command = commandService.failCommand(id, reason);
        return ResponseEntity.ok(ApiResponse.ok(command, "Command marked as failed"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get command by ID")
    public ResponseEntity<ApiResponse<DeviceCommand>> getById(@PathVariable UUID id) {
        DeviceCommand command = commandService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(command));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get commands for a device (paginated)")
    public ResponseEntity<PagedResponse<DeviceCommand>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DeviceCommand> commands = commandService.getByDeviceId(deviceId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(PagedResponse.from(commands, Function.identity()));
    }
}