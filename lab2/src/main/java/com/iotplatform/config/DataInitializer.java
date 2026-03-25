package com.iotplatform.config;

import com.iotplatform.dto.request.CreateDeviceRequest;
import com.iotplatform.dto.request.CreateThresholdRuleRequest;
import com.iotplatform.model.User;
import com.iotplatform.model.enums.DeviceType;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.model.enums.UserRole;
import com.iotplatform.repository.UserRepository;
import com.iotplatform.service.DeviceService;
import com.iotplatform.service.ThresholdRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeviceService deviceService;
    private final ThresholdRuleService ruleService;

    @Override
    public void run(String... args) {
        log.info("=== Initializing demo data ===");

        // --- Default admin user ---
        User admin = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@iotplatform.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(admin);
        log.info("Default admin created: admin / admin123");

        // --- Demo devices ---
        deviceService.registerDevice(CreateDeviceRequest.builder()
                .name("Temperature Sensor A1")
                .type(DeviceType.SENSOR)
                .location("Server Room - Rack 1")
                .firmwareVersion("2.1.3")
                .build());

        deviceService.registerDevice(CreateDeviceRequest.builder()
                .name("Network Router NR-01")
                .type(DeviceType.ROUTER)
                .location("Floor 2 - Network Closet")
                .firmwareVersion("5.0.1")
                .build());

        deviceService.registerDevice(CreateDeviceRequest.builder()
                .name("Edge Gateway GW-Alpha")
                .type(DeviceType.GATEWAY)
                .location("Building A - Entrance")
                .firmwareVersion("1.8.0")
                .build());

        deviceService.registerDevice(CreateDeviceRequest.builder()
                .name("Humidity Sensor H-03")
                .type(DeviceType.SENSOR)
                .location("Warehouse Zone C")
                .firmwareVersion("1.2.0")
                .build());

        // --- Global threshold rules ---
        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.TEMPERATURE)
                .minValue(-10.0)
                .maxValue(85.0)
                .description("Global temperature threshold: -10°C to 85°C")
                .build());

        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.CPU_USAGE)
                .maxValue(90.0)
                .description("Global CPU usage threshold: max 90%")
                .build());

        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.MEMORY_USAGE)
                .maxValue(95.0)
                .description("Global memory usage threshold: max 95%")
                .build());

        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.VOLTAGE)
                .minValue(3.0)
                .maxValue(5.5)
                .description("Global voltage threshold: 3.0V to 5.5V")
                .build());

        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.NETWORK_TRAFFIC)
                .maxValue(1000.0)
                .description("Global network traffic threshold: max 1000 Mbps")
                .build());

        ruleService.createRule(CreateThresholdRuleRequest.builder()
                .metricType(MetricType.HUMIDITY)
                .minValue(10.0)
                .maxValue(90.0)
                .description("Global humidity threshold: 10% to 90%")
                .build());

        log.info("=== Demo data initialized: {} devices, {} rules ===",
                deviceService.getDeviceCount(), ruleService.getAllRules().size());
    }
}