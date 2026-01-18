package com.iot.platform.dto.energy;

import lombok.Data;
import java.util.List;

/**
 * 能耗统计响应VO
 */
@Data
public class EnergyStatisticsVO {
    
    /**
     * 总能耗
     */
    private Double totalConsumption;
    
    /**
     * 单位（kWh/m³）
     */
    private String unit;
    
    /**
     * 平均能耗
     */
    private Double avgConsumption;
    
    /**
     * 同比增长率（%）
     */
    private Double yoy;
    
    /**
     * 环比增长率（%）
     */
    private Double mom;
    
    /**
     * 能耗Top10设备列表
     */
    private List<TopDeviceVO> top10Devices;
    
    @Data
    public static class TopDeviceVO {
        /**
         * 设备ID
         */
        private Long deviceId;
        
        /**
         * 设备名称
         */
        private String deviceName;
        
        /**
         * 设备编码
         */
        private String deviceCode;
        
        /**
         * 总能耗
         */
        private Double totalConsumption;
        
        /**
         * 单位
         */
        private String unit;
        
        /**
         * 记录数
         */
        private Integer recordCount;
    }
}
