package com.iot.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备告警配置请求
 */
@Data
public class DeviceAlarmConfigRequest {
    
    /**
     * 设备ID（单个配置时使用）
     */
    private Long deviceId;
    
    /**
     * 设备ID列表（批量配置时使用）
     */
    private List<Long> deviceIds;
    
    /**
     * 告警配置
     */
    private AlarmConfigDTO alarmConfig;
    
    /**
     * 是否启用告警
     */
    private Boolean enabled;
}
