package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.dto.energy.*;
import com.iot.platform.entity.User;
import com.iot.platform.service.EnergyStatisticsService;
import com.iot.platform.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 能源统计 Controller
 */
@Slf4j
@RestController
@RequestMapping("/energy")
@CrossOrigin
public class EnergyStatisticsController {
    
    @Resource
    private EnergyStatisticsService energyStatisticsService;
    
    @Resource
    private PermissionUtil permissionUtil;
    
    /**
     * 获取能耗统计
     */
    @PostMapping("/statistics")
    public Result<EnergyStatisticsVO> getStatistics(
            @RequestBody EnergyStatisticsRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 权限验证
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error(401, "未授权，请先登录");
            }
            
            // 获取权限范围内的分组ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            
            // 参数验证
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.error(400, "开始时间和结束时间不能为空");
            }
            
            if (request.getEnergyType() == null || request.getEnergyType().trim().isEmpty()) {
                return Result.error(400, "能源类型不能为空");
            }
            
            EnergyStatisticsVO result = energyStatisticsService.getEnergyStatistics(request, allowedGroupIds);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取能耗统计失败", e);
            return Result.error("获取能耗统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取能耗趋势
     */
    @PostMapping("/trend")
    public Result<List<EnergyTrendVO>> getTrend(
            @RequestBody EnergyTrendRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 权限验证
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error(401, "未授权，请先登录");
            }
            
            // 获取权限范围内的分组ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            
            // 参数验证
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.error(400, "开始时间和结束时间不能为空");
            }
            
            if (request.getEnergyType() == null || request.getEnergyType().trim().isEmpty()) {
                return Result.error(400, "能源类型不能为空");
            }
            
            List<EnergyTrendVO> result = energyStatisticsService.getEnergyTrend(request, allowedGroupIds);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取能耗趋势失败", e);
            return Result.error("获取能耗趋势失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取能耗报表
     */
    @PostMapping("/report")
    public Result<EnergyReportVO> getReport(
            @RequestBody EnergyReportRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 权限验证
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error(401, "未授权，请先登录");
            }
            
            // 获取权限范围内的分组ID列表
            List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(currentUser);
            
            // 参数验证
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return Result.error(400, "开始时间和结束时间不能为空");
            }
            
            if (request.getEnergyType() == null || request.getEnergyType().trim().isEmpty()) {
                return Result.error(400, "能源类型不能为空");
            }
            
            // 默认值
            if (request.getPage() == null || request.getPage() < 1) {
                request.setPage(1);
            }
            if (request.getPageSize() == null || request.getPageSize() < 1) {
                request.setPageSize(10);
            }
            
            EnergyReportVO result = energyStatisticsService.getEnergyReport(request, allowedGroupIds);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取能耗报表失败", e);
            return Result.error("获取能耗报表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取实时能耗（Dashboard用）
     */
    @PostMapping("/realtime")
    public Result<RealtimeEnergyVO> getRealtime(
            @RequestBody(required = false) RealtimeEnergyRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 权限验证
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error(401, "未授权，请先登录");
            }
            
            // 默认值
            if (request == null) {
                request = new RealtimeEnergyRequest();
                request.setTimeRange("today");
            }
            if (request.getTimeRange() == null || request.getTimeRange().trim().isEmpty()) {
                request.setTimeRange("today");
            }
            
            RealtimeEnergyVO result = energyStatisticsService.getRealtimeEnergy(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取实时能耗失败", e);
            return Result.error("获取实时能耗失败: " + e.getMessage());
        }
    }
}
