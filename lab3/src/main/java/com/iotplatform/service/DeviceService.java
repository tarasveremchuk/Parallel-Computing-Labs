package com.iotplatform.service;

import com.iotplatform.dto.request.CreateDeviceRequest;
import com.iotplatform.dto.request.UpdateDeviceRequest;
import com.iotplatform.dto.response.DeviceStatsResponse;
import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.Device;
import com.iotplatform.model.TelemetryReading;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.AlertRepository;
import com.iotplatform.repository.DeviceRepository;
import com.iotplatform.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final AlertRepository alertRepository;

    @Transactional
    public Device registerDevice(CreateDeviceRequest request, UUID ownerId) {
        if (deviceRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new DuplicateResourceException(
                    "Device with name '" + request.getName() + "' already exists");
        }

        Device device = Device.builder()
                .name(request.getName())
                .type(request.getType())
                .status(DeviceStatus.ONLINE)
                .location(request.getLocation())
                .firmwareVersion(request.getFirmwareVersion() != null
                        ? request.getFirmwareVersion() : "1.0.0")
                .ownerId(ownerId)
                .deleted(false)
                .build();

        Device saved = deviceRepository.save(device);
        log.info("Device registered: {} [{}] by user {}", saved.getName(), saved.getId(), ownerId);
        return saved;
    }

    public Page<Device> getAllDevices(Pageable pageable) {
        return deviceRepository.findByDeletedFalse(pageable);
    }

    public Page<Device> getAccessibleDevices(UUID userId, Pageable pageable) {
        return deviceRepository.findAccessibleDevices(userId, pageable);
    }

    public Device getDeviceById(UUID id) {
        return deviceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
    }

    public Page<Device> getDevicesByStatus(DeviceStatus status, Pageable pageable) {
        return deviceRepository.findByStatusAndDeletedFalse(status, pageable);
    }

    public Page<Device> getDevicesByType(DeviceType type, Pageable pageable) {
        return deviceRepository.findByTypeAndDeletedFalse(type, pageable);
    }

    public Page<Device> searchByLocation(String location, Pageable pageable) {
        return deviceRepository.findByLocationContainingIgnoreCaseAndDeletedFalse(location, pageable);
    }

    @Transactional
    public Device updateDevice(UUID id, UpdateDeviceRequest request) {
        Device device = getDeviceById(id);

        if (request.getName() != null) {
            if (!request.getName().equalsIgnoreCase(device.getName())
                    && deviceRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
                throw new DuplicateResourceException(
                        "Device with name '" + request.getName() + "' already exists");
            }
            device.setName(request.getName());
        }
        if (request.getType() != null) device.setType(request.getType());
        if (request.getLocation() != null) device.setLocation(request.getLocation());
        if (request.getFirmwareVersion() != null) device.setFirmwareVersion(request.getFirmwareVersion());

        deviceRepository.save(device);
        log.info("Device updated: {} [{}]", device.getName(), device.getId());
        return device;
    }

    @Transactional
    public void deleteDevice(UUID id) {
        Device device = getDeviceById(id);
        device.setDeleted(true);
        device.setStatus(DeviceStatus.OFFLINE);
        deviceRepository.save(device);
        log.info("Device soft-deleted: {} [{}]", device.getName(), device.getId());
    }

    @Transactional
    public Device heartbeat(UUID id) {
        Device device = getDeviceById(id);
        device.setLastSeenAt(LocalDateTime.now());
        if (device.getStatus() == DeviceStatus.OFFLINE || device.getStatus() == DeviceStatus.ERROR) {
            device.setStatus(DeviceStatus.ONLINE);
            log.info("Device back online: {} [{}]", device.getName(), device.getId());
        }
        deviceRepository.save(device);
        log.debug("Heartbeat received: {} [{}]", device.getName(), device.getId());
        return device;
    }

    @Transactional
    public Device updateStatus(UUID id, DeviceStatus status) {
        Device device = getDeviceById(id);
        DeviceStatus oldStatus = device.getStatus();
        device.setStatus(status);
        deviceRepository.save(device);
        log.info("Device status: {} [{}] {} -> {}", device.getName(), device.getId(), oldStatus, status);
        return device;
    }

    public DeviceStatsResponse getDeviceStats(UUID deviceId) {
        Device device = getDeviceById(deviceId);

        long totalReadings = telemetryRepository.countByDeviceId(deviceId);
        long anomalyCount = telemetryRepository.countByDeviceIdAndAnomalyTrue(deviceId);
        double healthScore = totalReadings == 0 ? 100.0
                : Math.round((1.0 - (double) anomalyCount / totalReadings) * 100.0 * 10) / 10.0;
        long unresolvedAlerts = alertRepository.countByDeviceId(deviceId);

        List<MetricType> metricTypes = telemetryRepository.findDistinctMetricTypesByDeviceId(deviceId);
        Map<MetricType, DeviceStatsResponse.MetricStats> metricsMap = new LinkedHashMap<>();

        for (MetricType metric : metricTypes) {
            Double avg = telemetryRepository.findAvgValueByDeviceIdAndMetricType(deviceId, metric);
            Double min = telemetryRepository.findMinValueByDeviceIdAndMetricType(deviceId, metric);
            Double max = telemetryRepository.findMaxValueByDeviceIdAndMetricType(deviceId, metric);
            long count = telemetryRepository.countByDeviceIdAndMetricType(deviceId, metric);

            List<TelemetryReading> lastReadings =
                    telemetryRepository.findTop1ByDeviceIdAndMetricTypeOrderByTimestampDesc(deviceId, metric);
            Double lastValue = lastReadings.isEmpty() ? null : lastReadings.get(0).getValue();

            metricsMap.put(metric, DeviceStatsResponse.MetricStats.builder()
                    .avg(avg != null ? Math.round(avg * 100.0) / 100.0 : null)
                    .min(min)
                    .max(max)
                    .lastValue(lastValue)
                    .readingCount(count)
                    .build());
        }

        return DeviceStatsResponse.builder()
                .deviceId(deviceId)
                .deviceName(device.getName())
                .totalReadings(totalReadings)
                .anomalyCount(anomalyCount)
                .healthScore(healthScore)
                .unresolvedAlerts(unresolvedAlerts)
                .metrics(metricsMap)
                .build();
    }

    public boolean existsById(UUID id) {
        return deviceRepository.existsByIdAndDeletedFalse(id);
    }

    public long getDeviceCount() {
        return deviceRepository.countByDeletedFalse();
    }
}