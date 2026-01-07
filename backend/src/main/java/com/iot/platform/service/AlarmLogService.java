package com.iot.platform.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.entity.AlarmLog;
import com.iot.platform.entity.Attribute;
import com.iot.platform.entity.Device;
import com.iot.platform.mapper.AlarmLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报警日志服务
 * 每个物模型属性作为独立的报警规则，独立触发、独立恢复、独立堆叠
 */
@Slf4j
@Service
public class AlarmLogService extends ServiceImpl<AlarmLogMapper, AlarmLog> {
    
    @Resource
    private ProductService productService;
    
    /**
     * 检查设备数据是否触发报警，如果触发则记录报警日志
     * 每个物模型属性独立检查，独立触发，独立恢复
     * 支持报警堆叠模式：开启时，同一设备同一指标在未恢复前不会重复报警
     * 
     * @param device 设备信息（包含报警配置）
     * @param dataMap 设备上报的数据（key=属性标识符，value=属性值）
     */
    public void checkAndRecordAlarm(Device device, Map<String, String> dataMap) {
        // 检查设备是否启用报警
        if (device.getAlarmEnabled() == null || !device.getAlarmEnabled()) {
            return;
        }
        
        // 检查是否配置了报警
        if (device.getAlarmConfig() == null || device.getAlarmConfig().trim().isEmpty()) {
            return;
        }
        
        try {
            // 解析报警配置
            AlarmConfigDTO alarmConfig = JSON.parseObject(device.getAlarmConfig(), AlarmConfigDTO.class);
            if (alarmConfig == null || alarmConfig.getMetrics() == null || alarmConfig.getMetrics().isEmpty()) {
                return;
            }
            
            // 获取产品物模型属性（用于显示属性名称）
            List<Attribute> attributes = productService.getProductAttributes(device.getProductId());
            Map<String, String> attrNameMap = new java.util.HashMap<>();
            Map<String, String> attrUnitMap = new java.util.HashMap<>();
            for (Attribute attr : attributes) {
                attrNameMap.put(attr.getAddr(), attr.getAttrName());
                attrUnitMap.put(attr.getAddr(), attr.getUnit() != null ? attr.getUnit() : "");
            }
            
            // 是否开启堆叠模式（全局配置）
            boolean stackMode = alarmConfig.getStackMode() != null && alarmConfig.getStackMode();
            
            // 遍历所有物模型属性配置，每个属性独立处理
            for (Map.Entry<String, AlarmConfigDTO.MetricConfig> entry : alarmConfig.getMetrics().entrySet()) {
                String metric = entry.getKey(); // 物模型属性标识符
                AlarmConfigDTO.MetricConfig config = entry.getValue();
                
                // 跳过未启用的监控
                if (config.getEnabled() == null || !config.getEnabled()) {
                    continue;
                }
                
                // 检查数据中是否包含该指标
                if (!dataMap.containsKey(metric)) {
                    continue;
                }
                
                String currentValueStr = dataMap.get(metric);
                String operator = config.getOperator();
                Double threshold = config.getThreshold();
                String level = config.getLevel();
                
                // 尝试将当前值转换为数字进行比较
                try {
                    Double currentValue = Double.parseDouble(currentValueStr);
                    
                    // 判断是否触发报警
                    boolean triggered = false;
                    switch (operator) {
                        case ">":
                            triggered = currentValue > threshold;
                            break;
                        case "<":
                            triggered = currentValue < threshold;
                            break;
                        case "=":
                            triggered = Math.abs(currentValue - threshold) < 0.0001;
                            break;
                        default:
                            log.warn("未知的运算符: {}", operator);
                            continue;
                    }
                    
                    if (triggered) {
                        // 触发报警：检查堆叠模式
                        boolean shouldRecord = true;
                        if (stackMode) {
                            // 检查该设备的该指标是否已有未恢复的报警
                            LambdaQueryWrapper<AlarmLog> checkWrapper = new LambdaQueryWrapper<>();
                            checkWrapper.eq(AlarmLog::getDeviceId, device.getId())
                                       .eq(AlarmLog::getMetric, metric)
                                       .eq(AlarmLog::getRecovered, 0);
                            long existingAlarmCount = this.count(checkWrapper);
                            
                            if (existingAlarmCount > 0) {
                                log.debug("堆叠模式 - 跳过重复报警: 设备={}, 指标={}", device.getDeviceName(), metric);
                                shouldRecord = false;
                            }
                        }
                        
                        if (shouldRecord) {
                            // 构造报警消息
                            String attrName = attrNameMap.getOrDefault(metric, metric);
                            String unit = attrUnitMap.getOrDefault(metric, "");
                            String alarmMessage = String.format("%s 当前值 %s%s %s 阈值 %s%s",
                                    attrName, currentValueStr, unit, operator, threshold, unit);
                            
                            // 创建报警日志
                            AlarmLog alarmLog = new AlarmLog();
                            alarmLog.setDeviceId(device.getId());
                            alarmLog.setDeviceCode(device.getDeviceCode());
                            alarmLog.setDeviceName(device.getDeviceName());
                            alarmLog.setMetric(metric);
                            // 记录处理人：单个ID转为JSON字符串（保持兼容性）
                            if (alarmConfig.getNotifyUser() != null) {
                                alarmLog.setNotifyUsers("[" + alarmConfig.getNotifyUser() + "]");
                            }
                            alarmLog.setAlarmLevel(level);
                            alarmLog.setAlarmMessage(alarmMessage);
                            alarmLog.setTriggerTime(LocalDateTime.now());
                            alarmLog.setStatus(0);
                            alarmLog.setRecovered(0);
                            alarmLog.setCreateTime(LocalDateTime.now());
                            
                            this.save(alarmLog);
                            
                            log.info("触发报警 - 设备: {}, 指标: {}, 级别: {}, 消息: {}",
                                    device.getDeviceName(), metric, level, alarmMessage);
                        }
                    } else {
                        // 未触发报警：检查是否需要恢复之前的报警
                        if (stackMode) {
                            // 检查该指标是否有未恢复的报警
                            LambdaQueryWrapper<AlarmLog> recoverWrapper = new LambdaQueryWrapper<>();
                            recoverWrapper.eq(AlarmLog::getDeviceId, device.getId())
                                         .eq(AlarmLog::getMetric, metric)
                                         .eq(AlarmLog::getRecovered, 0)
                                         .orderByDesc(AlarmLog::getTriggerTime)
                                         .last("LIMIT 1");
                            AlarmLog existingAlarm = this.getOne(recoverWrapper);
                            
                            if (existingAlarm != null) {
                                // 判断当前值是否不满足报警条件（即已恢复）
                                boolean recovered = false;
                                switch (operator) {
                                    case ">":
                                        recovered = currentValue <= threshold;
                                        break;
                                    case "<":
                                        recovered = currentValue >= threshold;
                                        break;
                                    case "=":
                                        recovered = Math.abs(currentValue - threshold) >= 0.0001;
                                        break;
                                }
                                
                                if (recovered) {
                                    existingAlarm.setRecovered(1);
                                    this.updateById(existingAlarm);
                                    log.info("报警恢复 - 设备: {}, 指标: {}", device.getDeviceName(), metric);
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("无法将值转换为数字进行比较: metric={}, value={}", metric, currentValueStr);
                }
            }
            
        } catch (Exception e) {
            log.error("检查报警失败 - 设备: {}", device.getDeviceCode(), e);
        }
    }
    
    /**
     * 分页查询报警日志（支持数据权限过滤）
     */
    public Page<AlarmLog> pageQuery(int page, int pageSize, String deviceCode, String alarmLevel,
                                     Integer status, LocalDateTime startTime, LocalDateTime endTime,
                                     List<Long> allowedDeviceIds) {
        LambdaQueryWrapper<AlarmLog> wrapper = new LambdaQueryWrapper<>();
        
        if (allowedDeviceIds != null) {
            if (allowedDeviceIds.isEmpty()) {
                return new Page<>(page, pageSize);
            }
            wrapper.in(AlarmLog::getDeviceId, allowedDeviceIds);
        }
        
        if (deviceCode != null && !deviceCode.trim().isEmpty()) {
            wrapper.like(AlarmLog::getDeviceCode, deviceCode);
        }
        
        if (alarmLevel != null && !alarmLevel.trim().isEmpty()) {
            wrapper.eq(AlarmLog::getAlarmLevel, alarmLevel);
        }
        
        if (status != null) {
            wrapper.eq(AlarmLog::getStatus, status);
        }
        
        if (startTime != null) {
            wrapper.ge(AlarmLog::getTriggerTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AlarmLog::getTriggerTime, endTime);
        }
        
        wrapper.orderByDesc(AlarmLog::getTriggerTime);
        
        return this.page(new Page<>(page, pageSize), wrapper);
    }
    
    /**
     * 处理告警
     */
    /**
     * 处理报警
     */
    public boolean handleAlarm(Long alarmId, String handler, String handleDescription, String handleImages) {
        AlarmLog alarmLog = this.getById(alarmId);
        if (alarmLog == null) {
            return false;
        }
        
        alarmLog.setStatus(1);
        alarmLog.setHandler(handler);
        alarmLog.setHandleDescription(handleDescription);
        alarmLog.setHandleImages(handleImages);
        alarmLog.setHandleTime(LocalDateTime.now());
        
        return this.updateById(alarmLog);
    }
    
    /**
     * 获取未处理报警数量
     */
    public long getUnhandledCount(List<Long> allowedDeviceIds) {
        LambdaQueryWrapper<AlarmLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlarmLog::getStatus, 0);
        
        if (allowedDeviceIds != null) {
            if (allowedDeviceIds.isEmpty()) {
                return 0;
            }
            wrapper.in(AlarmLog::getDeviceId, allowedDeviceIds);
        }
        
        return this.count(wrapper);
    }
}
