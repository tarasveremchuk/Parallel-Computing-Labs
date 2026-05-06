package com.iotplatform.telemetry.service;

import com.iotplatform.telemetry.client.DeviceServiceClient;
import com.iotplatform.telemetry.model.TelemetryReading;
import com.iotplatform.telemetry.model.enums.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j @Service @RequiredArgsConstructor
public class SimulatorService {

    private final TelemetryService telemetryService;
    private final DeviceServiceClient deviceServiceClient;
    private final Random random = new Random();

    public Map<String, Object> runSimulation(int readingsPerDevice) {
        long start = System.currentTimeMillis();
        int totalReadings = 0, anomalies = 0, alerts = 0;

        List<UUID> deviceIds = fetchDeviceIds();
        if (deviceIds.isEmpty()) {
            log.warn("No devices found for simulation");
            return Map.of("totalReadingsGenerated", 0, "anomaliesDetected", 0,
                    "alertsGenerated", 0, "executionTimeMs", 0, "error", "No devices found");
        }

        for (UUID deviceId : deviceIds) {
            MetricType[] metrics = MetricType.values();
            for (int i = 0; i < readingsPerDevice; i++) {
                MetricType metric = metrics[random.nextInt(metrics.length)];
                boolean shouldBeAnomaly = random.nextDouble() < 0.15;
                double value = generateValue(metric, shouldBeAnomaly);
                String unit = getUnit(metric);
                try {
                    TelemetryReading reading = telemetryService.ingest(deviceId, metric, value, unit);
                    totalReadings++;
                    if (reading.isAnomaly()) { anomalies++; alerts++; }
                } catch (Exception e) {
                    log.warn("Sim reading failed for device {}: {}", deviceId, e.getMessage());
                }
            }
        }

        long duration = System.currentTimeMillis() - start;
        log.info("Simulation: {} readings, {} anomalies, {}ms", totalReadings, anomalies, duration);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalReadingsGenerated", totalReadings);
        result.put("anomaliesDetected", anomalies);
        result.put("alertsGenerated", alerts);
        result.put("executionTimeMs", duration);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<UUID> fetchDeviceIds() {
        try {
            List<Map<String, Object>> devices = deviceServiceClient.getAllDevices();
            return devices.stream()
                    .map(d -> UUID.fromString((String) d.get("id")))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to fetch devices: {}", e.getMessage());
            return List.of();
        }
    }

    private double generateValue(MetricType metric, boolean anomaly) {
        return switch (metric) {
            case TEMPERATURE -> anomaly ? (random.nextBoolean() ? -20 + random.nextDouble() * 10 : 90 + random.nextDouble() * 20) : 20 + random.nextDouble() * 40;
            case CPU_USAGE -> anomaly ? 92 + random.nextDouble() * 8 : 10 + random.nextDouble() * 70;
            case MEMORY_USAGE -> anomaly ? 96 + random.nextDouble() * 4 : 20 + random.nextDouble() * 60;
            case VOLTAGE -> anomaly ? (random.nextBoolean() ? 0.5 + random.nextDouble() : 15 + random.nextDouble() * 3) : 3 + random.nextDouble() * 9;
            case HUMIDITY -> anomaly ? (random.nextBoolean() ? 2 + random.nextDouble() * 5 : 85 + random.nextDouble() * 15) : 30 + random.nextDouble() * 40;
            case NETWORK_TRAFFIC -> anomaly ? 1050 + random.nextDouble() * 500 : 50 + random.nextDouble() * 800;
            case DISK_USAGE -> anomaly ? 96 + random.nextDouble() * 4 : 15 + random.nextDouble() * 65;
        };
    }

    private String getUnit(MetricType metric) {
        return switch (metric) {
            case TEMPERATURE -> "°C";
            case CPU_USAGE, MEMORY_USAGE, HUMIDITY, DISK_USAGE -> "%";
            case VOLTAGE -> "V";
            case NETWORK_TRAFFIC -> "Mbps";
        };
    }
}