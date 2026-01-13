package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.User;
import com.iot.platform.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证 Controller
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Resource
    private UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Map<String, Object> loginResult = userService.login(request.getUsername(), request.getPassword());
            return Result.success(loginResult, "登录成功");
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);
            userService.logout(actualToken);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> params) {
        try {
            String refreshToken = params.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return Result.error("refreshToken不能为空");
            }
            
            String newToken = userService.refreshToken(refreshToken);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("expiresIn", 7200);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @PostMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);
            Long userId = userService.validateToken(actualToken);
            if (userId == null) {
                return Result.error("Token无效或已过期");
            }
            
            User user = userService.getById(userId);
            // 清除密码字段
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token,
                                       @RequestBody ChangePasswordRequest request) {
        try {
            String actualToken = extractToken(token);
            Long userId = userService.validateToken(actualToken);
            if (userId == null) {
                return Result.error("Token无效或已过期");
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            
            if (request.getNewPassword().length() < 6) {
                return Result.error("密码长度至少6位");
            }
            
            userService.changePassword(userId, request.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 登录请求
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
    
    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        private String newPassword;
    }
    
    /**
     * 从Authorization头中提取token（去掉Bearer前缀）
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader;
        }
        return authorizationHeader.substring(7);
    }
}