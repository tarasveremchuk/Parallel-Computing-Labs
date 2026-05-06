package com.iotplatform.telemetry.controller;

import com.iotplatform.telemetry.dto.response.PagedResponse;
import com.iotplatform.telemetry.model.AuditLog;
import com.iotplatform.telemetry.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.function.Function;

@RestController @RequestMapping("/v1/audit") @RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<PagedResponse<AuditLog>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(PagedResponse.from(
                auditService.getAll(PageRequest.of(page, size, Sort.by("timestamp").descending())),
                Function.identity()));
    }
}