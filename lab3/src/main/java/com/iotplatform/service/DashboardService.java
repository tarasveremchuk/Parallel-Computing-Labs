package com.iotplatform.service;

import com.iotplatform.dto.response.DashboardResponse;
import com.iotplatform.model.Device;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.AlertRepository;
import com.iotplatform.repository.DeviceRepository;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;

    public DashboardResponse getDashboard(List<UUID> accessibleDeviceIds) {
        List<Device> devices = accessibleDeviceIds.stream()
                .map(id -> deviceRepository.findByIdAndDeletedFalse(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        long totalReadings = 0;
        long totalAnomalies = 0;

        Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
        List<Object[]> severityGroups = alertRepository.countBySeverityGrouped(accessibleDeviceIds);
        for (Object[] row : severityGroups) {
            alertsBySeverity.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, DashboardResponse.DeviceHealth> healthMap = new LinkedHashMap<>();
        for (Device device : devices) {
            long deviceReadings = telemetryRepository.countByDeviceId(device.getId());
            long deviceAnomalies = telemetryRepository.countByDeviceIdAndAnomalyTrue(device.getId());

            totalReadings += deviceReadings;
            totalAnomalies += deviceAnomalies;

            double healthScore = deviceReadings == 0 ? 100.0
                    : Math.round((1.0 - (double) deviceAnomalies / deviceReadings) * 100.0 * 10) / 10.0;

            var lastTempList = telemetryRepository
                    .findTop1ByDeviceIdAndMetricTypeOrderByTimestampDesc(device.getId(), MetricType.TEMPERATURE);
            Double lastTemp = lastTempList.isEmpty() ? null : lastTempList.get(0).getValue();

            var lastCpuList = telemetryRepository
                    .findTop1ByDeviceIdAndMetricTypeOrderByTimestampDesc(device.getId(), MetricType.CPU_USAGE);
            Double lastCpu = lastCpuList.isEmpty() ? null : lastCpuList.get(0).getValue();

            healthMap.put(device.getId().toString(), DashboardResponse.DeviceHealth.builder()
                    .deviceName(device.getName())
                    .totalReadings(deviceReadings)
                    .anomalyCount(deviceAnomalies)
                    .healthScore(healthScore)
                    .lastTemperature(lastTemp)
                    .lastCpuUsage(lastCpu)
                    .build());
        }

        long unresolvedAlerts = alertRepository.countByResolvedFalseAndDeviceIdIn(accessibleDeviceIds);

        return DashboardResponse.builder()
                .totalDevices(devices.size())
                .onlineDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.ONLINE).count())
                .offlineDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.OFFLINE).count())
                .errorDevices(devices.stream().filter(d -> d.getStatus() == DeviceStatus.ERROR).count())
                .totalReadings(totalReadings)
                .totalAnomalies(totalAnomalies)
                .anomalyRate(totalReadings == 0 ? 0
                        : Math.round((double) totalAnomalies / totalReadings * 100.0 * 10) / 10.0)
                .totalAlerts(unresolvedAlerts + (totalReadings > 0 ? totalAnomalies : 0))
                .unresolvedAlerts(unresolvedAlerts)
                .alertsBySeverity(alertsBySeverity)
                .deviceHealthMap(healthMap)
                .build();
    }
}