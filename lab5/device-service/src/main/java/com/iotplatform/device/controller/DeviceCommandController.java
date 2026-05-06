package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.dto.response.PagedResponse;
import com.iotplatform.device.model.DeviceCommand;
import com.iotplatform.device.model.enums.CommandType;
import com.iotplatform.device.service.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/commands") @RequiredArgsConstructor
public class DeviceCommandController {

    private final DeviceCommandService commandService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceCommand>> send(@RequestBody Map<String, String> body) {
        DeviceCommand cmd = commandService.sendCommand(
                UUID.fromString(body.get("deviceId")),
                CommandType.valueOf(body.get("commandType")),
                body.getOrDefault("payload", null),
                UUID.fromString(body.getOrDefault("sentBy", "00000000-0000-0000-0000-000000000001")));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(cmd));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<PagedResponse<DeviceCommand>> getByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                commandService.getByDevice(deviceId, PageRequest.of(page, size, Sort.by("createdAt").descending())),
                Function.identity()));
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<DeviceCommand>> acknowledge(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String response = body != null ? body.getOrDefault("response", "OK") : "OK";
        return ResponseEntity.ok(ApiResponse.ok(commandService.acknowledge(id, response), "Acknowledged"));
    }
}