package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.service.DeviceDataService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.util.PermissionUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
     * 分页查询设备历史数据（数据库层 LIMIT，避免全量加载）
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getDeviceDataList(
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
            
            int pageNum = request.getPageNum() != null ? Math.max(1, request.getPageNum()) : 1;
            int pageSize = request.getPageSize() != null ? request.getPageSize() : 100;
            pageSize = Math.min(Math.max(1, pageSize), 500);  // 限制 1~500
            
            IPage<DeviceData> page = deviceDataService.getHistoryDataPage(
                request.getDeviceCode(),
                request.getAttrs(),
                startTime,
                endTime,
                pageNum,
                pageSize
            );
            
            Map<String, Object> result = new HashMap<>(2);
            result.put("list", page.getRecords());
            result.put("total", page.getTotal());
            return Result.success(result);
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
        private Integer pageNum;   // 页码，默认 1
        private Integer pageSize;  // 每页条数，默认 100，最大 500
    }
}