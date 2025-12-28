package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@TableName("tb_device")
public class Device implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String deviceCode;
    
    private String deviceName;
    
    private Long productId;
    
    private Long groupId;
    
    private Integer status;
    
    private LocalDateTime lastOnlineTime;
    
    private String location;
    
    private Integer offlineTimeout;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
