package com.iot.platform.dto.energy;

import lombok.Data;
import java.util.List;

/**
 * 能耗报表响应VO
 */
@Data
public class EnergyReportVO {
    
    /**
     * 报表数据列表
     */
    private List<ReportRecordVO> records;
    
    /**
     * 总记录数
     */
    private Integer total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
    
    @Data
    public static class ReportRecordVO {
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
         * 日期
         */
        private String date;
        
        /**
         * 能耗值
         */
        private Double consumption;
        
        /**
         * 单位（kWh/m³）
         */
        private String unit;
    }
}
