package com.iotplatform.controller;

import com.iotplatform.dto.response.ApiResponse;
import com.iotplatform.model.User;
import com.iotplatform.model.Webhook;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.service.AuthService;
import com.iotplatform.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Configure alert notification webhooks")
public class WebhookController {

    private final WebhookService webhookService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a webhook")
    public ResponseEntity<ApiResponse<Webhook>> create(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());

        AlertSeverity minSeverity = body.containsKey("minSeverity")
                ? AlertSeverity.valueOf(body.get("minSeverity")) : null;
        UUID deviceId = body.containsKey("deviceId")
                ? UUID.fromString(body.get("deviceId")) : null;

        Webhook webhook = webhookService.create(
                body.get("name"),
                body.get("url"),
                minSeverity,
                deviceId,
                user.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(webhook));
    }

    @GetMapping
    @Operation(summary = "Get my webhooks")
    public ResponseEntity<ApiResponse<List<Webhook>>> getMyWebhooks(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        List<Webhook> webhooks = webhookService.getByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(webhooks,
                "Found " + webhooks.size() + " webhook(s)"));
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Toggle webhook active/inactive")
    public ResponseEntity<ApiResponse<Webhook>> toggle(@PathVariable UUID id) {
        Webhook webhook = webhookService.toggle(id);
        return ResponseEntity.ok(ApiResponse.ok(webhook,
                "Webhook " + (webhook.isActive() ? "activated" : "deactivated")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a webhook")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        webhookService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Webhook deleted"));
    }
}