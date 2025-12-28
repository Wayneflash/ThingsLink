package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备数据实体
 */
@Data
@TableName("tb_device_data")
public class DeviceData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long deviceId;
    
    private String deviceCode;
    
    private String addr;
    
    private String addrv;
    
    private LocalDateTime ctime;
    
    private LocalDateTime receiveTime;
}
