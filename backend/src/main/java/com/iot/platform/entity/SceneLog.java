package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景联动执行日志实体
 */
@Data
@TableName("tb_scene_log")
public class SceneLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称（冗余存储）
     */
    private String ruleName;

    /**
     * 触发设备ID
     */
    private Long triggerDeviceId;

    /**
     * 触发设备名称
     */
    private String triggerDeviceName;

    /**
     * 触发属性
     */
    private String triggerAttr;

    /**
     * 实际触发值
     */
    private String triggerValue;

    /**
     * 目标设备ID
     */
    private Long targetDeviceId;

    /**
     * 目标设备名称
     */
    private String targetDeviceName;

    /**
     * 目标属性
     */
    private String targetAttr;

    /**
     * 下发值
     */
    private String targetValue;

    /**
     * 执行结果：success-成功, failed-失败
     */
    private String executeResult;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
