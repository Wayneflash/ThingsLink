package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SceneLog;
import com.iot.platform.entity.SceneRule;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.SceneRuleService;
import com.iot.platform.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场景联动控制器
 */
@RestController
@RequestMapping("/scene")
@RequiredArgsConstructor
public class SceneRuleController {

    private final SceneRuleService sceneRuleService;
    private final DeviceService deviceService;
    private final PermissionUtil permissionUtil;

    /**
     * 获取可选设备列表（用于创建/编辑规则时选择设备）
     * 按用户权限过滤，支持模糊搜索
     */
    @GetMapping("/devices")
    public Result<List<Device>> getAvailableDevices(
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        
        // 模糊搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(w -> w
                    .like(Device::getDeviceName, keyword)
                    .or()
                    .like(Device::getDeviceCode, keyword)
            );
        }
        
        // 权限过滤
        if (allowedGroupIds != null) {
            query.in(Device::getGroupId, allowedGroupIds);
        }
        
        query.orderByDesc(Device::getCreateTime);
        List<Device> devices = deviceService.list(query);
        
        return Result.success(devices);
    }

    /**
     * 分页查询规则列表（按用户权限过滤）
     */
    @GetMapping("/rules")
    public Result<Page<SceneRule>> getRuleList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        return Result.success(sceneRuleService.pageList(pageNum, pageSize, allowedGroupIds));
    }

    /**
     * 获取规则详情
     */
    @GetMapping("/rules/{id}")
    public Result<SceneRule> getRuleDetail(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        SceneRule rule = sceneRuleService.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        
        // 校验权限
        try {
            sceneRuleService.getRuleLogs(id, allowedGroupIds); // 复用权限校验
        } catch (RuntimeException e) {
            return Result.error("无权访问该规则");
        }
        
        return Result.success(rule);
    }

    /**
     * 创建规则
     */
    @PostMapping("/rules")
    public Result<SceneRule> createRule(@RequestBody SceneRule rule, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        try {
            SceneRule created = sceneRuleService.createRule(rule, allowedGroupIds);
            return Result.success(created, "创建成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新规则
     */
    @PutMapping("/rules/{id}")
    public Result<SceneRule> updateRule(@PathVariable Long id, @RequestBody SceneRule rule, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        rule.setId(id);
        try {
            SceneRule updated = sceneRuleService.updateRule(rule, allowedGroupIds);
            return Result.success(updated, "更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 切换规则启用状态
     */
    @PutMapping("/rules/{id}/toggle")
    public Result<Boolean> toggleRule(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        try {
            return Result.success(sceneRuleService.toggleEnabled(id, allowedGroupIds));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/rules/{id}")
    public Result<Boolean> deleteRule(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        try {
            return Result.success(sceneRuleService.deleteRule(id, allowedGroupIds));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取规则执行日志
     */
    @GetMapping("/rules/{ruleId}/logs")
    public Result<List<SceneLog>> getRuleLogs(@PathVariable Long ruleId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        
        try {
            return Result.success(sceneRuleService.getRuleLogs(ruleId, allowedGroupIds));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取最近执行日志（按用户权限过滤）
     */
    @GetMapping("/logs/recent")
    public Result<List<SceneLog>> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        List<Long> allowedGroupIds = permissionUtil.getAllowedGroupIds(token);
        return Result.success(sceneRuleService.getRecentLogs(limit, allowedGroupIds));
    }
}
