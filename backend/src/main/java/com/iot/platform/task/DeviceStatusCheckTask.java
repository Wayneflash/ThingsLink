package com.iot.platform.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.entity.AlarmLog;
import com.iot.platform.entity.Device;
import com.iot.platform.service.AlarmLogService;
import com.iot.platform.service.DeviceLogService;
import com.iot.platform.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备状态检查定时任务
 */
@Slf4j
@Component
public class DeviceStatusCheckTask {
    
    private static final String DEVICE_ONLINE_KEY = "device:online:";
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private DeviceLogService deviceLogService;
    
    @Resource
    private AlarmLogService alarmLogService;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Autowired
    @Lazy
    private com.iot.platform.service.NotificationService notificationService;
    
    /**
     * 每30秒检查一次设备在线状态
     */
    @Scheduled(fixedRate = 30000)
    public void checkDeviceStatus() {
        try {
            // 获取所有在线设备
            List<Device> onlineDevices = deviceService.lambdaQuery()
                .eq(Device::getStatus, 1)
                .list();
            
            if (onlineDevices.isEmpty()) {
                return;
            }
            
            log.debug("开始检查设备在线状态，当前在线设备数: {}", onlineDevices.size());
            
            int offlineCount = 0;
            for (Device device : onlineDevices) {
                // 检查 Redis 中的心跳 key 是否存在
                String key = DEVICE_ONLINE_KEY + device.getDeviceCode();
                Boolean exists = stringRedisTemplate.hasKey(key);
                
                // 如果 key 不存在，说明设备已超时离线
                if (exists == null || !exists) {
                    log.info("设备 {} 心跳超时，更新为离线状态", device.getDeviceCode());
                    deviceService.updateOnlineStatus(device.getDeviceCode(), false);
                    
                    // 记录设备离线日志
                    recordDeviceLog(device.getId(), device.getDeviceCode(), "offline", "设备离线");
                    
                    // 检查并触发离线报警
                    checkAndTriggerOfflineAlarm(device);
                    
                    offlineCount++;
                }
            }
            
            if (offlineCount > 0) {
                log.info("本次检查完成，{}个设备已离线", offlineCount);
            }
            
        } catch (Exception e) {
            log.error("检查设备状态失败", e);
        }
    }
    
    /**
     * 记录设备日志
     */
    private void recordDeviceLog(Long deviceId, String deviceCode, String logType, String logDetail) {
        try {
            com.iot.platform.entity.DeviceLog deviceLog = new com.iot.platform.entity.DeviceLog();
            deviceLog.setDeviceId(deviceId);
            deviceLog.setDeviceCode(deviceCode);
            deviceLog.setLogType(logType);
            deviceLog.setLogDetail(logDetail);
            deviceLog.setCreateTime(LocalDateTime.now());
            deviceLogService.save(deviceLog);
        } catch (Exception e) {
            log.error("记录设备日志失败 - 设备: {}", deviceCode, e);
        }
    }
    
    /**
     * 检查并触发离线报警
     * 当设备从在线变为离线时，直接触发离线报警（如果配置了离线报警）
     * 
     * 注意：这个方法只在设备状态从在线(1)变为离线(0)时被调用一次
     * 堆叠模式会确保在设备恢复上线前不会重复报警
     */
    private void checkAndTriggerOfflineAlarm(Device device) {
        try {
            // 获取设备报警配置
            String alarmConfigJson = device.getAlarmConfig();
            if (alarmConfigJson == null || alarmConfigJson.isEmpty()) {
                return; // 未配置报警，直接返回
            }
            
            // 解析报警配置
            AlarmConfigDTO alarmConfig = objectMapper.readValue(alarmConfigJson, AlarmConfigDTO.class);
            
            // 检查是否启用了离线报警
            if (alarmConfig.getOfflineAlarm() == null || !alarmConfig.getOfflineAlarm().getEnabled()) {
                return; // 未启用离线报警，直接返回
            }
            
            // 获取离线报警配置
            AlarmConfigDTO.OfflineAlarmConfig offlineAlarm = alarmConfig.getOfflineAlarm();
            
            // 直接触发离线报警（阈值用于配置离线多久触发报警，但实际上设备刚离线就触发）
            // 因为心跳有效期已经相当于"离线检测阈值"，设备心跳过期即视为离线
            triggerOfflineAlarm(device, offlineAlarm);
        } catch (Exception e) {
            log.error("检查离线报警失败 - 设备: {}", device.getDeviceCode(), e);
        }
    }
    
