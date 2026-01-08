package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("tb_notification")
public class Notification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 关联报警ID
     */
    @TableField("alarm_id")
    private Long alarmId;
    
    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;
    
    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;
    
    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;
    
    /**
     * 报警级别：critical（严重）、warning（警告）、info（提示）
     */
    @TableField("alarm_level")
    private String alarmLevel;
    
    /**
     * 报警消息
     */
    @TableField("alarm_message")
    private String alarmMessage;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;
    
    /**
     * 阅读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
