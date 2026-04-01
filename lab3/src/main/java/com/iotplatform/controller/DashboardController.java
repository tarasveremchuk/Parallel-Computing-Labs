package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.DashboardResponse;
import com.iotplatform.model.User;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.DashboardService;
import com.iotplatform.service.DeviceAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "System-wide analytics and device health")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;
    private final DeviceAccessService accessService;

    @GetMapping
    @Operation(summary = "Get dashboard with statistics for accessible devices")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        List<UUID> deviceIds = accessService.getAccessibleDeviceIds(currentUser.getId());
        DashboardResponse dashboard = dashboardService.getDashboard(deviceIds);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }
}