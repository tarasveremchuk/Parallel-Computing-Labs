package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.SimulationResponse;
import com.iotplatform.service.SimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/simulator")
@RequiredArgsConstructor
@Tag(name = "Simulator", description = "IoT traffic simulation with anomaly injection")
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/run")
    @Operation(summary = "Run IoT simulation (ADMIN only)")
    public ResponseEntity<ApiResponse<SimulationResponse>> runSimulation(
            @RequestParam(defaultValue = "10") int readingsPerDevice) {
        SimulationResponse response = simulatorService.runSimulation(readingsPerDevice);
        return ResponseEntity.ok(ApiResponse.ok(response, "Simulation completed"));
    }
}