package com.iotplatform.device.controller;

import com.iotplatform.device.dto.response.ApiResponse;
import com.iotplatform.device.dto.response.PagedResponse;
import com.iotplatform.device.model.ThresholdRule;
import com.iotplatform.device.model.enums.MetricType;
import com.iotplatform.device.service.ThresholdRuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@RestController @RequestMapping("/v1/rules") @RequiredArgsConstructor
@Tag(name = "Threshold Rules")
public class ThresholdRuleController {

    private final ThresholdRuleService ruleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ThresholdRule>> create(@RequestBody Map<String, String> body) {
        ThresholdRule rule = ruleService.createRule(
                MetricType.valueOf(body.get("metricType")),
                body.containsKey("minValue") ? Double.parseDouble(body.get("minValue")) : null,
                body.containsKey("maxValue") ? Double.parseDouble(body.get("maxValue")) : null,
                body.get("description"),
                body.containsKey("deviceId") ? UUID.fromString(body.get("deviceId")) : null,
                null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(rule));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ThresholdRule>> getAll(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                ruleService.getAllRules(PageRequest.of(page, size, Sort.by("createdAt").descending())),
                Function.identity()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ThresholdRule>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getRuleById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ThresholdRule>> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        ThresholdRule rule = ruleService.updateRule(id,
                body.containsKey("metricType") ? MetricType.valueOf((String) body.get("metricType")) : null,
                body.containsKey("minValue") ? Double.parseDouble(body.get("minValue").toString()) : null,
                body.containsKey("maxValue") ? Double.parseDouble(body.get("maxValue").toString()) : null,
                (String) body.get("description"),
                body.containsKey("active") ? (Boolean) body.get("active") : null);
        return ResponseEntity.ok(ApiResponse.ok(rule, "Rule updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Rule deleted"));
    }
}