package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.service.DeviceDataService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.util.PermissionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备数据 Controller
 */
@Slf4j
@RestController
@RequestMapping("/device-data")
@CrossOrigin
public class DeviceDataController {
    
    @Resource
    private DeviceDataService deviceDataService;
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private PermissionUtil permissionUtil;
    
    /**
     * 获取设备最新数据
     */
    @GetMapping("/latest/{deviceCode}")
    public Result<List<DeviceData>> getLatestData(
            @PathVariable String deviceCode,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据权限验证：检查设备是否在用户权限范围内
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 获取用户权限分组列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
            if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                return Result.error("无权限查看该设备数据");
            }
            
            List<DeviceData> list = deviceDataService.getLatestData(deviceCode, limit);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取最新数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询设备历史数据
     */
    @PostMapping("/list")
    public Result<List<DeviceData>> getDeviceDataList(
            @RequestBody DataQueryRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据权限验证：检查设备是否在用户权限范围内
            if (request.getDeviceCode() == null || request.getDeviceCode().trim().isEmpty()) {
                return Result.error("设备编码不能为空");
            }
            
            Device device = deviceService.getByDeviceCode(request.getDeviceCode());
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 获取用户权限分组列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
            if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                return Result.error("无权限查看该设备数据");
            }
            
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            
            if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
                startTime = LocalDateTime.parse(request.getStartTime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
                endTime = LocalDateTime.parse(request.getEndTime(), 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            
            List<DeviceData> dataList = deviceDataService.getHistoryData(
                request.getDeviceCode(),
                request.getAttrs(),
                startTime,
                endTime
            );
            
            // 限制最大1000条
            if (dataList.size() > 1000) {
                dataList = dataList.subList(0, 1000);
            }
            
            return Result.success(dataList);
        } catch (Exception e) {
            log.error("查询设备历史数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 数据查询请求参数
     */
    @Data
    public static class DataQueryRequest {
        private String deviceCode;
        private String startTime;
        private String endTime;
        private String attrs;      // 属性标识符（多个用逗号分隔）
    }
}