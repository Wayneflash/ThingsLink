package com.iot.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一上报数据模型
 * 将MQTT1.0和MQTT2.0格式转换为统一的数据结构
 */
@Data
public class UnifiedReportData {

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 属性列表
     */
    private List<PropertyData> properties;

    /**
     * 属性数据
     */
    @Data
    public static class PropertyData {
        /**
         * 属性标识符
         */
        private String name;

        /**
         * 属性值（字符串）
         */
        private String value;

        /**
         * 采集时间
         */
        private LocalDateTime ctime;
    }
}
