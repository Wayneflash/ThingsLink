package com.iot.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备上报数据 DTO (MQTT2.0 格式)
 */
@Data
public class DeviceReportV2DTO {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 确认标志
     */
    private Integer ack;

    /**
     * 设备参数数组
     */
    private List<DeviceParam> params;

    /**
     * 设备参数
     */
    @Data
    public static class DeviceParam {
        /**
         * 设备编码
         */
        private String clientID;

        /**
         * 属性数组
         */
        private List<Property> properties;
    }

    /**
     * 属性对象
     */
    @Data
    public static class Property {
        /**
         * 属性名称
         */
        private String name;

        /**
         * 属性值
         */
        private Object value;

        /**
         * Unix时间戳（秒）
         */
        private Long timestamp;
    }
}
