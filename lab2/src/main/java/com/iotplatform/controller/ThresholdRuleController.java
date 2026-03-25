package com.iotplatform.controller;

import com.iotplatform.dto.request.CreateThresholdRuleRequest;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.service.ThresholdRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Threshold Rules", description = "Anomaly detection threshold configuration")
public class ThresholdRuleController {

    private final ThresholdRuleService ruleService;

    @PostMapping
    @Operation(summary = "Create a new threshold rule (ADMIN only)")
    public ResponseEntity<ApiResponse<ThresholdRule>> createRule(
            @Valid @RequestBody CreateThresholdRuleRequest request) {
        ThresholdRule rule = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(rule));
    }

    @GetMapping
    @Operation(summary = "Get all threshold rules")
    public ResponseEntity<ApiResponse<List<ThresholdRule>>> getAllRules() {
        List<ThresholdRule> rules = ruleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.ok(rules,
                "Found " + rules.size() + " rule(s)"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get threshold rule by ID")
    public ResponseEntity<ApiResponse<ThresholdRule>> getRuleById(@PathVariable UUID id) {
        ThresholdRule rule = ruleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.ok(rule));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get rules for a specific device")
    public ResponseEntity<ApiResponse<List<ThresholdRule>>> getRulesByDevice(
            @PathVariable UUID deviceId) {
        List<ThresholdRule> rules = ruleService.getRulesByDeviceId(deviceId);
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }
}