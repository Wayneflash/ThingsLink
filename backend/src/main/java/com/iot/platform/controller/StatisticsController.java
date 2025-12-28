package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.service.DeviceDataService;
import com.iot.platform.service.DeviceService;
import lombok.Data;
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
 * 统计查询 Controller
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@CrossOrigin
public class StatisticsController {
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private DeviceDataService deviceDataService;
    
    /**
     * 获取平台统计数据概览（按照API文档）
     */
    @PostMapping("/overview")
    public Result<Map<String, Object>> getStatisticsOverview() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 设备统计
            Map<String, Long> deviceStats = deviceService.getDeviceStatistics();
            result.put("deviceTotal", deviceStats.get("total"));
            result.put("deviceOnline", deviceStats.get("online"));
            result.put("deviceOffline", deviceStats.get("offline"));
            
            // 其他统计信息
            result.put("alarmCount", 0); // 暂时设为0，后续可实现告警统计
            result.put("productCount", 0); // 暂时设为0，后续可实现产品统计
            result.put("userCount", 0); // 暂时设为0，后续可实现用户统计
            
            // 今日数据上报量
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
            query.ge(DeviceData::getReceiveTime, todayStart);
            long todayDataCount = deviceDataService.count(query);
            result.put("todayDataCount", todayDataCount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取Dashboard统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取数据上报量趋势（最近24小时/7天/30天）
     */
    @PostMapping("/data-trend")
    public Result<Map<String, Object>> getDataTrend(@RequestBody DataTrendRequest request) {
        try {
            String range = request.getRange() != null ? request.getRange() : "24h";
            
            Map<String, Object> result = new HashMap<>();
            
            // 根据范围生成时间标签和数据
            if ("24h".equals(range)) {
                // 最近24小时，每小时一个数据点
                List<String> timeLabels = new ArrayList<>();
                List<Integer> dataCounts = new ArrayList<>();
                
                LocalDateTime now = LocalDateTime.now();
                for (int i = 23; i >= 0; i--) {
                    LocalDateTime time = now.minusHours(i);
                    timeLabels.add(String.format("%02d:00", time.getHour()));
                    
                    // 统计该小时的数据量
                    LocalDateTime hourStart = time.withMinute(0).withSecond(0);
                    LocalDateTime hourEnd = hourStart.plusHours(1);
                    
                    LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
                    query.ge(DeviceData::getReceiveTime, hourStart)
                         .lt(DeviceData::getReceiveTime, hourEnd);
                    long count = deviceDataService.count(query);
                    dataCounts.add((int) count);
                }
                
                result.put("timeLabels", timeLabels);
                result.put("dataCounts", dataCounts);
                
            } else if ("7d".equals(range)) {
                // 最近7天，每天一个数据点
                List<String> timeLabels = new ArrayList<>();
                List<Integer> dataCounts = new ArrayList<>();
                
                LocalDateTime now = LocalDateTime.now();
                for (int i = 6; i >= 0; i--) {
                    LocalDateTime date = now.minusDays(i);
                    timeLabels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    
                    LocalDateTime dayStart = date.withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime dayEnd = dayStart.plusDays(1);
                    
                    LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
                    query.ge(DeviceData::getReceiveTime, dayStart)
                         .lt(DeviceData::getReceiveTime, dayEnd);
                    long count = deviceDataService.count(query);
                    dataCounts.add((int) count);
                }
                
                result.put("timeLabels", timeLabels);
                result.put("dataCounts", dataCounts);
                
            } else if ("30d".equals(range)) {
                // 最近30天，每天一个数据点
                List<String> timeLabels = new ArrayList<>();
                List<Integer> dataCounts = new ArrayList<>();
                
                LocalDateTime now = LocalDateTime.now();
                for (int i = 29; i >= 0; i--) {
                    LocalDateTime date = now.minusDays(i);
                    timeLabels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
                    
                    LocalDateTime dayStart = date.withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime dayEnd = dayStart.plusDays(1);
                    
                    LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
                    query.ge(DeviceData::getReceiveTime, dayStart)
                         .lt(DeviceData::getReceiveTime, dayEnd);
                    long count = deviceDataService.count(query);
                    dataCounts.add((int) count);
                }
                
                result.put("timeLabels", timeLabels);
                result.put("dataCounts", dataCounts);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取数据趋势失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设备分布统计
     */
    @PostMapping("/device-distribution")
    public Result<Map<String, Object>> getDeviceDistribution(@RequestBody Map<String, String> request) {
        try {
            String type = request.get("type");
            if (type == null) {
                return Result.error("统计类型不能为空");
            }
            
            List<Map<String, Object>> items = new ArrayList<>();
            
            if ("group".equals(type)) {
                // 按分组统计设备分布
                // 这里需要实现具体的按分组统计逻辑
                // 暂时返回示例数据
                Map<String, Object> item1 = new HashMap<>();
                item1.put("name", "办公区域");
                item1.put("count", 56);
                item1.put("percentage", 35.9);
                items.add(item1);
                
                Map<String, Object> item2 = new HashMap<>();
                item2.put("name", "生产区域");
                item2.put("count", 100);
                item2.put("percentage", 64.1);
                items.add(item2);
            } else if ("product".equals(type)) {
                // 按产品统计设备分布
                // 这里需要实现具体的按产品统计逻辑
                // 暂时返回示例数据
                Map<String, Object> item1 = new HashMap<>();
                item1.put("name", "温湿度传感器");
                item1.put("count", 100);
                item1.put("percentage", 64.1);
                items.add(item1);
                
                Map<String, Object> item2 = new HashMap<>();
                item2.put("name", "智能门锁");
                item2.put("count", 56);
                item2.put("percentage", 35.9);
                items.add(item2);
            } else {
                return Result.error("统计类型不支持");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("items", items);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取设备分布统计失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 数据趋势查询请求参数
     */
    @Data
    public static class DataTrendRequest {
        private String range;  // 时间范围（24h/7d/30d）
    }
}