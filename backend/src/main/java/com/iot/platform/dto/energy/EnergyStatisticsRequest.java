package com.iot.platform.dto.energy;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 能耗统计请求DTO
 */
@Data
public class EnergyStatisticsRequest {
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;
    
    /**
     * 设备ID（不传则查询所有设备）
     */
    private Long deviceId;
    
    /**
     * 分组ID（不传则查询所有分组）
     */
    private Long groupId;
    
    /**
     * 能源类型（electric/water/gas）
     */
    private String energyType;
}
