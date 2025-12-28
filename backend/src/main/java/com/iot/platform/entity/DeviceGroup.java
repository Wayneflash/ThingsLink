package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备分组实体
 */
@Data
@TableName("tb_device_group")
public class DeviceGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;        // 父分组ID（0为顶级）
    
    private String groupName;     // 分组名称
    
    private String groupType;     // 分组类型
    
    private String description;   // 分组描述
    
    private Integer sort;         // 排序
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
