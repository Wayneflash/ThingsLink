package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.Notification;
import com.iot.platform.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务
 */
@Slf4j
@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    
    /**
     * 创建通知消息（报警触发时调用）
     * 
     * @param userId 接收用户ID
     * @param alarmId 报警ID
     * @param deviceId 设备ID
     * @param deviceCode 设备编码
     * @param deviceName 设备名称
     * @param alarmLevel 报警级别
     * @param alarmMessage 报警消息
     */
    public void createNotification(Long userId, Long alarmId, Long deviceId, 
                                   String deviceCode, String deviceName, 
                                   String alarmLevel, String alarmMessage) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setAlarmId(alarmId);
        notification.setDeviceId(deviceId);
        notification.setDeviceCode(deviceCode);
        notification.setDeviceName(deviceName);
        notification.setAlarmLevel(alarmLevel);
        notification.setAlarmMessage(alarmMessage);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        
        this.save(notification);
        log.info("创建通知消息 - 用户ID: {}, 报警ID: {}, 设备: {}", userId, alarmId, deviceName);
    }
    
    /**
     * 分页查询用户通知列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param isRead 是否已读（null表示全部）
     * @return 通知列表
     */
    public Page<Notification> pageQuery(Long userId, int page, int pageSize, Integer isRead) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getCreateTime);
        
        return this.page(new Page<>(page, pageSize), wrapper);
    }
    
    /**
     * 获取用户未读通知数量
     * 
     * @param userId 用户ID
     * @return 未读数量
     */
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        
        return this.count(wrapper);
    }
    
    /**
     * 标记通知为已读
     * 
     * @param notificationId 通知ID
     * @return 是否成功
     */
    public boolean markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification == null) {
            return false;
        }
        
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        
        return this.updateById(notification);
    }
    
    /**
     * 批量标记为已读
     * 
     * @param notificationIds 通知ID列表
     * @return 成功数量
     */
    public int markAsReadBatch(List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }
        
        List<Notification> notifications = this.listByIds(notificationIds);
        if (notifications.isEmpty()) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(now);
        }
        
        this.updateBatchById(notifications);
        return notifications.size();
    }
    
    /**
     * 标记用户所有通知为已读
     * 
     * @param userId 用户ID
     * @return 成功数量
     */
    public int markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        
        List<Notification> notifications = this.list(wrapper);
        if (notifications.isEmpty()) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : notifications) {
            notification.setIsRead(1);
            notification.setReadTime(now);
        }
        
        this.updateBatchById(notifications);
        return notifications.size();
    }
}
