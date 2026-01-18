package com.iot.platform.dto.energy;

import lombok.Data;
import java.util.List;

/**
 * 实时能耗响应VO（Dashboard用）
 */
@Data
public class RealtimeEnergyVO {
    
    /**
     * 电力数据
     */
    private EnergyData electric;
    
    /**
     * 水耗数据
     */
    private EnergyData water;
    
    /**
     * 气耗数据
     */
    private EnergyData gas;
    
    @Data
    public static class EnergyData {
        /**
         * 总能耗
         */
        private Double totalConsumption;
        
        /**
         * 单位（kWh/m³）
         */
        private String unit;
        
        /**
         * 当前功率（仅电力）
         */
        private Double currentPower;
        
        /**
         * 当前流量（仅水/气）
         */
        private Double currentFlow;
        
        /**
         * Top设备列表
         */
        private List<TopDeviceVO> topDevices;
    }
    
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
         * 能耗值
         */
        private Double consumption;
        
        /**
         * 单位
         */
        private String unit;
    }
}
