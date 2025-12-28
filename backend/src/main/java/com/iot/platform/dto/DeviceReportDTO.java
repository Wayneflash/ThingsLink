package com.iot.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备上报数据 DTO
 */
@Data
public class DeviceReportDTO {
    
    /**
     * 设备ID (Device ID / Gateway ID)
     */
    private String did;
    
    /**
     * 上报内容数组
     */
    private List<ReportContent> content;
    
    @Data
    public static class ReportContent {
        /**
         * 属性标识符
         */
        private String addr;
        
        /**
         * 属性值
         */
        private String addrv;
        
        /**
         * 采集时间 (UTC)
         */
        private String ctime;
        
        /**
         * 设备编码
         */
        private String pid;
    }
}
