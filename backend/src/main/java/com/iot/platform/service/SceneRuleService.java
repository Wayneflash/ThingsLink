package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SceneLog;
import com.iot.platform.entity.SceneRule;
import com.iot.platform.mapper.SceneLogMapper;
import com.iot.platform.mapper.SceneRuleMapper;
import com.iot.platform.mqtt.MqttPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场景联动服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneRuleService extends ServiceImpl<SceneRuleMapper, SceneRule> {

    private final SceneLogMapper sceneLogMapper;
    private final DeviceService deviceService;
    private final MqttPublisher mqttPublisher;

    /**
     * 分页查询规则列表（按用户设备权限过滤）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param allowedGroupIds 用户允许访问的分组ID列表，null表示不过滤（超级管理员）
     */
    public Page<SceneRule> pageList(int pageNum, int pageSize, List<Long> allowedGroupIds) {
        Page<SceneRule> page = new Page<>(pageNum, pageSize);
        
        // 获取所有规则（带设备信息）
        List<SceneRule> allRules = baseMapper.selectRulesWithDevice();
        
        // 按权限过滤
        List<SceneRule> filteredRules;
        if (allowedGroupIds == null) {
            // 超级管理员，不过滤
            filteredRules = allRules;
        } else {
            // 获取用户有权限的设备ID列表
            List<Long> allowedDeviceIds = getDeviceIdsByGroupIds(allowedGroupIds);
            
            // 过滤规则：触发设备和目标设备都必须在用户权限范围内
            filteredRules = allRules.stream()
                    .filter(rule -> allowedDeviceIds.contains(rule.getTriggerDeviceId()) 
                            && allowedDeviceIds.contains(rule.getTargetDeviceId()))
                    .collect(Collectors.toList());
        }
        
        page.setRecords(filteredRules);
        page.setTotal(filteredRules.size());
        return page;
    }

    /**
     * 获取最近执行日志（按用户设备权限过滤）
     */
    public List<SceneLog> getRecentLogs(int limit, List<Long> allowedGroupIds) {
        List<SceneLog> allLogs = sceneLogMapper.selectRecent(limit);
        
        if (allowedGroupIds == null) {
            return allLogs;
        }
        
        // 获取用户有权限的设备ID列表
        List<Long> allowedDeviceIds = getDeviceIdsByGroupIds(allowedGroupIds);
        
        // 过滤日志
        return allLogs.stream()
                .filter(log -> allowedDeviceIds.contains(log.getTriggerDeviceId()) 
                        && allowedDeviceIds.contains(log.getTargetDeviceId()))
                .collect(Collectors.toList());
    }

    /**
     * 创建规则（带权限和唯一性校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public SceneRule createRule(SceneRule rule, List<Long> allowedGroupIds) {
        // 1. 校验设备权限
        validateDevicePermission(rule.getTriggerDeviceId(), allowedGroupIds, "触发设备");
        validateDevicePermission(rule.getTargetDeviceId(), allowedGroupIds, "目标设备");
        
        // 2. 校验设备唯一性
        validateDeviceUniqueness(rule.getTriggerDeviceId(), rule.getTargetDeviceId(), null);
        
        // 3. 保存规则
        rule.setEnabled(1);
        rule.setTriggerCount(0);
        save(rule);
        
        // 4. 补充设备名称
        Device triggerDevice = deviceService.getById(rule.getTriggerDeviceId());
        Device targetDevice = deviceService.getById(rule.getTargetDeviceId());
        rule.setTriggerDeviceName(triggerDevice != null ? triggerDevice.getDeviceName() : null);
        rule.setTargetDeviceName(targetDevice != null ? targetDevice.getDeviceName() : null);
        
        return rule;
    }

    /**
     * 更新规则（带权限和唯一性校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public SceneRule updateRule(SceneRule rule, List<Long> allowedGroupIds) {
        // 1. 校验原规则权限
        SceneRule existRule = getById(rule.getId());
        if (existRule == null) {
            throw new RuntimeException("规则不存在");
        }
        validateRulePermission(existRule, allowedGroupIds);
        
        // 2. 校验新设备权限
        validateDevicePermission(rule.getTriggerDeviceId(), allowedGroupIds, "触发设备");
        validateDevicePermission(rule.getTargetDeviceId(), allowedGroupIds, "目标设备");
        
        // 3. 校验设备唯一性（排除当前规则）
        validateDeviceUniqueness(rule.getTriggerDeviceId(), rule.getTargetDeviceId(), rule.getId());
        
        // 4. 更新规则
        updateById(rule);
        
        // 5. 补充设备名称
        Device triggerDevice = deviceService.getById(rule.getTriggerDeviceId());
        Device targetDevice = deviceService.getById(rule.getTargetDeviceId());
        rule.setTriggerDeviceName(triggerDevice != null ? triggerDevice.getDeviceName() : null);
        rule.setTargetDeviceName(targetDevice != null ? targetDevice.getDeviceName() : null);
        
        return rule;
    }

    /**
     * 切换规则启用状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleEnabled(Long ruleId, List<Long> allowedGroupIds) {
        SceneRule rule = getById(ruleId);
        if (rule == null) {
            return false;
        }
        
        // 校验权限
        validateRulePermission(rule, allowedGroupIds);
        
        rule.setEnabled(rule.getEnabled() == 1 ? 0 : 1);
        return updateById(rule);
    }

    /**
     * 删除规则
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long ruleId, List<Long> allowedGroupIds) {
        SceneRule rule = getById(ruleId);
        if (rule == null) {
            return false;
        }
        
        // 校验权限
        validateRulePermission(rule, allowedGroupIds);
        
        return removeById(ruleId);
    }

    /**
     * 获取规则执行日志
     */
    public List<SceneLog> getRuleLogs(Long ruleId, List<Long> allowedGroupIds) {
        // 先校验规则权限
        SceneRule rule = getById(ruleId);
        if (rule == null) {
            return new ArrayList<>();
        }
        validateRulePermission(rule, allowedGroupIds);
        
        return sceneLogMapper.selectByRuleId(ruleId);
    }

    /**
     * 获取所有启用的规则（内部使用，用于规则引擎）
     */
    public List<SceneRule> getEnabledRules() {
        return baseMapper.selectEnabledRulesWithDevice();
    }

    /**
     * 检查并执行规则（内部使用，由设备数据上报触发）
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndExecute(Long deviceId, String attr, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        List<SceneRule> rules = getEnabledRules();
        for (SceneRule rule : rules) {
            if (!rule.getTriggerDeviceId().equals(deviceId)) {
                continue;
            }
            if (!rule.getTriggerAttr().equals(attr)) {
                continue;
            }

            if (checkCondition(rule, value)) {
                log.info("场景联动规则触发: ruleId={}, ruleName={}, triggerValue={}", 
                        rule.getId(), rule.getRuleName(), value);
                executeRule(rule, value);
            }
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 校验设备是否在用户权限范围内
     */
    private void validateDevicePermission(Long deviceId, List<Long> allowedGroupIds, String deviceType) {
        if (allowedGroupIds == null) {
            return; // 超级管理员，不校验
        }
        
        Device device = deviceService.getById(deviceId);
        if (device == null) {
            throw new RuntimeException(deviceType + "不存在");
        }
        
        if (!allowedGroupIds.contains(device.getGroupId())) {
            throw new RuntimeException("无权访问该" + deviceType);
        }
    }

    /**
     * 校验规则是否在用户权限范围内
     */
    private void validateRulePermission(SceneRule rule, List<Long> allowedGroupIds) {
        if (allowedGroupIds == null) {
            return; // 超级管理员，不校验
        }
        
        // 规则中的触发设备和目标设备都必须在用户权限范围内
        validateDevicePermission(rule.getTriggerDeviceId(), allowedGroupIds, "触发设备");
        validateDevicePermission(rule.getTargetDeviceId(), allowedGroupIds, "目标设备");
    }

    /**
     * 校验设备唯一性
     * 一个设备只能出现在一个规则中（无论是触发还是目标）
     */
    private void validateDeviceUniqueness(Long triggerDeviceId, Long targetDeviceId, Long excludeRuleId) {
        // 检查触发设备是否已被使用
        LambdaQueryWrapper<SceneRule> triggerQuery = new LambdaQueryWrapper<>();
        triggerQuery.eq(SceneRule::getTriggerDeviceId, triggerDeviceId);
        if (excludeRuleId != null) {
            triggerQuery.ne(SceneRule::getId, excludeRuleId);
        }
        if (this.count(triggerQuery) > 0) {
            throw new RuntimeException("触发设备已被其他规则使用");
        }
        
        // 检查触发设备是否作为目标设备被使用
        LambdaQueryWrapper<SceneRule> triggerAsTargetQuery = new LambdaQueryWrapper<>();
        triggerAsTargetQuery.eq(SceneRule::getTargetDeviceId, triggerDeviceId);
        if (excludeRuleId != null) {
            triggerAsTargetQuery.ne(SceneRule::getId, excludeRuleId);
        }
        if (this.count(triggerAsTargetQuery) > 0) {
            throw new RuntimeException("触发设备已被其他规则作为目标设备使用");
        }
        
        // 检查目标设备是否已被使用
        LambdaQueryWrapper<SceneRule> targetQuery = new LambdaQueryWrapper<>();
        targetQuery.eq(SceneRule::getTargetDeviceId, targetDeviceId);
        if (excludeRuleId != null) {
            targetQuery.ne(SceneRule::getId, excludeRuleId);
        }
        if (this.count(targetQuery) > 0) {
            throw new RuntimeException("目标设备已被其他规则使用");
        }
        
        // 检查目标设备是否作为触发设备被使用
        LambdaQueryWrapper<SceneRule> targetAsTriggerQuery = new LambdaQueryWrapper<>();
        targetAsTriggerQuery.eq(SceneRule::getTriggerDeviceId, targetDeviceId);
        if (excludeRuleId != null) {
            targetAsTriggerQuery.ne(SceneRule::getId, excludeRuleId);
        }
        if (this.count(targetAsTriggerQuery) > 0) {
            throw new RuntimeException("目标设备已被其他规则作为触发设备使用");
        }
        
        // 检查触发设备和目标设备是否相同
        if (triggerDeviceId.equals(targetDeviceId)) {
            throw new RuntimeException("触发设备和目标设备不能相同");
        }
    }

    /**
     * 根据分组ID列表获取设备ID列表
     */
    private List<Long> getDeviceIdsByGroupIds(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        query.in(Device::getGroupId, groupIds);
        List<Device> devices = deviceService.list(query);
        
        return devices.stream()
                .map(Device::getId)
                .collect(Collectors.toList());
    }

    /**
     * 检查条件是否满足
     */
    private boolean checkCondition(SceneRule rule, String actualValue) {
        try {
            double actual = Double.parseDouble(actualValue);
            double threshold = Double.parseDouble(rule.getTriggerValue());
            String operator = rule.getOperator();

            switch (operator) {
                case "gt":
                    return actual > threshold;
                case "lt":
                    return actual < threshold;
                case "gte":
                    return actual >= threshold;
                case "lte":
                    return actual <= threshold;
                case "eq":
                    return Math.abs(actual - threshold) < 0.0001;
                case "neq":
                    return Math.abs(actual - threshold) >= 0.0001;
                default:
                    return actualValue.equals(rule.getTriggerValue());
            }
        } catch (NumberFormatException e) {
            return actualValue.equals(rule.getTriggerValue());
        }
    }

    /**
     * 执行规则动作
     */
    private void executeRule(SceneRule rule, String triggerValue) {
        SceneLog sceneLog = new SceneLog();
        sceneLog.setRuleId(rule.getId());
        sceneLog.setRuleName(rule.getRuleName());
        sceneLog.setTriggerDeviceId(rule.getTriggerDeviceId());
        sceneLog.setTriggerAttr(rule.getTriggerAttr());
        sceneLog.setTriggerValue(triggerValue);
        sceneLog.setTargetDeviceId(rule.getTargetDeviceId());
        sceneLog.setTargetAttr(rule.getTargetAttr());
        sceneLog.setTargetValue(rule.getTargetValue());

        try {
            Device targetDevice = deviceService.getById(rule.getTargetDeviceId());
            if (targetDevice == null) {
                throw new RuntimeException("目标设备不存在");
            }

            sceneLog.setTriggerDeviceName(getDeviceName(rule.getTriggerDeviceId()));
            sceneLog.setTargetDeviceName(targetDevice.getDeviceName());

            mqttPublisher.sendCommand(
                    targetDevice.getDeviceCode(),
                    rule.getTargetAttr(),
                    rule.getTargetValue()
            );

            sceneLog.setExecuteResult("success");

            rule.setTriggerCount(rule.getTriggerCount() + 1);
            rule.setLastTriggeredAt(LocalDateTime.now());
            updateById(rule);

            log.info("场景联动执行成功: ruleId={}, targetDevice={}, attr={}, value={}",
                    rule.getId(), targetDevice.getDeviceCode(), rule.getTargetAttr(), rule.getTargetValue());

        } catch (Exception e) {
            sceneLog.setExecuteResult("failed");
            sceneLog.setErrorMessage(e.getMessage());
            log.error("场景联动执行失败: ruleId={}, error={}", rule.getId(), e.getMessage());
        }

        sceneLogMapper.insert(sceneLog);
    }

    private String getDeviceName(Long deviceId) {
        Device device = deviceService.getById(deviceId);
        return device != null ? device.getDeviceName() : "未知设备";
    }
}
