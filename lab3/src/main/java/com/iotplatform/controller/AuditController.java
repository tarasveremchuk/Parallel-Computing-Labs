package com.iotplatform.controller;

import com.iotplatform.dto.response.PagedResponse;
import com.iotplatform.model.AuditLog;
import com.iotplatform.model.enums.AuditAction;
import com.iotplatform.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "System activity audit trail")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get audit logs (paginated, filtered)")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs;

        if (userId != null) {
            logs = auditService.getByUserId(userId, pageable);
        } else if (action != null) {
            logs = auditService.getByAction(action, pageable);
        } else if (entityType != null && entityId != null) {
            logs = auditService.getByEntity(entityType, entityId, pageable);
        } else {
            logs = auditService.getAll(pageable);
        }

        return ResponseEntity.ok(PagedResponse.from(logs, Function.identity()));
    }
}