package com.iotplatform.device.service;

import com.iotplatform.device.exception.ResourceNotFoundException;
import com.iotplatform.device.model.ThresholdRule;
import com.iotplatform.device.model.enums.MetricType;
import com.iotplatform.device.repository.ThresholdRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class ThresholdRuleService {

    private final ThresholdRuleRepository ruleRepository;

    @Transactional
    public ThresholdRule createRule(MetricType metricType, Double minValue, Double maxValue,
                                    String description, UUID deviceId, UUID createdBy) {
        ThresholdRule rule = ThresholdRule.builder()
                .metricType(metricType).minValue(minValue).maxValue(maxValue)
                .description(description).deviceId(deviceId).createdBy(createdBy)
                .active(true).deleted(false).build();
        ThresholdRule saved = ruleRepository.save(rule);
        log.info("Rule created: {} [{}-{}] for metric {}", saved.getId(), minValue, maxValue, metricType);
        return saved;
    }

    public Page<ThresholdRule> getAllRules(Pageable pageable) {
        return ruleRepository.findByDeletedFalse(pageable);
    }

    public ThresholdRule getRuleById(UUID id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdRule", "id", id));
    }

    public List<ThresholdRule> getApplicableRules(UUID deviceId, MetricType metricType) {
        return ruleRepository.findApplicableRules(deviceId, metricType);
    }

    @Transactional
    public ThresholdRule updateRule(UUID id, MetricType metricType, Double minValue, Double maxValue,
                                    String description, Boolean active) {
        ThresholdRule rule = getRuleById(id);
        if (metricType != null) rule.setMetricType(metricType);
        if (minValue != null) rule.setMinValue(minValue);
        if (maxValue != null) rule.setMaxValue(maxValue);
        if (description != null) rule.setDescription(description);
        if (active != null) rule.setActive(active);
        ruleRepository.save(rule);
        return rule;
    }

    @Transactional
    public void deleteRule(UUID id) {
        ThresholdRule rule = getRuleById(id);
        rule.setDeleted(true);
        ruleRepository.save(rule);
    }

    public Page<ThresholdRule> getRulesByDevice(UUID deviceId, Pageable pageable) {
        return ruleRepository.findByDeviceIdAndDeletedFalse(deviceId, pageable);
    }
}