package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.common.Result;
import com.iot.platform.entity.AlarmLog;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.User;
import com.iot.platform.service.AlarmAnalysisService;
import com.iot.platform.service.AlarmLogService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报警日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/alarm-log")
public class AlarmLogController {
    
    @Resource
    private AlarmLogService alarmLogService;
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private PermissionUtil permissionUtil;
    
    @Resource
    private AlarmAnalysisService alarmAnalysisService;
    
    /**
     * 分页查询报警日志
     * 支持按设备、级别、时间范围、处理状态筛选
     * 实现数据权限过滤（通过设备的group_id）
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getAlarmLogList(@RequestHeader("Authorization") String token,
                                                        @RequestBody Map<String, Object> params) {
        try {
            // 获取当前用户信息
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 获取查询参数
            int page = params.get("page") != null ? (int) params.get("page") : 1;
            int pageSize = params.get("pageSize") != null ? (int) params.get("pageSize") : 20;
            String deviceCode = (String) params.get("deviceCode");
            String alarmLevel = (String) params.get("alarmLevel");
            Integer status = params.get("status") != null ? (Integer) params.get("status") : null;
            String startTimeStr = (String) params.get("startTime");
            String endTimeStr = (String) params.get("endTime");
            
            // 解析时间参数
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                startTime = LocalDateTime.parse(startTimeStr, formatter);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = LocalDateTime.parse(endTimeStr, formatter);
            }
            
            // 数据权限过滤：获取用户有权限查看的设备ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            List<Long> allowedDeviceIds = null;
            
            if (allowedGroupIds != null) {
                // 普通用户：获取权限范围内的所有设备ID
                LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
                deviceQuery.in(Device::getGroupId, allowedGroupIds);
                List<Device> allowedDevices = deviceService.list(deviceQuery);
                allowedDeviceIds = new ArrayList<>();
                for (Device device : allowedDevices) {
                    allowedDeviceIds.add(device.getId());
                }
                
                // 如果没有任何设备权限，返回空结果
                if (allowedDeviceIds.isEmpty()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("list", new ArrayList<>());
                    result.put("total", 0);
                    return Result.success(result);
                }
            }
            // 超级管理员：allowedDeviceIds = null，表示不过滤
            
            // 分页查询报警日志
            Page<AlarmLog> pageResult = alarmLogService.pageQuery(
                    page, pageSize, deviceCode, alarmLevel, status, 
                    startTime, endTime, allowedDeviceIds);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询报警日志失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 处理报警
     * 权限验证：只有通知人员才能处理
     */
    @PostMapping("/handle")
    public Result<Void> handleAlarm(@RequestHeader("Authorization") String token,
                                     @RequestBody Map<String, Object> params) {
        try {
            // 获取当前用户信息
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 获取参数
            Long alarmId = params.get("alarmId") != null ? 
                    Long.valueOf(params.get("alarmId").toString()) : null;
            String handleDescription = (String) params.get("handleDescription");
            Object handleImages = params.get("handleImages"); // 可能是List或String
            
            if (alarmId == null) {
                return Result.error("报警ID不能为空");
            }
            
            // 数据权限验证：检查用户是否有权限处理该报警
            AlarmLog alarmLog = alarmLogService.getById(alarmId);
            if (alarmLog == null) {
                return Result.error("报警不存在");
            }
            
            // 获取报警对应的设备
            Device device = deviceService.getById(alarmLog.getDeviceId());
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 检查权限：超级管理员或有数据权限的通知人员可以处理
            boolean isSuperAdmin = permissionUtil.isSuperAdmin(currentUser);
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            boolean hasDataPermission = allowedGroupIds == null || allowedGroupIds.contains(device.getGroupId());
            
            // 检查是否为通知人员
            boolean isNotifyUser = false;
            if (alarmLog.getNotifyUsers() != null && !alarmLog.getNotifyUsers().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Long> notifyUserIds = mapper.readValue(alarmLog.getNotifyUsers(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, Long.class));
                    isNotifyUser = notifyUserIds.contains(currentUser.getId());
                } catch (Exception e) {
                    log.error("解析通知人员失败", e);
                }
            }
            
            // 权限判断：超级管理员可以处理所有报警，普通用户需要有数据权限且是通知人员
            // 如果通知人员列表为空，允许有数据权限的用户处理（兼容旧数据）
            if (!isSuperAdmin) {
                if (!hasDataPermission) {
                    return Result.error("无权限处理该报警");
                }
                // 如果配置了通知人员，则必须是通知人员才能处理
                if (alarmLog.getNotifyUsers() != null && !alarmLog.getNotifyUsers().isEmpty() && !isNotifyUser) {
                    return Result.error("只有配置的处理人才能处理此报警");
                }
            }
            
            // 处理图片数据：转换为JSON字符串
            String handleImagesJson = null;
            if (handleImages != null) {
                if (handleImages instanceof String) {
                    handleImagesJson = (String) handleImages;
                } else {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        handleImagesJson = mapper.writeValueAsString(handleImages);
                    } catch (Exception e) {
                        log.error("处理图片数据失败", e);
                    }
                }
            }
            
