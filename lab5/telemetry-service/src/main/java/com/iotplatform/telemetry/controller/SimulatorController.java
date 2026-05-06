package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.dto.response.ApiResponse;
import com.iotplatform.telemetry.service.SimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/v1/simulator") @RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> run(
            @RequestParam(defaultValue = "10") int readingsPerDevice) {
        return ResponseEntity.ok(ApiResponse.ok(simulatorService.runSimulation(readingsPerDevice)));
    }
}