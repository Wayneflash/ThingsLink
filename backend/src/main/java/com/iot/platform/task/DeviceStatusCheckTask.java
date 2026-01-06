package com.iot.platform.task;

import com.iot.platform.entity.Device;
import com.iot.platform.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
}
