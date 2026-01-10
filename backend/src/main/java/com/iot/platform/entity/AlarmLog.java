package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警日志实体
 */
@Data
@TableName("tb_alarm_log")
public class AlarmLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 设备编码
     */
    private String deviceCode;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 监控指标（属性标识符）
     */
    private String metric;
    
    /**
     * 触发时的运算符（用于恢复判断）
     */
    @TableField("trigger_operator")
    private String triggerOperator;
    
    /**
     * 触发时的阈值（用于恢复判断）
     */
    @TableField("trigger_threshold")
    private Double triggerThreshold;
    
    /**
     * 通知人员ID列表（JSON格式）
     */
    @TableField("notify_users")
    private String notifyUsers;
    
    /**
     * 告警级别：critical（严重）、warning（警告）、info（提示）
     */
    private String alarmLevel;
    
    /**
     * 告警消息（描述触发的条件和实际值）
     */
    private String alarmMessage;
    
    /**
     * 触发时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime triggerTime;
    
    /**
     * 处理状态：0-未处理，1-已处理
     */
    private Integer status;
    
    /**
     * 是否已恢复：0-未恢复，1-已恢复
     */
    private Integer recovered;
    
    /**
     * 处理人
     */
    private String handler;
    
    /**
     * 处理描述
     */
    private String handleDescription;
    
    /**
     * 处理图片（JSON格式）
     */
    private String handleImages;
    
    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime handleTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
