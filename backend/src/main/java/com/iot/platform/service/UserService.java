package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.Role;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.RoleMapper;
import com.iot.platform.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private RoleMapper roleMapper;
    
    private static final String TOKEN_PREFIX = "user:token:";
    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:token:";
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        // 查询用户（注意：MySQL默认不区分大小写）
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, username);
        User user = this.getOne(query);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 严格校验用户名大小写（防止MySQL不区分大小写的问题）
        if (!user.getUsername().equals(username)) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码（明文比对）
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        
        // 生成Token和RefreshToken
        String token = UUID.randomUUID().toString().replace("-", "");
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        
        // 存储Token到Redis（24小时过期）
        String tokenKey = TOKEN_PREFIX + token;
        stringRedisTemplate.opsForValue().set(tokenKey, String.valueOf(user.getId()), 24, TimeUnit.HOURS);
        
        // 存储RefreshToken到Redis（7天过期）
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        stringRedisTemplate.opsForValue().set(refreshTokenKey, String.valueOf(user.getId()), 7, TimeUnit.DAYS);
        
        log.info("用户登录成功: {}", username);
        
        // 构造返回数据（按照API文档规约顺序）
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("expiresIn", 7200);
        
        // 构造用户信息
        Map<String, Object> userInfo = new java.util.LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("avatar", null);  // 当前版本暂无头像功能
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("roleId", user.getRoleId());
        userInfo.put("status", user.getStatus());  // 0=禁用, 1=启用
        result.put("user", userInfo);
        
        // 获取用户角色和菜单权限
        List<Map<String, Object>> menus = getUserMenus(user);
        result.put("menus", menus);
        
        return result;
    }
    
    /**
     * 获取用户菜单权限
     */
    private List<Map<String, Object>> getUserMenus(User user) {
        List<Map<String, Object>> menus = new ArrayList<>();
        
        // 获取用户角色
        if (user.getRoleId() == null) {
            return menus; // 无角色返回空菜单
        }
        
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            return menus; // 角色不存在返回空菜单
        }
        
        // 判断是否为超级管理员
        boolean isSuperAdmin = "super_admin".equals(role.getRoleCode());
        
        // 解析角色权限
        Set<String> permissions = new HashSet<>();
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            String[] menuArr = role.getMenuIds().split(",");
            for (String menu : menuArr) {
                permissions.add(menu.trim());
            }
        }
        
        // 根据权限构建菜单树
        int sort = 1;
        
        // 1. 设备概览
        if (isSuperAdmin || permissions.contains("overview")) {
            menus.add(createMenu("overview", "设备概览", "/overview", "DataAnalysis", null, sort++));
        }
        
        // 2. 设备分组
        if (isSuperAdmin || permissions.contains("groups")) {
            menus.add(createMenu("groups", "设备分组", "/groups", "FolderOpened", null, sort++));
        }
        
        // 3. 设备管理
        if (isSuperAdmin || permissions.contains("devices")) {
            menus.add(createMenu("devices", "设备管理", "/devices", "Monitor", null, sort++));
        }
        
        // 4. 产品管理
        if (isSuperAdmin || permissions.contains("products")) {
            menus.add(createMenu("products", "产品管理", "/products", "Box", null, sort++));
        }
        
        // 5. 报警日志
        if (isSuperAdmin || permissions.contains("alarms")) {
            menus.add(createMenu("alarms", "报警日志", "/alarms", "BellFilled", null, sort++));
        }
        
        // 6. 数据查询
        if (isSuperAdmin || permissions.contains("data-query")) {
            menus.add(createMenu("data-query", "数据查询", "/data-query", "Search", null, sort++));
        }
        
        // 7. 用户管理（仅超级管理员或有权限的角色）
        if (isSuperAdmin || permissions.contains("users")) {
            menus.add(createMenu("users", "用户管理", "/users", "User", null, sort++));
        }
        
        // 8. 角色管理（仅超级管理员或有权限的角色）
        if (isSuperAdmin || permissions.contains("roles")) {
            menus.add(createMenu("roles", "角色管理", "/roles", "Setting", null, sort++));
        }
        
        return menus;
    }
    
    /**
     * 创建菜单对象
     */
    private Map<String, Object> createMenu(String code, String name, String path, 
                                            String icon, List<Map<String, Object>> children, int sort) {
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("code", code);
        menu.put("name", name);
        menu.put("path", path);
        menu.put("icon", icon);
        menu.put("sort", sort);
        if (children != null && !children.isEmpty()) {
            menu.put("children", children);
        } else {
            menu.put("children", null);
        }
        return menu;
    }
    
    /**
     * 刷新Token
     */
    public String refreshToken(String refreshToken) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = stringRedisTemplate.opsForValue().get(refreshTokenKey);
        
        if (userId == null) {
            throw new RuntimeException("RefreshToken无效或已过期");
        }
        
        // 生成新Token
        String newToken = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = TOKEN_PREFIX + newToken;
        stringRedisTemplate.opsForValue().set(tokenKey, userId, 24, TimeUnit.HOURS);
        
        // 刷新RefreshToken过期时间
        stringRedisTemplate.expire(refreshTokenKey, 7, TimeUnit.DAYS);
        
        log.info("用户 {} 刷新Token成功", userId);
        return newToken;
    }
    
    /**
     * 用户登出
     */
    public void logout(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        stringRedisTemplate.delete(tokenKey);
        log.info("用户登出成功");
    }
    
    /**
     * 验证Token
     */
    public Long validateToken(String token) {
        String tokenKey = TOKEN_PREFIX + token;
        String userId = stringRedisTemplate.opsForValue().get(tokenKey);
        if (userId == null) {
            return null;
        }
        // 刷新Token过期时间
        stringRedisTemplate.expire(tokenKey, 24, TimeUnit.HOURS);
        return Long.parseLong(userId);
    }
    
    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, user.getUsername());
        if (this.count(query) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码直接存储（明文）
        // user.setPassword(user.getPassword());
        user.setStatus(1);  // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        this.save(user);
        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码（明文比对）
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码（明文）
        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        
        log.info("用户 {} 修改密码成功", user.getUsername());
    }
}
