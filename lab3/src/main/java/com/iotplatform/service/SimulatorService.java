package com.iotplatform.service;

import com.iotplatform.dto.request.CreateTelemetryRequest;
import com.iotplatform.dto.response.SimulationResponse;
import com.iotplatform.model.Device;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.enums.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulatorService {

    private final DeviceService deviceService;
    private final TelemetryService telemetryService;

    private final Random random = new Random();

    @Transactional
    public SimulationResponse runSimulation(int readingsPerDevice) {
        long start = System.currentTimeMillis();

        List<Device> devices = deviceService.getAllDevices(
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        int totalReadings = 0;
        int anomalies = 0;

        for (Device device : devices) {
            for (int i = 0; i < readingsPerDevice; i++) {
                MetricType metric = getRandomMetricForDevice(device);
                double value = generateValue(metric);

                CreateTelemetryRequest request = CreateTelemetryRequest.builder()
                        .deviceId(device.getId())
                        .metricType(metric)
                        .value(value)
                        .build();

                TelemetryReading reading = telemetryService.ingestTelemetry(request);
                totalReadings++;
                if (reading.isAnomaly()) {
                    anomalies++;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - start;

        log.info("Simulation complete: {} readings, {} anomalies in {}ms",
                totalReadings, anomalies, elapsed);

        return SimulationResponse.builder()
                .totalReadingsGenerated(totalReadings)
                .anomaliesDetected(anomalies)
                .alertsCreated(anomalies)
                .executionTimeMs(elapsed)
                .summary(String.format("Generated %d readings across %d devices. " +
                                "Detected %d anomalies (%.1f%% anomaly rate).",
                        totalReadings, devices.size(), anomalies,
                        totalReadings > 0 ? (double) anomalies / totalReadings * 100 : 0))
                .build();
    }

    private MetricType getRandomMetricForDevice(Device device) {
        MetricType[] metrics = switch (device.getType()) {
            case SENSOR -> new MetricType[]{
                    MetricType.TEMPERATURE, MetricType.HUMIDITY, MetricType.VOLTAGE};
            case ROUTER -> new MetricType[]{
                    MetricType.CPU_USAGE, MetricType.MEMORY_USAGE, MetricType.NETWORK_TRAFFIC};
            case GATEWAY -> new MetricType[]{
                    MetricType.CPU_USAGE, MetricType.MEMORY_USAGE, MetricType.TEMPERATURE, MetricType.NETWORK_TRAFFIC};
            case ACTUATOR -> new MetricType[]{
                    MetricType.VOLTAGE, MetricType.CPU_USAGE};
            case CAMERA -> new MetricType[]{
                    MetricType.CPU_USAGE, MetricType.MEMORY_USAGE, MetricType.DISK_USAGE};
        };
        return metrics[random.nextInt(metrics.length)];
    }

    private double generateValue(MetricType metric) {
        boolean makeAnomaly = random.nextDouble() < 0.15;

        return switch (metric) {
            case TEMPERATURE -> makeAnomaly ? randomInRange(90, 200) : randomInRange(15, 75);
            case CPU_USAGE -> makeAnomaly ? randomInRange(92, 100) : randomInRange(5, 80);
            case MEMORY_USAGE -> makeAnomaly ? randomInRange(96, 100) : randomInRange(20, 85);
            case NETWORK_TRAFFIC -> makeAnomaly ? randomInRange(1100, 5000) : randomInRange(10, 800);
            case VOLTAGE -> makeAnomaly ? randomInRange(0.5, 2.5) : randomInRange(3.3, 5.0);
            case HUMIDITY -> makeAnomaly ? randomInRange(92, 100) : randomInRange(20, 80);
            case DISK_USAGE -> randomInRange(10, 85);
        };
    }

    private double randomInRange(double min, double max) {
        double value = min + (max - min) * random.nextDouble();
        return Math.round(value * 100.0) / 100.0;
    }
}