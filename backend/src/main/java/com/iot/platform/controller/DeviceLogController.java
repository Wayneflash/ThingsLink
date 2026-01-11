package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceLog;
import com.iot.platform.entity.User;
import com.iot.platform.service.DeviceLogService;
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
 * 设备日志控制器
 */
@Slf4j
@RestController
@RequestMapping("/device-logs")
@CrossOrigin
public class DeviceLogController {

    @Resource
    private DeviceLogService deviceLogService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private PermissionUtil permissionUtil;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询设备日志
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getDeviceLogList(@RequestHeader("Authorization") String token,
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
            String logType = (String) params.get("logType");
            String startTimeStr = (String) params.get("startTime");
            String endTimeStr = (String) params.get("endTime");

            // 解析时间参数
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                startTime = LocalDateTime.parse(startTimeStr, FORMATTER);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
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

            // 分页查询设备日志
            Page<DeviceLog> pageResult = deviceLogService.pageQuery(
                    page, pageSize, deviceCode, logType,
                    startTime, endTime, allowedDeviceIds);

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());

            return Result.success(result);
        } catch (Exception e) {
            log.error("查询设备日志失败", e);
            return Result.error(e.getMessage());
        }
    }
}
