package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Role;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.RoleMapper;
import com.iot.platform.service.UserService;
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
 * 角色管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/roles")
@CrossOrigin
public class RoleController {
    
    @Resource
    private RoleMapper roleMapper;
    
    @Resource
    private com.iot.platform.mapper.UserMapper userMapper;
    
    @Resource
    private UserService userService;
    
    /**
     * 验证当前用户是否为超级管理员
     */
    private boolean isSuperAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        String tokenValue = token.substring(7);
        Long userId = userService.validateToken(tokenValue);
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role != null && "super_admin".equals(role.getRoleCode());
    }
    
    /**
     * 查询角色列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> list(@RequestBody(required = false) Map<String, Object> params) {
        try {
            if (params == null) {
                params = new HashMap<>();
            }
            
            Integer page = params.get("page") != null ? 
                Integer.valueOf(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                Integer.valueOf(params.get("pageSize").toString()) : 20;
            
            Page<Role> pageObj = new Page<>(page, pageSize);
            LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
            query.orderByDesc(Role::getCreateTime);
            
            Page<Role> result = roleMapper.selectPage(pageObj, query);
            
            // 构造返回数据，添加用户数和超管标志
            java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Role role : result.getRecords()) {
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("id", role.getId());
                item.put("name", role.getRoleName());
                item.put("code", role.getRoleCode());
                item.put("description", role.getDescription());
                item.put("status", role.getStatus());  // 0=禁用, 1=启用
                
                // 统计该角色的用户数
                LambdaQueryWrapper<com.iot.platform.entity.User> userQuery = new LambdaQueryWrapper<>();
                userQuery.eq(com.iot.platform.entity.User::getRoleId, role.getId());
                Long userCount = userMapper.selectCount(userQuery);
                item.put("userCount", userCount);
                
                // 判断是否是超级管理员角色
                boolean isSuperAdmin = "super_admin".equals(role.getRoleCode());
                item.put("isSuperAdmin", isSuperAdmin);
                
                // 格式化时间
                item.put("createTime", role.getCreateTime() != null ? 
                    role.getCreateTime().format(formatter) : null);
                list.add(item);
            }
            
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("list", list);
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("pageSize", result.getSize());
            data.put("totalPages", result.getPages());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询角色列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询角色详情
     */
    @PostMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            if (id == null) {
                return Result.error("角色ID不能为空");
            }
            
            Role role = roleMapper.selectById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            // 角色基本信息
            Map<String, Object> roleInfo = new java.util.LinkedHashMap<>();
            roleInfo.put("id", role.getId());
            roleInfo.put("name", role.getRoleName());
            roleInfo.put("code", role.getRoleCode());
            roleInfo.put("description", role.getDescription());
            roleInfo.put("isSuperAdmin", "super_admin".equals(role.getRoleCode()));
            
            // 统计用户数
            LambdaQueryWrapper<com.iot.platform.entity.User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(com.iot.platform.entity.User::getRoleId, role.getId());
            Long userCount = userMapper.selectCount(userQuery);
            roleInfo.put("userCount", userCount);
            
            roleInfo.put("createTime", role.getCreateTime() != null ? 
                role.getCreateTime().format(formatter) : null);
            
            // 所有可用菜单权限（树形结构），并标记已授权状态
            java.util.Set<String> grantedSet = new java.util.HashSet<>();
            if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
                String[] menuArr = role.getMenuIds().split(",");
                for (String menu : menuArr) {
                    grantedSet.add(menu.trim());
                }
            }
            
            java.util.List<Map<String, Object>> allPermissions = buildAllPermissions(grantedSet);
            
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("roleInfo", roleInfo);
            data.put("permissions", allPermissions);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询角色详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 构建所有可用权限树，并标记已授权状态
     * 注意：这里的权限code必须与登录接口返回的菜单code完全一致
     */
    private java.util.List<Map<String, Object>> buildAllPermissions(java.util.Set<String> grantedSet) {
        java.util.List<Map<String, Object>> permissions = new java.util.ArrayList<>();
        
        // 1. 数据概览
        Map<String, Object> overview = new java.util.LinkedHashMap<>();
        overview.put("code", "overview");
        overview.put("name", "数据概览");
        overview.put("icon", "📊");
        overview.put("sort", 1);
        overview.put("granted", grantedSet.contains("overview"));
        overview.put("children", null);
        permissions.add(overview);
        
        // 2. 设备管理（子权限：devices, video, groups）
        Map<String, Object> deviceManagement = new java.util.LinkedHashMap<>();
        deviceManagement.put("code", "device-management");
        deviceManagement.put("name", "设备管理");
        deviceManagement.put("icon", "📱");
        deviceManagement.put("sort", 2);
        deviceManagement.put("granted", grantedSet.contains("devices") || grantedSet.contains("video") || grantedSet.contains("groups"));
        java.util.List<Map<String, Object>> deviceChildren = new java.util.ArrayList<>();
        Map<String, Object> devices = new java.util.LinkedHashMap<>();
        devices.put("code", "devices");
        devices.put("name", "设备列表");
        devices.put("granted", grantedSet.contains("devices"));
        deviceChildren.add(devices);
        Map<String, Object> video = new java.util.LinkedHashMap<>();
        video.put("code", "video");
        video.put("name", "视频管理");
        video.put("granted", grantedSet.contains("video"));
        deviceChildren.add(video);
        Map<String, Object> groups = new java.util.LinkedHashMap<>();
        groups.put("code", "groups");
        groups.put("name", "设备分组");
        groups.put("granted", grantedSet.contains("groups"));
        deviceChildren.add(groups);
        deviceManagement.put("children", deviceChildren);
        permissions.add(deviceManagement);
        
        // 3. 能源管理（子权限：energy，子菜单：energy-statistics, energy-report, energy-trend）
        Map<String, Object> energyManagement = new java.util.LinkedHashMap<>();
        energyManagement.put("code", "energy");
        energyManagement.put("name", "能源管理");
        energyManagement.put("icon", "⚡");
        energyManagement.put("sort", 3);
        energyManagement.put("granted", grantedSet.contains("energy"));
        java.util.List<Map<String, Object>> energyChildren = new java.util.ArrayList<>();
        Map<String, Object> energyStatistics = new java.util.LinkedHashMap<>();
        energyStatistics.put("code", "energy-statistics");
        energyStatistics.put("name", "能源统计");
        energyStatistics.put("granted", grantedSet.contains("energy"));
        energyChildren.add(energyStatistics);
        Map<String, Object> energyReport = new java.util.LinkedHashMap<>();
        energyReport.put("code", "energy-report");
        energyReport.put("name", "能源报表");
        energyReport.put("granted", grantedSet.contains("energy"));
        energyChildren.add(energyReport);
        Map<String, Object> energyTrend = new java.util.LinkedHashMap<>();
        energyTrend.put("code", "energy-trend");
        energyTrend.put("name", "能源趋势");
        energyTrend.put("granted", grantedSet.contains("energy"));
        energyChildren.add(energyTrend);
        energyManagement.put("children", energyChildren);
        permissions.add(energyManagement);
        
        // 4. 报警管理（子权限：alarm-threshold, alarms）
        Map<String, Object> alarmManagement = new java.util.LinkedHashMap<>();
        alarmManagement.put("code", "alarm-management");
        alarmManagement.put("name", "报警管理");
        alarmManagement.put("icon", "⚠️");
        alarmManagement.put("sort", 4);
        alarmManagement.put("granted", grantedSet.contains("alarm-threshold") || grantedSet.contains("alarms"));
        java.util.List<Map<String, Object>> alarmChildren = new java.util.ArrayList<>();
        Map<String, Object> alarmThreshold = new java.util.LinkedHashMap<>();
        alarmThreshold.put("code", "alarm-threshold");
        alarmThreshold.put("name", "报警配置");
        alarmThreshold.put("granted", grantedSet.contains("alarm-threshold"));
        alarmChildren.add(alarmThreshold);
        Map<String, Object> alarms = new java.util.LinkedHashMap<>();
        alarms.put("code", "alarms");
        alarms.put("name", "报警统计");
        alarms.put("granted", grantedSet.contains("alarms"));
        alarmChildren.add(alarms);
        alarmManagement.put("children", alarmChildren);
        permissions.add(alarmManagement);
        
        // 5. 产品管理
        Map<String, Object> products = new java.util.LinkedHashMap<>();
        products.put("code", "products");
        products.put("name", "产品管理");
        products.put("icon", "📦");
        products.put("sort", 5);
        products.put("granted", grantedSet.contains("products"));
        products.put("children", null);
        permissions.add(products);
        
        // 6. 场景联动
        Map<String, Object> scene = new java.util.LinkedHashMap<>();
        scene.put("code", "scene");
        scene.put("name", "场景联动");
        scene.put("icon", "🔗");
        scene.put("sort", 6);
        scene.put("granted", grantedSet.contains("scene"));
        scene.put("children", null);
        permissions.add(scene);
        
        // 7. 用户管理（超级管理员或有权限的角色可访问，但只有超级管理员可以创建/编辑/删除用户）
        Map<String, Object> users = new java.util.LinkedHashMap<>();
        users.put("code", "users");
        users.put("name", "用户管理");
        users.put("icon", "👥");
        users.put("sort", 7);
        users.put("granted", grantedSet.contains("users"));
        users.put("children", null);
        permissions.add(users);
        
        // 8. 角色管理（超级管理员或有权限的角色可访问，但只有超级管理员可以创建/编辑/删除角色）
        Map<String, Object> roles = new java.util.LinkedHashMap<>();
        roles.put("code", "roles");
        roles.put("name", "角色管理");
        roles.put("icon", "🎭");
        roles.put("sort", 8);
        roles.put("granted", grantedSet.contains("roles"));
        roles.put("children", null);
        permissions.add(roles);
        
        return permissions;
    }
    
    /**
     * 创建按钮级别权限
     */
    private Map<String, Object> createAction(String code, String name, boolean granted) {
        Map<String, Object> action = new java.util.LinkedHashMap<>();
        action.put("code", code);
        action.put("name", name);
        action.put("granted", granted);
        return action;
    }
    
    /**
     * 创建角色
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody CreateRoleRequest request) {
        try {
            // 验证权限：只有超级管理员可以创建角色
            if (!isSuperAdmin(token)) {
                return Result.error("无权限操作，只有超级管理员可以创建角色");
            }
            
            // 验证必填字段
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error("角色名称不能为空");
            }
            
            Role role = new Role();
            role.setRoleName(request.getName());
            
            // 自动生成角色编码：使用ROLE_前缀+时间戳
            String code = request.getCode();
            if (code == null || code.trim().isEmpty()) {
                code = "ROLE_" + System.currentTimeMillis();
            }
            role.setRoleCode(code);
            role.setDescription(request.getDescription());
            
            // 处理权限数据：支持菜单+按钮级别权限
            java.util.List<String> allPermissions = new java.util.ArrayList<>();
            if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
                for (PermissionItem item : request.getPermissions()) {
                    if (item.getGranted() != null && item.getGranted()) {
                        // 菜单级别权限
                        allPermissions.add(item.getCode());
                        
                        // 按钮级别权限
                        if (item.getActions() != null && !item.getActions().isEmpty()) {
                            for (String action : item.getActions()) {
                                allPermissions.add(item.getCode() + ":" + action);
                            }
                        }
                    }
                }
            }
            
            role.setMenuIds(String.join(",", allPermissions));
            role.setDataScope(2); // 默认仅本人
            role.setStatus(1); // 默认启用
            role.setCreateTime(LocalDateTime.now());
            
            roleMapper.insert(role);
            
            // 返回创建结果
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", role.getId());
            result.put("name", role.getRoleName());
            result.put("code", role.getRoleCode());
            result.put("description", role.getDescription());
            result.put("status", role.getStatus());
            result.put("createTime", role.getCreateTime() != null ? 
                role.getCreateTime().format(formatter) : null);
            
            return Result.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新角色
     */
    @PostMapping("/update")
    public Result<Void> update(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody UpdateRoleRequest request) {
        try {
            // 验证权限：只有超级管理员可以更新角色
            if (!isSuperAdmin(token)) {
                return Result.error("无权限操作，只有超级管理员可以更新角色");
            }
            
            if (request.getId() == null) {
                return Result.error("角色ID不能为空");
            }
            
            Role role = roleMapper.selectById(request.getId());
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            // 禁止编辑超级管理员角色
            if ("super_admin".equals(role.getRoleCode())) {
                return Result.error("超级管理员角色不允许编辑");
            }
            
            if (request.getName() != null) {
                role.setRoleName(request.getName());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
            
            // 处理权限更新（支持两种方式）
            // 方式1：直接传menuIds字符串
            if (request.getMenuIds() != null) {
                role.setMenuIds(request.getMenuIds());
            }
            // 方式2：传permissions数组
            else if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
                java.util.List<String> allPermissions = new java.util.ArrayList<>();
                for (PermissionItem item : request.getPermissions()) {
                    if (item.getGranted() != null && item.getGranted()) {
                        // 菜单级别权限
                        allPermissions.add(item.getCode());
                        
                        // 按钮级别权限
                        if (item.getActions() != null && !item.getActions().isEmpty()) {
                            for (String action : item.getActions()) {
                                allPermissions.add(item.getCode() + ":" + action);
                            }
                        }
                    }
                }
                role.setMenuIds(String.join(",", allPermissions));
            }
            
            roleMapper.updateById(role);
            
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除角色
     */
    @PostMapping("/delete")
    public Result<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Long> params) {
        try {
            // 验证权限：只有超级管理员可以删除角色
            if (!isSuperAdmin(token)) {
                return Result.error("无权限操作，只有超级管理员可以删除角色");
            }
            
            Long id = params.get("id");
            if (id == null) {
                return Result.error("角色ID不能为空");
            }
            
            // 检查角色是否存在
            Role role = roleMapper.selectById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            
            // 禁止删除超级管理员角色
            if ("super_admin".equals(role.getRoleCode())) {
                return Result.error("超级管理员角色不允许删除");
            }
            
            roleMapper.deleteById(id);
            
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除角色失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    @Data
    public static class CreateRoleRequest {
        private String name;
        private String code;
        private String description;
        private List<PermissionItem> permissions;
    }
    
    @Data
    public static class PermissionItem {
        private String code;
        private Boolean granted;
        private List<String> actions;
    }
    
    @Data
    public static class UpdateRoleRequest {
        private Long id;
        private String name;
        private String description;
        private String menuIds;  // 直接支持menuIds字符串
        private List<PermissionItem> permissions;  // 也支持permissions数组
    }
}
