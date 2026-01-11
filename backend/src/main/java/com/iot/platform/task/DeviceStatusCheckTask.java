package com.iot.platform.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.entity.AlarmLog;
import com.iot.platform.entity.Device;
import com.iot.platform.service.AlarmLogService;
import com.iot.platform.service.DeviceLogService;
import com.iot.platform.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
     * 当设备离线时，检查是否配置了离线报警，如果配置且离线时长超过阈值，则触发报警
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
            
            // 计算离线时长（分钟）
            LocalDateTime lastOnlineTime = device.getLastOnlineTime();
            if (lastOnlineTime == null) {
                return; // 没有最后上线时间，无法计算离线时长
            }
            
            long offlineMinutes = java.time.Duration.between(lastOnlineTime, LocalDateTime.now()).toMinutes();
            
            // 检查是否超过阈值
            if (offlineMinutes > offlineAlarm.getThreshold()) {
                // 检查是否已经触发过报警（避免重复报警）
                // 这里简化处理，每次离线都检查，实际生产中可能需要记录上次报警时间
                // 触发离线报警
                triggerOfflineAlarm(device, offlineAlarm, offlineMinutes);
            }
        } catch (Exception e) {
            log.error("检查离线报警失败 - 设备: {}", device.getDeviceCode(), e);
        }
    }
    
    /**
     * 触发离线报警
     */
    private void triggerOfflineAlarm(Device device, AlarmConfigDTO.OfflineAlarmConfig offlineAlarm, long offlineMinutes) {
        try {
            // 构造报警消息
            String alarmMessage = String.format("设备离线超过%d分钟（阈值：%d分钟）",
                    offlineMinutes, offlineAlarm.getThreshold());
            
            // 创建报警日志
            AlarmLog alarmLog = new AlarmLog();
            alarmLog.setDeviceId(device.getId());
            alarmLog.setDeviceCode(device.getDeviceCode());
            alarmLog.setDeviceName(device.getDeviceName());
            alarmLog.setAlarmLevel(offlineAlarm.getLevel());
            alarmLog.setAlarmMessage(alarmMessage);
            alarmLog.setTriggerTime(LocalDateTime.now());
            alarmLog.setStatus(0); // 未处理
            
            // 设置处理人
            try {
                String alarmConfigJson = device.getAlarmConfig();
                AlarmConfigDTO alarmConfig = objectMapper.readValue(alarmConfigJson, AlarmConfigDTO.class);
                if (alarmConfig.getNotifyUser() != null) {
                    alarmLog.setNotifyUsers(objectMapper.writeValueAsString(
                            java.util.Collections.singletonList(alarmConfig.getNotifyUser())));
                }
            } catch (Exception e) {
                log.error("设置处理人失败", e);
            }
            
            // 保存报警日志
            alarmLogService.save(alarmLog);
            
            log.info("触发离线报警 - 设备: {}, 离线时长: {}分钟, 阈值: {}分钟, 级别: {}",
                    device.getDeviceCode(), offlineMinutes, offlineAlarm.getThreshold(), offlineAlarm.getLevel());
        } catch (Exception e) {
            log.error("触发离线报警失败 - 设备: {}", device.getDeviceCode(), e);
        }
    }
}
