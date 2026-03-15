package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场景联动规则实体
 */
@Data
@TableName("tb_scene_rule")
public class SceneRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 是否启用：1-启用，0-禁用
     */
    private Integer enabled;

    /**
     * 触发设备ID
     */
    private Long triggerDeviceId;

    /**
     * 触发属性地址（addr）
     */
    private String triggerAttr;

    /**
     * 操作符：gt-大于, lt-小于, gte-大于等于, lte-小于等于, eq-等于, neq-不等于
     */
    private String operator;

    /**
     * 触发阈值
     */
    private String triggerValue;

    /**
     * 目标设备ID
     */
    private Long targetDeviceId;

    /**
     * 目标属性地址（addr）
     */
    private String targetAttr;

    /**
     * 下发值
     */
    private String targetValue;

    /**
     * 触发次数
     */
    private Integer triggerCount;

    /**
     * 上次触发时间
     */
    private LocalDateTime lastTriggeredAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ========== 非数据库字段 ==========

    /**
     * 触发设备名称（关联查询）
     */
    @TableField(exist = false)
    private String triggerDeviceName;

    /**
     * 触发设备编码（关联查询）
     */
    @TableField(exist = false)
    private String triggerDeviceCode;

    /**
     * 目标设备名称（关联查询）
     */
    @TableField(exist = false)
    private String targetDeviceName;

    /**
     * 目标设备编码（关联查询）
     */
    @TableField(exist = false)
    private String targetDeviceCode;

    /**
     * 触发属性名称（关联查询）
     */
    @TableField(exist = false)
    private String triggerAttrName;

    /**
     * 触发属性单位（关联查询）
     */
    @TableField(exist = false)
    private String triggerAttrUnit;

    /**
     * 目标属性名称（关联查询）
     */
    @TableField(exist = false)
    private String targetAttrName;

    /**
     * 目标属性单位（关联查询）
     */
    @TableField(exist = false)
    private String targetAttrUnit;
}
