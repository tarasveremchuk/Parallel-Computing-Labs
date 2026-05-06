package com.iotplatform.device.config;

import com.iotplatform.device.model.Device;
import com.iotplatform.device.model.ThresholdRule;
import com.iotplatform.device.model.enums.DeviceStatus;
import com.iotplatform.device.model.enums.DeviceType;
import com.iotplatform.device.model.enums.MetricType;
import com.iotplatform.device.repository.DeviceRepository;
import com.iotplatform.device.repository.ThresholdRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j @Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DeviceRepository deviceRepository;
    private final ThresholdRuleRepository ruleRepository;

    // Use a fixed UUID as placeholder ownerId (matches admin in auth-service)
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    public void run(String... args) {
        if (deviceRepository.count() > 0) {
            log.info("=== Device DB already has data, skipping ===");
            return;
        }
        log.info("=== Initializing device data ===");

        deviceRepository.save(Device.builder().name("Temperature Sensor A1").type(DeviceType.SENSOR)
                .status(DeviceStatus.ONLINE).location("Server Room - Rack 1").firmwareVersion("2.1.3").ownerId(ADMIN_ID).build());
        deviceRepository.save(Device.builder().name("Network Router NR-01").type(DeviceType.ROUTER)
                .status(DeviceStatus.ONLINE).location("Floor 2 - Comms Room").firmwareVersion("5.0.1").ownerId(ADMIN_ID).build());
        deviceRepository.save(Device.builder().name("Gateway Hub GH-01").type(DeviceType.GATEWAY)
                .status(DeviceStatus.OFFLINE).location("Building B - Entrance").firmwareVersion("1.4.0").ownerId(ADMIN_ID).build());
        deviceRepository.save(Device.builder().name("Security Camera SC-01").type(DeviceType.CAMERA)
                .status(DeviceStatus.ONLINE).location("Parking Lot - North").firmwareVersion("3.2.0").ownerId(ADMIN_ID).build());
        log.info("4 devices created");

        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.TEMPERATURE).minValue(-10.0).maxValue(85.0).description("Global temperature threshold").active(true).build());
        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.CPU_USAGE).minValue(0.0).maxValue(90.0).description("Global CPU usage threshold").active(true).build());
        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.MEMORY_USAGE).minValue(0.0).maxValue(95.0).description("Global memory threshold").active(true).build());
        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.VOLTAGE).minValue(1.5).maxValue(14.0).description("Global voltage threshold").active(true).build());
        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.HUMIDITY).minValue(10.0).maxValue(80.0).description("Global humidity threshold").active(true).build());
        ruleRepository.save(ThresholdRule.builder().metricType(MetricType.NETWORK_TRAFFIC).minValue(0.0).maxValue(1000.0).description("Global network threshold").active(true).build());
        log.info("6 threshold rules created");

        log.info("=== Device data initialized ===");
    }
}