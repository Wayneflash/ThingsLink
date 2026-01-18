package com.iot.platform.dto.energy;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 能耗趋势请求DTO
 */
@Data
public class EnergyTrendRequest {
    
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
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 能源类型（electric/water/gas）
     */
    private String energyType;
    
    /**
     * 时间格式（day/hour）
     */
    private String dateFormat;
}
