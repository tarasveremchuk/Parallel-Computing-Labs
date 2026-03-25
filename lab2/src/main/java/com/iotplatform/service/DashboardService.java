package com.iotplatform.service;

import com.iotplatform.dto.response.DashboardResponse;
import com.iotplatform.model.Alert;
import com.iotplatform.model.Device;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.enums.AlertSeverity;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.AlertRepository;
import com.iotplatform.repository.DeviceRepository;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;

    public DashboardResponse getDashboard() {
        List<Device> devices = deviceRepository.findAll();
        List<TelemetryReading> readings = telemetryRepository.findAll();
        List<Alert> alerts = alertRepository.findAll();

        long totalAnomalies = readings.stream().filter(TelemetryReading::isAnomaly).count();

        Map<String, Long> alertsBySeverity = alerts.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getSeverity().name(),
                        Collectors.counting()));

        Map<String, DashboardResponse.DeviceHealth> healthMap = new LinkedHashMap<>();
        for (Device device : devices) {
            List<TelemetryReading> deviceReadings = readings.stream()
                    .filter(r -> r.getDeviceId().equals(device.getId()))
                    .toList();

            long deviceAnomalies = deviceReadings.stream()
                    .filter(TelemetryReading::isAnomaly).count();

            double healthScore = deviceReadings.isEmpty() ? 100.0
                    : Math.round((1.0 - (double) deviceAnomalies / deviceReadings.size()) * 100.0 * 10) / 10.0;

            Double lastTemp = deviceReadings.stream()
                    .filter(r -> r.getMetricType() == MetricType.TEMPERATURE)
                    .max(Comparator.comparing(TelemetryReading::getTimestamp))
                    .map(TelemetryReading::getValue).orElse(null);

            Double lastCpu = deviceReadings.stream()
                    .filter(r -> r.getMetricType() == MetricType.CPU_USAGE)
                    .max(Comparator.comparing(TelemetryReading::getTimestamp))
                    .map(TelemetryReading::getValue).orElse(null);

            healthMap.put(device.getId().toString(), DashboardResponse.DeviceHealth.builder()
                    .deviceName(device.getName())
                    .totalReadings(deviceReadings.size())
                    .anomalyCount(deviceAnomalies)
                    .healthScore(healthScore)
                    .lastTemperature(lastTemp)
                    .lastCpuUsage(lastCpu)
                    .build());
        }

        return DashboardResponse.builder()
                .totalDevices(devices.size())
                .onlineDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.ONLINE).count())
                .offlineDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.OFFLINE).count())
                .errorDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.ERROR).count())
                .totalReadings(readings.size())
                .totalAnomalies(totalAnomalies)
                .anomalyRate(readings.isEmpty() ? 0 : Math.round((double) totalAnomalies / readings.size() * 100.0 * 10) / 10.0)
                .totalAlerts(alerts.size())
                .unresolvedAlerts(alerts.stream().filter(a -> !a.isResolved()).count())
                .alertsBySeverity(alertsBySeverity)
                .deviceHealthMap(healthMap)
                .build();
    }
}