package com.iot.platform.dto.energy;

import lombok.Data;

/**
 * 能耗趋势响应VO
 */
@Data
public class EnergyTrendVO {
    
    /**
     * 时间标签
     */
    private String timeLabel;
    
    /**
     * 能耗值
     */
    private Double consumption;
    
    /**
     * 单位（kWh/m³）
     */
    private String unit;
}
