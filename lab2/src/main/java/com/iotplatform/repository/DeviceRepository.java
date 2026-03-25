package com.iotplatform.repository;

import com.iotplatform.model.Device;
import com.iotplatform.model.enums.DeviceStatus;
import com.iotplatform.model.enums.DeviceType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class DeviceRepository {

    private final Map<UUID, Device> storage = new ConcurrentHashMap<>();

    public Device save(Device device) {
        storage.put(device.getId(), device);
        return device;
    }

    public Optional<Device> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Device> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Device> findByStatus(DeviceStatus status) {
        return storage.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Device> findByType(DeviceType type) {
        return storage.values().stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Device> findByLocation(String location) {
        return storage.values().stream()
                .filter(d -> d.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }

    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }

    public void deleteById(UUID id) {
        storage.remove(id);
    }

    public long count() {
        return storage.size();
    }
}