package com.iot.platform.util;

import com.iot.platform.entity.Role;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.RoleMapper;
import com.iot.platform.mapper.UserMapper;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统一权限工具类
 * 
 * 功能：
 * 1. 统一获取当前用户
 * 2. 统一判断超级管理员
 * 3. 统一获取权限分组列表（本分组+所有下级分组）
 * 
 * 使用说明：
 * 所有Controller都应该使用此工具类进行权限判断，确保数据权限逻辑完全一致
 */
@Slf4j
@Component
public class PermissionUtil {
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private RoleMapper roleMapper;
    
    @Resource
    private DeviceService deviceService;
    
    /**
     * 从Token中获取当前用户信息
     * 
     * @param token Authorization请求头（格式：Bearer {token}）
     * @return 用户对象，如果token无效返回null
     */
    public User getCurrentUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String actualToken = token.substring(7);
        Long userId = userService.validateToken(actualToken);
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }
    
    /**
     * 判断用户是否为超级管理员
     * 
     * @param user 用户对象
     * @return true表示超级管理员，false表示普通用户
     */
    public boolean isSuperAdmin(User user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role != null && "super_admin".equals(role.getRoleCode());
    }
    
    /**
     * 判断用户是否为超级管理员（通过Token）
     * 
     * @param token Authorization请求头
     * @return true表示超级管理员，false表示普通用户或未登录
     */
    public boolean isSuperAdmin(String token) {
        User user = getCurrentUser(token);
        return isSuperAdmin(user);
    }
    
    /**
     * 获取用户权限范围内的分组ID列表
     * 
     * 权限规则：
     * - 超级管理员：返回null（表示不过滤，可查看所有数据）
     * - 普通用户：返回用户所属分组ID及所有下级分组ID列表
     * 
     * @param token Authorization请求头
     * @return 分组ID列表，超级管理员返回null
     */
    public List<Long> getAllowedGroupIds(String token) {
        User currentUser = getCurrentUser(token);
        if (currentUser == null) {
            return null; // 未登录用户，返回null表示不过滤（实际应该先验证登录）
        }
        
        // 超级管理员返回null，表示不过滤
        if (isSuperAdmin(currentUser)) {
            return null;
        }
        
        // 普通用户：获取用户所属分组及所有下级分组ID
        Long userGroupId = currentUser.getGroupId() != null ? currentUser.getGroupId() : 0L;
        List<Long> allowedGroupIds = deviceService.getAllSubGroupIds(userGroupId);
        log.debug("用户 {} 允许查看的分组ID: {}", currentUser.getUsername(), allowedGroupIds);
        
        return allowedGroupIds;
    }
    
    /**
     * 获取用户权限范围内的分组ID列表（通过User对象）
     * 
     * @param user 用户对象
     * @return 分组ID列表，超级管理员返回null
     */
    public List<Long> getAllowedGroupIds(User user) {
        if (user == null) {
            return null;
        }
        
        // 超级管理员返回null，表示不过滤
        if (isSuperAdmin(user)) {
            return null;
        }
        
        // 普通用户：获取用户所属分组及所有下级分组ID
        Long userGroupId = user.getGroupId() != null ? user.getGroupId() : 0L;
        List<Long> allowedGroupIds = deviceService.getAllSubGroupIds(userGroupId);
        log.debug("用户 {} 允许查看的分组ID: {}", user.getUsername(), allowedGroupIds);
        
        return allowedGroupIds;
    }
}

