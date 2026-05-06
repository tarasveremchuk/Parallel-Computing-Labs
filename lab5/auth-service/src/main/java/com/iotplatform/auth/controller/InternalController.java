package com.iotplatform.auth.controller;

import com.iotplatform.auth.dto.response.UserResponse;
import com.iotplatform.auth.model.User;
import com.iotplatform.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;
    @GetMapping("/users/search")
    public ResponseEntity<UserResponse> searchByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(UserResponse.fromModel(user));
    }
    @GetMapping("/users/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.existsById(id));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.fromModel(user));
    }
}