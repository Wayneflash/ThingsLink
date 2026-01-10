package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Notification;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 消息通知服务
 */
@Slf4j
@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {
    
    @Resource
    private MailService mailService;
    
    @Resource
    private SmsService smsService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private DeviceService deviceService;
    
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
        // 创建数据库通知记录
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
        
        // 检查是否需要发送邮件通知
        sendMailNotification(userId, alarmId, deviceId, deviceCode, deviceName, alarmLevel, alarmMessage);
        
        // 检查是否需要发送短信通知
        sendSmsNotification(userId, alarmId, deviceId, deviceCode, deviceName, alarmLevel, alarmMessage);
    }
    
    /**
     * 发送邮件通知
     */
    private void sendMailNotification(Long userId, Long alarmId, Long deviceId,
                                      String deviceCode, String deviceName,
                                      String alarmLevel, String alarmMessage) {
        try {
            // 获取设备信息，检查邮件通知是否启用
            Device device = deviceService.getById(deviceId);
            if (device == null || device.getAlarmConfig() == null || device.getAlarmConfig().trim().isEmpty()) {
                log.debug("设备不存在或未配置报警，跳过邮件通知 - 设备ID: {}", deviceId);
                return;
            }
            
            // 解析报警配置
            AlarmConfigDTO alarmConfig = com.alibaba.fastjson.JSON.parseObject(
                device.getAlarmConfig(), AlarmConfigDTO.class);
            
            // 检查邮件通知是否启用
            if (alarmConfig.getMailEnabled() == null || !alarmConfig.getMailEnabled()) {
                log.debug("邮件通知未启用，跳过发送 - 设备: {}", deviceName);
                return;
            }
            
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("用户不存在，无法发送邮件 - 用户ID: {}", userId);
                return;
            }
            
            // 检查用户是否有邮箱
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("用户未配置邮箱，无法发送邮件 - 用户ID: {}, 用户名: {}", userId, user.getUsername());
                return;
            }
            
            // 生成邮件内容
            String subject = "【物联网平台】设备报警通知 - " + getAlarmLevelText(alarmLevel);
            String content = buildAlarmEmailContent(user.getRealName() != null ? user.getRealName() : user.getUsername(),
                                                      deviceCode, deviceName, alarmLevel, alarmMessage);
            
            // 发送邮件
            boolean success = mailService.sendHtmlMail(user.getEmail(), subject, content);
            if (success) {
                log.info("报警邮件发送成功 - 收件人: {}, 设备: {}, 报警级别: {}",
                        user.getEmail(), deviceName, alarmLevel);
            } else {
                log.error("报警邮件发送失败 - 收件人: {}, 设备: {}", user.getEmail(), deviceName);
            }
            
        } catch (Exception e) {
            log.error("发送报警邮件失败 - 用户ID: {}, 设备: {}", userId, deviceName, e);
            // 邮件发送失败不影响通知记录
        }
    }
    
    /**
     * 构建报警邮件内容（HTML格式）
     * 使用短信平台模板格式：【ThingsLink】{user_name}，您的设备{device_code}在{trigger_time}触发{alarm_level}报警。报警详情：{alarm_message}。请登录平台查看详情并及时处理。
     */
    private String buildAlarmEmailContent(String userName, String deviceCode, String deviceName,
                                           String alarmLevel, String alarmMessage) {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String alarmLevelText = getAlarmLevelText(alarmLevel);
        String alarmLevelClass = getAlarmLevelClass(alarmLevel);
        
        // 使用短信平台模板格式作为邮件主要内容
        String mainContent = "【ThingsLink】" + userName + "，您的设备" + deviceCode + "在" + currentTime + "触发" + alarmLevelText + "报警。报警详情：" + alarmMessage + "。请登录平台查看详情并及时处理。";
        
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>设备报警通知</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }\n" +
                "        .header h1 { margin: 0; font-size: 24px; }\n" +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }\n" +
                "        .main-message { background: #fff; padding: 20px; border-radius: 8px; border-left: 4px solid #667eea; margin-bottom: 20px; line-height: 1.8; font-size: 15px; }\n" +
                "        .info-item { margin-bottom: 15px; padding: 12px; background: white; border-left: 4px solid #667eea; border-radius: 4px; }\n" +
                "        .info-label { font-weight: bold; color: #666; }\n" +
                "        .info-value { color: #333; margin-top: 5px; }\n" +
                "        .alarm-level { display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 14px; font-weight: bold; }\n" +
                "        .alarm-critical { background: #fee; color: #c00; }\n" +
                "        .alarm-warning { background: #fff3e0; color: #e65100; }\n" +
                "        .alarm-info { background: #e3f2fd; color: #1976d2; }\n" +
                "        .alarm-message { background: #fff3cd; padding: 15px; border-radius: 4px; border-left: 4px solid #ffc107; margin: 20px 0; }\n" +
                "        .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>设备报警通知</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <div class=\"main-message\">" + mainContent + "</div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <div class=\"info-label\">设备名称</div>\n" +
                "                <div class=\"info-value\">" + deviceName + "</div>\n" +
                "            </div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <div class=\"info-label\">设备编码</div>\n" +
                "                <div class=\"info-value\">" + deviceCode + "</div>\n" +
                "            </div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <div class=\"info-label\">报警级别</div>\n" +
                "                <div class=\"info-value\">\n" +
                "                    <span class=\"alarm-level " + alarmLevelClass + "\">" + alarmLevelText + "</span>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <div class=\"info-label\">触发时间</div>\n" +
                "                <div class=\"info-value\">" + currentTime + "</div>\n" +
                "            </div>\n" +
                "            <div class=\"alarm-message\">\n" +
                "                <div class=\"info-label\">报警内容</div>\n" +
                "                <div class=\"info-value\">" + alarmMessage + "</div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>此邮件由系统自动发送，请勿回复。</p>\n" +
                "            <p>发送时间：" + currentTime + "</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * 获取报警级别文本
     */
    private String getAlarmLevelText(String alarmLevel) {
        if (alarmLevel == null) return "未知";
        switch (alarmLevel.toLowerCase()) {
            case "critical": return "严重";
            case "warning": return "警告";
            case "info": return "提示";
            default: return alarmLevel;
        }
    }
    
    /**
     * 获取报警级别CSS类名
     */
    private String getAlarmLevelClass(String alarmLevel) {
        if (alarmLevel == null) return "alarm-info";
        switch (alarmLevel.toLowerCase()) {
            case "critical": return "alarm-critical";
            case "warning": return "alarm-warning";
            case "info": return "alarm-info";
            default: return "alarm-info";
        }
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
    
    /**
     * 发送短信通知
     */
    private void sendSmsNotification(Long userId, Long alarmId, Long deviceId,
                                     String deviceCode, String deviceName,
                                     String alarmLevel, String alarmMessage) {
        try {
            // 获取设备信息，检查短信通知是否启用
            Device device = deviceService.getById(deviceId);
            if (device == null || device.getAlarmConfig() == null || device.getAlarmConfig().trim().isEmpty()) {
                log.debug("设备不存在或未配置报警，跳过短信通知 - 设备ID: {}", deviceId);
                return;
            }
            
            // 解析报警配置
            AlarmConfigDTO alarmConfig = com.alibaba.fastjson.JSON.parseObject(
                device.getAlarmConfig(), AlarmConfigDTO.class);
            
            // 检查短信通知是否启用
            if (alarmConfig.getSmsEnabled() == null || !alarmConfig.getSmsEnabled()) {
                log.debug("短信通知未启用，跳过发送 - 设备: {}", deviceName);
                return;
            }
            
            // 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                log.warn("用户不存在，无法发送短信 - 用户ID: {}", userId);
                return;
            }
            
            // 检查用户是否有手机号
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                log.warn("用户未配置手机号，无法发送短信 - 用户ID: {}, 用户名: {}", userId, user.getUsername());
                return;
            }
            
            // 生成短信内容（使用短信平台模板）
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String alarmLevelText = getAlarmLevelText(alarmLevel);
            String smsContent = String.format("【ThingsLink】%s，您的设备%s在%s触发%s报警。报警详情：%s。请登录平台查看详情并及时处理。",
                    user.getRealName() != null ? user.getRealName() : user.getUsername(),
                    deviceCode,
                    currentTime,
                    alarmLevelText,
                    alarmMessage);
            
            // 发送短信
            boolean success = smsService.sendSms(user.getPhone(), smsContent);
            if (success) {
                log.info("报警短信发送成功 - 收件人: {}, 设备: {}, 报警级别: {}",
                        user.getPhone(), deviceName, alarmLevel);
            } else {
                log.error("报警短信发送失败 - 收件人: {}, 设备: {}", user.getPhone(), deviceName);
            }
            
        } catch (Exception e) {
            log.error("发送报警短信失败 - 用户ID: {}, 设备: {}", userId, deviceName, e);
            // 短信发送失败不影响通知记录
        }
    }
}