            // 处理报警（使用真实姓名，如果为空则使用账号）
            String handlerName = currentUser.getRealName() != null && !currentUser.getRealName().trim().isEmpty() 
                ? currentUser.getRealName() 
                : currentUser.getUsername();
            boolean success = alarmLogService.handleAlarm(alarmId, handlerName, 
                handleDescription, handleImagesJson);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("处理报警失败");
            }
        } catch (Exception e) {
            log.error("处理报警失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取未处理报警数量
     */
    @PostMapping("/unhandled-count")
    public Result<Long> getUnhandledCount(@RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户信息
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 数据权限过滤：获取用户有权限查看的设备ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            List<Long> allowedDeviceIds = null;
            
            if (allowedGroupIds != null) {
                // 普通用户：获取权限范围内的所有设备ID
                LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
                deviceQuery.in(Device::getGroupId, allowedGroupIds);
                List<Device> allowedDevices = deviceService.list(deviceQuery);
                allowedDeviceIds = new ArrayList<>();
                for (Device device : allowedDevices) {
                    allowedDeviceIds.add(device.getId());
                }
            }
            // 超级管理员：allowedDeviceIds = null，表示不过滤
            
            // 查询未处理报警数量
            long count = alarmLogService.getUnhandledCount(allowedDeviceIds);
            
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未处理报警数量失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取报警级别枚举列表
     */
    @PostMapping("/alarm-levels")
    public Result<List<Map<String, String>>> getAlarmLevels() {
        try {
            List<Map<String, String>> levels = new ArrayList<>();
            
            Map<String, String> critical = new HashMap<>();
            critical.put("value", "critical");
            critical.put("label", "严重");
            levels.add(critical);
            
            Map<String, String> warning = new HashMap<>();
            warning.put("value", "warning");
            warning.put("label", "警告");
            levels.add(warning);
            
            Map<String, String> info = new HashMap<>();
            info.put("value", "info");
            info.put("label", "提示");
            levels.add(info);
            
            return Result.success(levels);
        } catch (Exception e) {
            log.error("获取报警级别枚举失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取报警统计信息
     */
    @PostMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户信息
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 数据权限过滤：获取用户有权限查看的设备ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            List<Long> allowedDeviceIds = null;
            
            if (allowedGroupIds != null) {
                LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
                deviceQuery.in(Device::getGroupId, allowedGroupIds);
                List<Device> allowedDevices = deviceService.list(deviceQuery);
                allowedDeviceIds = new ArrayList<>();
                for (Device device : allowedDevices) {
                    allowedDeviceIds.add(device.getId());
                }
            }
            
            // 统计数据
            Map<String, Object> stats = new HashMap<>();
            
            LambdaQueryWrapper<AlarmLog> wrapper = new LambdaQueryWrapper<>();
            if (allowedDeviceIds != null) {
                if (allowedDeviceIds.isEmpty()) {
                    // 没有权限，返回0
                    stats.put("total", 0L);
                    stats.put("unhandled", 0L);
                    stats.put("critical", 0L);
                    stats.put("warning", 0L);
                    stats.put("info", 0L);
                    return Result.success(stats);
                }
                wrapper.in(AlarmLog::getDeviceId, allowedDeviceIds);
            }
            
            // 总报警数
            long total = alarmLogService.count(wrapper);
            stats.put("total", total);
            
            // 未处理数
            LambdaQueryWrapper<AlarmLog> unhandledWrapper = wrapper.clone();
            unhandledWrapper.eq(AlarmLog::getStatus, 0);
            long unhandled = alarmLogService.count(unhandledWrapper);
            stats.put("unhandled", unhandled);
            
            // 按级别统计
            LambdaQueryWrapper<AlarmLog> criticalWrapper = wrapper.clone();
            criticalWrapper.eq(AlarmLog::getAlarmLevel, "critical");
            long critical = alarmLogService.count(criticalWrapper);
            stats.put("critical", critical);
            
            LambdaQueryWrapper<AlarmLog> warningWrapper = wrapper.clone();
            warningWrapper.eq(AlarmLog::getAlarmLevel, "warning");
            long warning = alarmLogService.count(warningWrapper);
            stats.put("warning", warning);
            
            LambdaQueryWrapper<AlarmLog> infoWrapper = wrapper.clone();
            infoWrapper.eq(AlarmLog::getAlarmLevel, "info");
            long info = alarmLogService.count(infoWrapper);
            stats.put("info", info);
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取报警统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取报警分析数据
     * 支持单设备或多设备分析，用于设备详情页或Dashboard全局统计
     */
    @PostMapping("/analysis")
    public Result<Map<String, Object>> getAlarmAnalysis(@RequestHeader("Authorization") String token,
                                                         @RequestBody Map<String, Object> params) {
        try {
            log.info("========== 开始处理报警分析请求 ==========");
            
            // 获取当前用户信息
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 获取查询参数
            @SuppressWarnings("unchecked")
            List<String> deviceCodes = (List<String>) params.get("deviceCodes");
            String timeRange = (String) params.get("timeRange");
            String startTime = (String) params.get("startTime");
            String endTime = (String) params.get("endTime");
            
            // 默认参数
            if (timeRange == null || timeRange.isEmpty()) {
                timeRange = "7days";
            }
            
            // 验证设备编码列表
            if (deviceCodes == null || deviceCodes.isEmpty()) {
                return Result.error("设备编码列表不能为空");
            }
            
            log.info("请求参数 - 设备编码: {}, 时间范围: {}, 开始时间: {}, 结束时间: {}",
                    deviceCodes, timeRange, startTime, endTime);
            
            // 数据权限过滤：获取用户有权限查看的设备编码列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            List<String> allowedDeviceCodes = new ArrayList<>();
            
            if (allowedGroupIds != null) {
                // 普通用户：获取权限范围内的所有设备编码
                LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
                deviceQuery.in(Device::getGroupId, allowedGroupIds);
                List<Device> allowedDevices = deviceService.list(deviceQuery);
                for (Device device : allowedDevices) {
                    allowedDeviceCodes.add(device.getDeviceCode());
                }
                
                // 过滤请求的设备编码，只保留有权限的设备
                List<String> filteredDeviceCodes = new ArrayList<>();
                for (String code : deviceCodes) {
                    if (allowedDeviceCodes.contains(code)) {
                        filteredDeviceCodes.add(code);
                    }
                }
                
                // 如果过滤后没有设备权限，返回空结果
                if (filteredDeviceCodes.isEmpty()) {
                    log.warn("用户没有访问任何请求设备的权限");
                    Map<String, Object> result = new HashMap<>();
                    result.put("trend", new HashMap<>());
                    result.put("levelDistribution", new HashMap<>());
                    result.put("efficiency", new HashMap<>());
                    return Result.success(result);
                }
                
                deviceCodes = filteredDeviceCodes;
            }
            // 超级管理员：不过滤设备编码
            
            log.info("权限过滤后设备编码: {}", deviceCodes);
            
            // 调用服务层获取分析数据
            Map<String, Object> analysisData = alarmAnalysisService.getAlarmAnalysis(
                    deviceCodes, timeRange, startTime, endTime);
            
            log.info("========== 报警分析请求处理完成 ==========");
            return Result.success(analysisData);
        } catch (Exception e) {
            log.error("获取报警分析数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}
