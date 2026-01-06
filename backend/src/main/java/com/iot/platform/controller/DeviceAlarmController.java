package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.dto.DeviceAlarmConfigRequest;
import com.iot.platform.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备告警配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/device-alarm")
public class DeviceAlarmController {
    
    @Resource
    private DeviceService deviceService;
    
    /**
     * 配置设备告警阈值（单个或批量）
     */
    @PostMapping("/configure")
    public Result<Map<String, Object>> configureAlarm(@RequestBody DeviceAlarmConfigRequest request) {
        try {
            // 确定要配置的设备ID列表
            java.util.List<Long> deviceIds;
            if (request.getDeviceIds() != null && !request.getDeviceIds().isEmpty()) {
                // 批量配置
                deviceIds = request.getDeviceIds();
            } else if (request.getDeviceId() != null) {
                // 单个配置
                deviceIds = Collections.singletonList(request.getDeviceId());
            } else {
                return Result.error("设备ID不能为空");
            }
            
            // 执行配置
            int count = deviceService.configureAlarm(
                deviceIds, 
                request.getAlarmConfig(), 
                request.getEnabled() != null ? request.getEnabled() : true
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("message", "成功配置 " + count + " 台设备");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("配置设备告警阈值失败", e);
            return Result.error("配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备告警配置
     */
    @GetMapping("/config/{deviceId}")
    public Result<AlarmConfigDTO> getAlarmConfig(@PathVariable Long deviceId) {
        try {
            AlarmConfigDTO config = deviceService.getAlarmConfig(deviceId);
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取设备告警配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换设备告警启用状态
     */
    @PostMapping("/toggle/{deviceId}")
    public Result<String> toggleAlarmEnabled(@PathVariable Long deviceId, @RequestParam Boolean enabled) {
        try {
            deviceService.toggleAlarmEnabled(deviceId, enabled);
            return Result.success("切换成功");
        } catch (Exception e) {
            log.error("切换设备告警状态失败", e);
            return Result.error("切换失败: " + e.getMessage());
        }
    }
}
