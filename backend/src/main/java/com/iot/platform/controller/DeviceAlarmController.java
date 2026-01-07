package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.dto.DeviceAlarmConfigRequest;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.User;
import com.iot.platform.service.DeviceService;
import com.iot.platform.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备告警配置控制器
 * 
 * 数据权限说明：
 * - 超级管理员：可配置所有设备的告警阈值
 * - 普通用户：只能配置本分组及所有下级分组下的设备告警阈值
 */
@Slf4j
@RestController
@RequestMapping("/device-alarm")
@CrossOrigin
public class DeviceAlarmController {
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private PermissionUtil permissionUtil;
    
    /**
     * 配置设备告警阈值（单个或批量）
     * 
     * 数据权限验证：验证用户是否有权限操作这些设备
     */
    @PostMapping("/configure")
    public Result<Map<String, Object>> configureAlarm(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody DeviceAlarmConfigRequest request) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            
            // 确定要配置的设备ID列表
            List<Long> deviceIds;
            if (request.getDeviceIds() != null && !request.getDeviceIds().isEmpty()) {
                // 批量配置
                deviceIds = request.getDeviceIds();
            } else if (request.getDeviceId() != null) {
                // 单个配置
                deviceIds = Collections.singletonList(request.getDeviceId());
            } else {
                return Result.error("设备ID不能为空");
            }
            
            // 验证告警配置
            if (request.getAlarmConfig() == null) {
                return Result.error("告警配置不能为空");
            }
            
            // 验证：一个物模型属性只能有1个条件
            if (request.getAlarmConfig().getConditions() != null && !request.getAlarmConfig().getConditions().isEmpty()) {
                java.util.Set<String> metricSet = new java.util.HashSet<>();
                for (AlarmConfigDTO.AlarmCondition condition : request.getAlarmConfig().getConditions()) {
                    if (condition.getMetric() != null && !condition.getMetric().isEmpty()) {
                        if (metricSet.contains(condition.getMetric())) {
                            return Result.error("每个物模型属性只能配置一个条件，属性 " + condition.getMetric() + " 重复了");
                        }
                        metricSet.add(condition.getMetric());
                    }
                }
            }
            
            // 数据权限验证：检查用户是否有权限操作这些设备
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            for (Long deviceId : deviceIds) {
                Device device = deviceService.getById(deviceId);
                if (device == null) {
                    return Result.error("设备不存在: " + deviceId);
                }
                
                // 超级管理员可以操作所有设备，普通用户只能操作权限范围内的设备
                if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                    return Result.error("无权限操作设备: " + device.getDeviceName() + " (" + device.getDeviceCode() + ")");
                }
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
            
            log.info("用户 {} 配置了 {} 台设备的告警阈值", currentUser.getUsername(), count);
            return Result.success(result);
        } catch (Exception e) {
            log.error("配置设备告警阈值失败", e);
            return Result.error("配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备告警配置
     * 
     * 数据权限验证：验证用户是否有权限查看该设备的告警配置
     */
    @GetMapping("/config/{deviceId}")
    public Result<AlarmConfigDTO> getAlarmConfig(
            @PathVariable Long deviceId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            
            // 获取设备信息
            Device device = deviceService.getById(deviceId);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 数据权限验证：检查用户是否有权限查看该设备
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                return Result.error("无权限查看该设备的告警配置");
            }
            
            AlarmConfigDTO config = deviceService.getAlarmConfig(deviceId);
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取设备告警配置失败", e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换设备告警启用状态
     * 
     * 数据权限验证：验证用户是否有权限操作该设备
     */
    @PostMapping("/toggle/{deviceId}")
    public Result<String> toggleAlarmEnabled(
            @PathVariable Long deviceId,
            @RequestParam Boolean enabled,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            
            // 获取设备信息
            Device device = deviceService.getById(deviceId);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 数据权限验证：检查用户是否有权限操作该设备
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                return Result.error("无权限操作该设备的告警状态");
            }
            
            deviceService.toggleAlarmEnabled(deviceId, enabled);
            log.info("用户 {} {}了设备 {} 的告警", currentUser.getUsername(), enabled ? "启用" : "禁用", device.getDeviceName());
            return Result.success("切换成功");
        } catch (Exception e) {
            log.error("切换设备告警状态失败", e);
            return Result.error("切换失败: " + e.getMessage());
        }
    }
}
