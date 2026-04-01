package com.iotplatform.service;

import com.iotplatform.dto.request.CreateThresholdRuleRequest;
import com.iotplatform.dto.request.UpdateThresholdRuleRequest;
import com.iotplatform.exception.InvalidOperationException;
import com.iotplatform.exception.ResourceNotFoundException;
import com.iotplatform.model.ThresholdRule;
import com.iotplatform.model.enums.MetricType;
import com.iotplatform.repository.ThresholdRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThresholdRuleService {

    private final ThresholdRuleRepository ruleRepository;

    @Transactional
    public ThresholdRule createRule(CreateThresholdRuleRequest request, UUID createdBy) {
        validateThresholds(request.getMinValue(), request.getMaxValue());

        ThresholdRule rule = ThresholdRule.builder()
                .deviceId(request.getDeviceId())
                .metricType(request.getMetricType())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .description(request.getDescription())
                .active(true)
                .deleted(false)
                .createdBy(createdBy)
                .build();

        ThresholdRule saved = ruleRepository.save(rule);
        log.info("Threshold rule created: {} for metric {} [min={}, max={}] by user {}",
                saved.getId(), saved.getMetricType(), saved.getMinValue(), saved.getMaxValue(), createdBy);
        return saved;
    }

    public Page<ThresholdRule> getAllRules(Pageable pageable) {
        return ruleRepository.findByDeletedFalse(pageable);
    }

    public ThresholdRule getRuleById(UUID id) {
        return ruleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdRule", "id", id));
    }

    public List<ThresholdRule> getApplicableRules(UUID deviceId, MetricType metricType) {
        return ruleRepository.findApplicableRules(deviceId, metricType);
    }

    public List<ThresholdRule> getRulesByDeviceId(UUID deviceId) {
        return ruleRepository.findByDeviceIdAndDeletedFalse(deviceId);
    }

    @Transactional
    public ThresholdRule updateRule(UUID id, UpdateThresholdRuleRequest request) {
        ThresholdRule rule = getRuleById(id);

        if (request.getMetricType() != null) rule.setMetricType(request.getMetricType());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getActive() != null) rule.setActive(request.getActive());

        Double newMin = request.getMinValue() != null ? request.getMinValue() : rule.getMinValue();
        Double newMax = request.getMaxValue() != null ? request.getMaxValue() : rule.getMaxValue();

        if (request.getMinValue() != null || request.getMaxValue() != null) {
            validateThresholds(newMin, newMax);
            rule.setMinValue(newMin);
            rule.setMaxValue(newMax);
        }

        ruleRepository.save(rule);
        log.info("Threshold rule updated: {} [min={}, max={}]", rule.getId(), rule.getMinValue(), rule.getMaxValue());
        return rule;
    }

    @Transactional
    public void deleteRule(UUID id) {
        ThresholdRule rule = getRuleById(id);
        rule.setDeleted(true);
        rule.setActive(false);
        ruleRepository.save(rule);
        log.info("Threshold rule soft-deleted: {}", id);
    }

    private void validateThresholds(Double min, Double max) {
        if (min == null && max == null) {
            throw new InvalidOperationException(
                    "At least one threshold boundary (minValue or maxValue) must be specified");
        }
        if (min != null && max != null && min >= max) {
            throw new InvalidOperationException("minValue must be less than maxValue");
        }
    }
}