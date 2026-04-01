package com.iotplatform.controller;

import com.iotplatform.dto.request.CreateThresholdRuleRequest;
import com.iotplatform.dto.request.UpdateThresholdRuleRequest;
import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.dto.response.PagedResponse;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.User;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.ThresholdRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Threshold Rules", description = "Anomaly detection threshold configuration")
public class ThresholdRuleController {

    private final ThresholdRuleService ruleService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a new threshold rule (ADMIN only)")
    public ResponseEntity<ApiResponse<ThresholdRule>> createRule(
            @Valid @RequestBody CreateThresholdRuleRequest request,
            Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        ThresholdRule rule = ruleService.createRule(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(rule));
    }

    @GetMapping
    @Operation(summary = "Get all threshold rules (paginated)")
    public ResponseEntity<PagedResponse<ThresholdRule>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<ThresholdRule> rules = ruleService.getAllRules(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(PagedResponse.from(rules, Function.identity()));
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
        return ResponseEntity.ok(ApiResponse.ok(rules,
                "Found " + rules.size() + " rule(s)"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update threshold rule (ADMIN only)")
    public ResponseEntity<ApiResponse<ThresholdRule>> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateThresholdRuleRequest request) {
        ThresholdRule rule = ruleService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.ok(rule, "Rule updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete threshold rule - soft delete (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Rule deleted"));
    }
}