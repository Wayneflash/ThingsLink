package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Role;
import com.iot.platform.entity.User;
import com.iot.platform.service.UserService;
import com.iot.platform.mapper.RoleMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private RoleMapper roleMapper;
    
    @Resource
    private com.iot.platform.mapper.DeviceGroupMapper deviceGroupMapper;
    
    @Resource
    private com.iot.platform.mapper.UserMapper userMapper;
    
    /**
     * 获取当前用户信息
     */
    private User getCurrentUser(String token) {
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
     */
    private boolean isSuperAdmin(User user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role != null && "super_admin".equals(role.getRoleCode());
    }
    
    /**
     * 用户列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getUserList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) UserQueryRequest requestParam) {
        try {
            // 获取当前用户
            User currentUser = getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            
            // 判断是否为超级管理员
            boolean isSuper = isSuperAdmin(currentUser);
            
            // 如果requestParam为null，使用默认值
            final UserQueryRequest request = (requestParam != null) ? requestParam : new UserQueryRequest();
            
            Page<User> pageParam = new Page<>(
                request.getPage() != null ? request.getPage() : 1,
                request.getPageSize() != null ? request.getPageSize() : 20
            );
            
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            
            // 数据权限过滤：非超管只能查看本分组及下级分组的用户
            if (!isSuper) {
                Long userGroupId = currentUser.getGroupId() != null ? currentUser.getGroupId() : 0L;
                // TODO: 这里需要查询所有下级分组ID，暂时只限制本分组
                query.eq(User::getGroupId, userGroupId);
            }
            
            // 分组筛选
            if (request.getGroupId() != null) {
                query.eq(User::getGroupId, request.getGroupId());
            }
            
            // 关键词搜索（用户名或姓名）
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                query.and(wrapper -> wrapper
                    .like(User::getUsername, request.getKeyword())
                    .or()
                    .like(User::getRealName, request.getKeyword())
                );
            }
            
            // 状态筛选
            if (request.getStatus() != null) {
                query.eq(User::getStatus, request.getStatus());
            }
            
            query.orderByDesc(User::getCreateTime);
            
            IPage<User> page = userService.page(pageParam, query);
            
            // 构造返回数据（按原型格式）
            java.util.List<Map<String, Object>> userList = new java.util.ArrayList<>();
            page.getRecords().forEach(user -> {
                Map<String, Object> userMap = new java.util.LinkedHashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("realName", user.getRealName());  // 改为realName驼峰命名
                userMap.put("groupId", user.getGroupId());
                
                // 查询分组名称
                String groupName = "默认分组";  // 默认值
                if (user.getGroupId() != null && user.getGroupId() != 0) {
                    com.iot.platform.entity.DeviceGroup group = deviceGroupMapper.selectById(user.getGroupId());
                    if (group != null) {
                        groupName = group.getGroupName();  // 使用getGroupName()
                    }
                }
                userMap.put("groupName", groupName);
                
                // 查询用户角色
                Long roleId = user.getRoleId();
                String roleName = null;
                if (roleId != null) {
                    Role role = roleMapper.selectById(roleId);
                    if (role != null) {
                        roleName = role.getRoleName();
                    }
                }
                userMap.put("roleId", roleId);
                userMap.put("roleName", roleName);
                userMap.put("status", user.getStatus()); // 0=禁用, 1=启用
                userMap.put("createTime", user.getCreateTime());
                
                // 标记是否为超级管理员（admin用户）
                userMap.put("isSuper", "admin".equals(user.getUsername()));
                
                userList.add(userMap);
            });
            
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("total", page.getTotal());
            result.put("page", page.getCurrent());
            result.put("pageSize", page.getSize());
            result.put("list", userList);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建用户
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody CreateUserRequest request) {
        try {
            // 权限验证：只有超级管理员可以创建用户
            User currentUser = getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!isSuperAdmin(currentUser)) {
                return Result.error("无权限操作，只有超级管理员可以创建用户");
            }
            
            // 参数验证
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (request.getRealname() == null || request.getRealname().trim().isEmpty()) {
                return Result.error("姓名不能为空");
            }
            if (request.getRoleId() == null) {
                return Result.error("角色不能为空");
            }
            
            // 验证角色是否存在
            Role role = roleMapper.selectById(request.getRoleId());
            if (role == null) {
                return Result.error("角色不存在");
            }
            if (request.getGroupId() == null) {
                return Result.error("所属分组不能为空");
            }
            
            // 验证分组是否存在（0为根分组，始终有效）
            if (request.getGroupId() != 0) {
                com.iot.platform.entity.DeviceGroup group = deviceGroupMapper.selectById(request.getGroupId());
                if (group == null) {
                    return Result.error("所属分组不存在");
                }
            }
            
            // 禁止创建 admin 用户名
            if ("admin".equals(request.getUsername().trim())) {
                return Result.error("不允许创建 admin 用户名");
            }
            
            // 检查用户名是否已存在
            LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
            query.eq(User::getUsername, request.getUsername().trim());
            if (userMapper.selectCount(query) > 0) {
                return Result.error("用户名已存在");
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(request.getPassword()); // TODO: 密码加密
            user.setRealName(request.getRealname());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setGroupId(request.getGroupId()); // 必填
            user.setRoleId(request.getRoleId());
            user.setStatus(request.getStatus() != null ? request.getStatus() : 1); // 默认启用
            user.setCreateTime(java.time.LocalDateTime.now());
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            userMapper.insert(user);
            
            // 构造返回数据
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realname", user.getRealName());
            result.put("phone", user.getPhone());
            result.put("email", user.getEmail());
            result.put("groupId", user.getGroupId());
            result.put("roleId", user.getRoleId());
            result.put("status", user.getStatus());
            result.put("createTime", user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return Result.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    @Data
    public static class CreateUserRequest {
        private String username;   // 用户名（必填）
        private String password;   // 密码（必填）
        private String realname;   // 姓名（必填）
        private String phone;      // 手机号（可选）
        private String email;      // 邮箱（可选）
        private Long groupId;      // 分组ID（必填）
        private Long roleId;       // 角色ID（必填，单选）
        private Integer status;    // 状态（可选，默认1）
    }
    
    /**
     * 更新用户
     */
    @PostMapping("/update")
    public Result<Map<String, Object>> updateUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody UpdateUserRequest request) {
        try {
            // 权限验证：只有超级管理员可以编辑用户
            User currentUser = getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!isSuperAdmin(currentUser)) {
                return Result.error("无权限操作，只有超级管理员可以编辑用户");
            }
            
            // 参数验证
            if (request.getId() == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 查询用户是否存在
            User user = userMapper.selectById(request.getId());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 禁止编辑admin用户
            if ("admin".equals(user.getUsername())) {
                return Result.error("不允许编辑 admin 用户");
            }
            
            // 验证分组是否存在（如果传了groupId）
            if (request.getGroupId() != null && request.getGroupId() != 0) {
                com.iot.platform.entity.DeviceGroup group = deviceGroupMapper.selectById(request.getGroupId());
                if (group == null) {
                    return Result.error("所属分组不存在");
                }
            }
            
            // 验证角色是否存在（如果传了roleId）
            if (request.getRoleId() != null) {
                Role role = roleMapper.selectById(request.getRoleId());
                if (role == null) {
                    return Result.error("角色不存在");
                }
            }
            
            // 更新用户信息
            if (request.getRealname() != null) {
                user.setRealName(request.getRealname());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getGroupId() != null) {
                user.setGroupId(request.getGroupId());
            }
            if (request.getRoleId() != null) {
                user.setRoleId(request.getRoleId());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
            user.setUpdateTime(java.time.LocalDateTime.now());
            
            userMapper.updateById(user);
            
            // 构造返回数据
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realname", user.getRealName());
            result.put("phone", user.getPhone());
            result.put("email", user.getEmail());
            result.put("groupId", user.getGroupId());
            result.put("roleId", user.getRoleId());
            result.put("status", user.getStatus());
            result.put("updateTime", user.getUpdateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return Result.success(result, "更新成功");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    @Data
    public static class UpdateUserRequest {
        private Long id;           // 用户ID（必填）
        private String realname;   // 姓名（可选）
        private String phone;      // 手机号（可选）
        private String email;      // 邮箱（可选）
        private Long groupId;      // 分组ID（可选）
        private Long roleId;       // 角色ID（可选）
        private Integer status;    // 状态（可选）
    }
    
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public Result<Void> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Long> params) {
        try {
            // 权限验证：只有超级管理员可以删除用户
            User currentUser = getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!isSuperAdmin(currentUser)) {
                return Result.error("无权限操作，只有超级管理员可以删除用户");
            }
            
            Long id = params.get("id");
            if (id == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 查询用户是否存在
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 禁止删除admin用户
            if ("admin".equals(user.getUsername())) {
                return Result.error("不允许删除 admin 用户");
            }
            
            userMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁用/启用用户
     */
    @PostMapping("/status")
    public Result<Void> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody UpdateStatusRequest request) {
        try {
            // 权限验证：只有超级管理员可以启用/禁用用户
            User currentUser = getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!isSuperAdmin(currentUser)) {
                return Result.error("无权限操作，只有超级管理员可以启用/禁用用户");
            }
            
            // 参数验证
            if (request.getId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (request.getStatus() == null) {
                return Result.error("状态不能为空");
            }
            if (request.getStatus() != 0 && request.getStatus() != 1) {
                return Result.error("状态值无效，必须为0或1");
            }
            
            // 查询用户是否存在
            User user = userMapper.selectById(request.getId());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 禁止操作admin用户
            if ("admin".equals(user.getUsername())) {
                return Result.error("不允许操作 admin 用户");
            }
            
            // 更新状态
            user.setStatus(request.getStatus());
            user.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.updateById(user);
            
            return Result.success("状态已更新");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    @Data
    public static class UpdateStatusRequest {
        private Long id;         // 用户ID（必填）
        private Integer status;  // 状态（必填，1=启用，0=禁用）
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> params) {
        try {
            Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
            String newPassword = (String) params.get("newPassword");
            
            if (id == null || newPassword == null) {
                return Result.error("参数错误");
            }
            
            User user = new User();
            user.setId(id);
            user.setPassword(newPassword); // 注意：实际实现中需要加密存储
            userService.updateById(user);
            
            return Result.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户查询请求参数
     */
    @Data
    public static class UserQueryRequest {
        private Integer page;
        private Integer pageSize;
        private Long groupId;  // 分组ID筛选
        private String keyword;  // 关键词搜索
        private Integer status;  // 状态筛选
    }
}