package com.iot.platform.dto.energy;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 能耗日统计DTO（数据库聚合查询结果）
 */
@Data
public class EnergyDailyStatDTO {
    /**
     * 统计日期（格式根据报表类型：yyyy-MM-dd / yyyy-MM / yyyy）
     */
    private String statDate;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 当天第一条数据的值（最早时间）
     */
    private BigDecimal startValue;
    
    /**
     * 当天最后一条数据的值（最晚时间）
     */
    private BigDecimal endValue;
    
    /**
     * 能耗值（endValue - startValue）
     */
    private BigDecimal consumption;
}
