package com.iotplatform.telemetry.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "auth-service", url = "${services.auth-service.url}")
public interface AuthServiceClient {

    @GetMapping("/v1/internal/users/{id}/exists")
    Boolean userExists(@PathVariable UUID id);

    @GetMapping("/v1/internal/users/{id}")
    Map<String, Object> getUser(@PathVariable UUID id);
}