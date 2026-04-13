package com.iotplatform.service;

import com.iotplatform.exception.DuplicateResourceException;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.DeviceGroup;
import com.iotplatform.model.DeviceGroupMembership;
import com.iotplatform.repository.DeviceGroupMembershipRepository;
import com.iotplatform.repository.DeviceGroupRepository;
import com.iotplatform.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGroupService {

    private final DeviceGroupRepository groupRepository;
    private final DeviceGroupMembershipRepository membershipRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceGroup createGroup(String name, String description, String color, UUID createdBy) {
        if (groupRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Group with name '" + name + "' already exists");
        }

        DeviceGroup group = DeviceGroup.builder()
                .name(name)
                .description(description)
                .color(color)
                .createdBy(createdBy)
                .build();

        DeviceGroup saved = groupRepository.save(group);
        log.info("Device group created: {} [{}]", saved.getName(), saved.getId());
        return saved;
    }

    public List<DeviceGroup> getAllGroups() {
        return groupRepository.findAll();
    }

    public DeviceGroup getGroupById(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceGroup", "id", id));
    }

    @Transactional
    public DeviceGroup updateGroup(UUID id, String name, String description, String color) {
        DeviceGroup group = getGroupById(id);
        if (name != null && !name.equalsIgnoreCase(group.getName())) {
            if (groupRepository.existsByNameIgnoreCase(name)) {
                throw new DuplicateResourceException("Group with name '" + name + "' already exists");
            }
            group.setName(name);
        }
        if (description != null) group.setDescription(description);
        if (color != null) group.setColor(color);
        groupRepository.save(group);
        log.info("Device group updated: {} [{}]", group.getName(), group.getId());
        return group;
    }

    @Transactional
    public void deleteGroup(UUID id) {
        DeviceGroup group = getGroupById(id);
        membershipRepository.deleteByGroupId(id);
        groupRepository.delete(group);
        log.info("Device group deleted: {} [{}]", group.getName(), group.getId());
    }

    @Transactional
    public void addDeviceToGroup(UUID groupId, UUID deviceId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("DeviceGroup", "id", groupId);
        }
        if (!deviceRepository.existsByIdAndDeletedFalse(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        if (membershipRepository.existsByGroupIdAndDeviceId(groupId, deviceId)) {
            throw new DuplicateResourceException("Device is already in this group");
        }

        DeviceGroupMembership membership = DeviceGroupMembership.builder()
                .groupId(groupId)
                .deviceId(deviceId)
                .build();
        membershipRepository.save(membership);
        log.info("Device {} added to group {}", deviceId, groupId);
    }

    @Transactional
    public void removeDeviceFromGroup(UUID groupId, UUID deviceId) {
        if (!membershipRepository.existsByGroupIdAndDeviceId(groupId, deviceId)) {
            throw new ResourceNotFoundException("DeviceGroupMembership", "groupId+deviceId",
                    groupId + "/" + deviceId);
        }
        membershipRepository.deleteByGroupIdAndDeviceId(groupId, deviceId);
        log.info("Device {} removed from group {}", deviceId, groupId);
    }

    public List<UUID> getDeviceIdsInGroup(UUID groupId) {
        return membershipRepository.findDeviceIdsByGroupId(groupId);
    }

    public List<DeviceGroupMembership> getGroupMemberships(UUID groupId) {
        return membershipRepository.findByGroupId(groupId);
    }

    public List<DeviceGroupMembership> getDeviceGroups(UUID deviceId) {
        return membershipRepository.findByDeviceId(deviceId);
    }

    public long getDeviceCount(UUID groupId) {
        return membershipRepository.countByGroupId(groupId);
    }
}