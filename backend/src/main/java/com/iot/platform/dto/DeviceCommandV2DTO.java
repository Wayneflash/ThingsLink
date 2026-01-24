package com.iot.platform.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * 设备命令下发 DTO (MQTT2.0 格式)
 * 符合 MQTT通信协议说明.md 中定义的下发格式
 */
@Data
public class DeviceCommandV2DTO {
    
    /**
     * 消息ID号，字符串类型的数字，值范围：0-4294967295，每条消息ID应唯一
     */
    @JSONField(ordinal = 1)
    private String id;
    
    /**
     * 版本号，固定为 "1.0"（注意：下发时没有V前缀）
     */
    @JSONField(ordinal = 2)
    private String version;
    
    /**
     * 确认标志：
     * 0: 云端不返回响应数据（下发时使用）
     * 1: 云端返回响应数据
     */
    @JSONField(ordinal = 3)
    private Integer ack;
    
    /**
     * 请求入参数组（jsonObjectArray），包含设备信息
     */
    @JSONField(ordinal = 4)
    private List<DeviceParam> params;
    
    /**
     * 设备参数（params数组元素）
     */
    @Data
    public static class DeviceParam {
        /**
         * 设备编码（如："11223344"）
         */
        private String clientID;
        
        /**
         * 属性数组，包含控制命令
         */
        private List<Property> properties;
    }
    
    /**
     * 属性对象（properties数组元素）
     */
    @Data
    public static class Property {
        /**
         * 属性名称（如："window"）
         */
        private String name;
        
        /**
         * 属性值，可以是数字、字符串、布尔值或对象等类型（如："12345", 123, true）
         * 下发时 value 为字符串类型
         */
        private Object value;
    }
}
