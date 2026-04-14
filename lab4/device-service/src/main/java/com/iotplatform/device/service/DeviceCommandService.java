package com.iotplatform.device.service;

import com.iotplatform.device.exception.InvalidOperationException;
import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.DeviceCommand;
import com.iotplatform.device.model.enums.CommandStatus;
import com.iotplatform.device.model.enums.CommandType;
import com.iotplatform.device.repository.DeviceCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class DeviceCommandService {

    private final DeviceCommandRepository commandRepository;

    @Transactional
    public DeviceCommand sendCommand(UUID deviceId, CommandType type, String payload, UUID sentBy) {
        long pending = commandRepository.countByDeviceIdAndStatus(deviceId, CommandStatus.PENDING);
        if (pending >= 5) throw new InvalidOperationException("Too many pending commands (max 5)");
        DeviceCommand cmd = DeviceCommand.builder()
                .deviceId(deviceId).commandType(type).status(CommandStatus.PENDING)
                .payload(payload).sentBy(sentBy).sentAt(LocalDateTime.now()).build();
        return commandRepository.save(cmd);
    }

    @Transactional
    public DeviceCommand acknowledge(UUID id, String response) {
        DeviceCommand cmd = commandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceCommand", "id", id));
        cmd.setStatus(CommandStatus.ACKNOWLEDGED);
        cmd.setResponse(response);
        cmd.setAcknowledgedAt(LocalDateTime.now());
        return commandRepository.save(cmd);
    }

    public Page<DeviceCommand> getByDevice(UUID deviceId, Pageable pageable) {
        return commandRepository.findByDeviceId(deviceId, pageable);
    }
}