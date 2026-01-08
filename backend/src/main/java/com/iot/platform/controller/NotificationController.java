package com.iot.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Notification;
import com.iot.platform.entity.User;
import com.iot.platform.service.NotificationService;
import com.iot.platform.util.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@CrossOrigin
public class NotificationController {
    
    @Resource
    private NotificationService notificationService;
    
    @Resource
    private PermissionUtil permissionUtil;
    
    /**
     * 分页查询通知列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getNotificationList(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> params) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 获取查询参数
            int page = 1;
            int pageSize = 20;
            Integer isRead = null;
            
            try {
                if (params.get("page") != null) {
                    Object pageObj = params.get("page");
                    if (pageObj instanceof Number) {
                        page = ((Number) pageObj).intValue();
                    } else {
                        page = Integer.parseInt(pageObj.toString());
                    }
                }
                if (params.get("pageSize") != null) {
                    Object pageSizeObj = params.get("pageSize");
                    if (pageSizeObj instanceof Number) {
                        pageSize = ((Number) pageSizeObj).intValue();
                    } else {
                        pageSize = Integer.parseInt(pageSizeObj.toString());
                    }
                }
                if (params.get("isRead") != null) {
                    Object isReadObj = params.get("isRead");
                    if (isReadObj instanceof Number) {
                        isRead = ((Number) isReadObj).intValue();
                    } else {
                        isRead = Integer.parseInt(isReadObj.toString());
                    }
                }
            } catch (Exception e) {
                log.warn("参数解析失败，使用默认值", e);
            }
            
            // 分页查询
            Page<Notification> pageResult = notificationService.pageQuery(
                    currentUser.getId(), page, pageSize, isRead);
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询通知列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取未读通知数量
     */
    @PostMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            long count = notificationService.getUnreadCount(currentUser.getId());
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 标记通知为已读
     */
    @PostMapping("/mark-read")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> params) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            // 获取通知ID
            Object notificationIdObj = params.get("notificationId");
            Object notificationIdsObj = params.get("notificationIds");
            
            if (notificationIdObj != null) {
                // 单个标记
                Long notificationId = Long.valueOf(notificationIdObj.toString());
                Notification notification = notificationService.getById(notificationId);
                if (notification == null || !notification.getUserId().equals(currentUser.getId())) {
                    return Result.error("通知不存在或无权限");
                }
                notificationService.markAsRead(notificationId);
            } else if (notificationIdsObj != null && notificationIdsObj instanceof List) {
                // 批量标记
                @SuppressWarnings("unchecked")
                List<Long> notificationIds = (List<Long>) notificationIdsObj;
                notificationService.markAsReadBatch(notificationIds);
            } else {
                return Result.error("通知ID不能为空");
            }
            
            return Result.success();
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public Result<Map<String, Object>> markAllAsRead(@RequestHeader("Authorization") String token) {
        try {
            // 获取当前用户
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("用户未登录");
            }
            
            int count = notificationService.markAllAsRead(currentUser.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("标记所有通知为已读失败", e);
            return Result.error(e.getMessage());
        }
    }
}
