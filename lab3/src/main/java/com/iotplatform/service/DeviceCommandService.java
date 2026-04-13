package com.iotplatform.service;

import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.DeviceCommand;
import com.iotplatform.model.enums.CommandStatus;
import com.iotplatform.model.enums.CommandType;
import com.iotplatform.repository.DeviceCommandRepository;
import com.iotplatform.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceCommand sendCommand(UUID deviceId, CommandType type, String payload, UUID sentBy) {
        if (!deviceRepository.existsByIdAndDeletedFalse(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }

        long pending = commandRepository.countByDeviceIdAndStatus(deviceId, CommandStatus.PENDING);
        if (pending >= 5) {
            throw new InvalidOperationException("Device has too many pending commands (max 5)");
        }

        DeviceCommand command = DeviceCommand.builder()
                .deviceId(deviceId)
                .commandType(type)
                .status(CommandStatus.PENDING)
                .payload(payload)
                .sentBy(sentBy)
                .sentAt(LocalDateTime.now())
                .build();

        DeviceCommand saved = commandRepository.save(command);
        log.info("Command {} sent to device {} by user {}", type, deviceId, sentBy);
        return saved;
    }

    @Transactional
    public DeviceCommand acknowledge(UUID commandId, String response) {
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceCommand", "id", commandId));

        if (command.getStatus() != CommandStatus.PENDING && command.getStatus() != CommandStatus.SENT) {
            throw new InvalidOperationException("Command is not in a state that can be acknowledged");
        }

        command.setStatus(CommandStatus.ACKNOWLEDGED);
        command.setResponse(response);
        command.setAcknowledgedAt(LocalDateTime.now());
        commandRepository.save(command);
        log.info("Command {} acknowledged", commandId);
        return command;
    }

    @Transactional
    public DeviceCommand failCommand(UUID commandId, String reason) {
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceCommand", "id", commandId));

        command.setStatus(CommandStatus.FAILED);
        command.setResponse(reason);
        commandRepository.save(command);
        log.info("Command {} failed: {}", commandId, reason);
        return command;
    }

    public Page<DeviceCommand> getByDeviceId(UUID deviceId, Pageable pageable) {
        return commandRepository.findByDeviceId(deviceId, pageable);
    }

    public DeviceCommand getById(UUID id) {
        return commandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceCommand", "id", id));
    }
}