    /**
     * 触发离线报警
     * 
     * 报警逻辑：
     * 1. 设备离线时触发一次报警
     * 2. 堆叠模式下，设备上线前不会重复报警
     * 3. 设备上线后，离线报警标记为已恢复
     * 4. 设备再次离线时，可以重新触发报警
     */
    private void triggerOfflineAlarm(Device device, AlarmConfigDTO.OfflineAlarmConfig offlineAlarm) {
        try {
            // 获取报警配置，检查是否开启堆叠模式
            AlarmConfigDTO alarmConfig = null;
            boolean stackMode = true; // 默认开启堆叠模式
            try {
                String alarmConfigJson = device.getAlarmConfig();
                if (alarmConfigJson != null && !alarmConfigJson.isEmpty()) {
                    alarmConfig = objectMapper.readValue(alarmConfigJson, AlarmConfigDTO.class);
                    if (alarmConfig != null && alarmConfig.getStackMode() != null) {
                        stackMode = alarmConfig.getStackMode();
                    }
                }
            } catch (Exception e) {
                log.warn("解析报警配置失败，使用默认堆叠模式 - 设备: {}", device.getDeviceCode(), e);
            }
            
            // 离线报警使用特殊标识 "__offline__" 作为 metric
            final String OFFLINE_METRIC = "__offline__";
            
            // 如果开启了堆叠模式，检查是否已有未恢复的离线报警
            // 注意：堆叠模式只检查 recovered=0（未恢复），不检查 status（处理状态）
            // 因为用户可能已经处理了报警，但设备仍然离线，此时不应该重复触发报警
            if (stackMode) {
                LambdaQueryWrapper<AlarmLog> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(AlarmLog::getDeviceId, device.getId())
                           .eq(AlarmLog::getMetric, OFFLINE_METRIC)
                           .eq(AlarmLog::getRecovered, 0); // 只检查是否未恢复，不检查处理状态
                
                long existingCount = alarmLogService.count(checkWrapper);
                if (existingCount > 0) {
                    log.debug("堆叠模式 - 跳过重复离线报警: 设备={}, 已有未恢复的离线报警", device.getDeviceCode());
                    return; // 已有未恢复的离线报警，不重复触发
                }
            }
            
            // 构造报警消息（简洁明了，只显示设备离线）
            String alarmMessage = String.format("设备已离线（心跳超时%d分钟）", 
                    offlineAlarm.getThreshold() != null ? offlineAlarm.getThreshold() : 5);
            
            // 创建报警日志
            AlarmLog alarmLog = new AlarmLog();
            alarmLog.setDeviceId(device.getId());
            alarmLog.setDeviceCode(device.getDeviceCode());
            alarmLog.setDeviceName(device.getDeviceName());
            alarmLog.setMetric(OFFLINE_METRIC); // 设置离线报警标识
            alarmLog.setAlarmLevel(offlineAlarm.getLevel());
            alarmLog.setAlarmMessage(alarmMessage);
            alarmLog.setTriggerTime(LocalDateTime.now());
            alarmLog.setStatus(0); // 未处理
            alarmLog.setRecovered(0); // 未恢复
            
            // 设置处理人
            try {
                if (alarmConfig != null && alarmConfig.getNotifyUser() != null) {
                    alarmLog.setNotifyUsers(objectMapper.writeValueAsString(
                            java.util.Collections.singletonList(alarmConfig.getNotifyUser())));
                }
            } catch (Exception e) {
                log.error("设置处理人失败", e);
            }
            
            // 保存报警日志
            alarmLogService.save(alarmLog);
            
            log.info("触发离线报警 - 设备: {}, 级别: {}", device.getDeviceCode(), offlineAlarm.getLevel());
            
            // 创建通知消息（给处理人发送邮件和短信通知）
            if (alarmConfig != null && alarmConfig.getNotifyUser() != null) {
                try {
                    notificationService.createNotification(
                        alarmConfig.getNotifyUser(),
                        alarmLog.getId(),
                        device.getId(),
                        device.getDeviceCode(),
                        device.getDeviceName(),
                        offlineAlarm.getLevel(),
                        alarmMessage
                    );
                } catch (Exception e) {
                    log.error("创建通知消息失败 - 报警ID: {}, 用户ID: {}", 
                            alarmLog.getId(), alarmConfig.getNotifyUser(), e);
                    // 通知创建失败不影响报警记录
                }
            }
        } catch (Exception e) {
            log.error("触发离线报警失败 - 设备: {}", device.getDeviceCode(), e);
        }
    }
}
