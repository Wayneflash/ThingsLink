package com.iot.platform.dto.energy;

import lombok.Data;

/**
 * 实时能耗请求DTO（Dashboard用）
 */
@Data
public class RealtimeEnergyRequest {
    
    /**
     * 时间范围（today/week/month）
     */
    private String timeRange;
}
