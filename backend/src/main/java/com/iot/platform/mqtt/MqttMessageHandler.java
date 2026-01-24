package com.iot.platform.mqtt;

import com.alibaba.fastjson.JSON;
import com.iot.platform.dto.DeviceReportDTO;
import com.iot.platform.dto.DeviceReportV2DTO;
import com.iot.platform.dto.UnifiedReportData;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.service.DeviceDataService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * MQTT 消息处理器
 * 支持 MQTT1.0 和 MQTT2.0 双版本协议
 */
@Slf4j
@Component
public class MqttMessageHandler {
    
    @Resource
    private DeviceDataService deviceDataService;
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private ProductService productService;
    
    /**
     * 处理 MQTT 消息
     */
    @Async
    public void handleMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            log.info("收到 MQTT 消息 - Topic: {}, Payload: {}", topic, payload);
            
            // 判断消息类型
            if (topic.endsWith("/report")) {
                // 设备上报数据
                handleDeviceReport(topic, payload);
            } else {
                log.warn("未知的消息主题: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("处理 MQTT 消息失败 - Topic: {}", topic, e);
        }
    }
    
    /**
     * 处理设备上报数据（支持双版本协议）
     * 从 topic 中提取设备编码，格式：ssc/{deviceCode}/report
     */
    private void handleDeviceReport(String topic, String payload) {
        try {
            // 从 topic 中提取设备编码
            // topic 格式：ssc/{deviceCode}/report
            String[] topicParts = topic.split("/");
            if (topicParts.length < 2) {
                log.error("无法从 topic 中提取设备编码: {}", topic);
                return;
            }
            String deviceCode = topicParts[topicParts.length - 2]; // 倒数第二部分是设备编码
            
            // 获取设备协议类型（从数据库实时查询，支持协议动态切换）
            String protocol = getDeviceProtocol(deviceCode);
            log.info("设备 {} 配置的协议: {}", deviceCode, protocol);
            
            // 根据协议类型解析数据（带容错机制）
            UnifiedReportData unifiedData = null;
            boolean parseSuccess = false;
            
            if ("MQTT2.0".equalsIgnoreCase(protocol)) {
                // 优先使用配置的MQTT2.0格式解析
                DeviceReportV2DTO v2DTO = parseV2Report(payload);
                if (v2DTO != null) {
                    unifiedData = convertV2ToUnified(v2DTO);
                    parseSuccess = true;
                    log.info("设备 {} 使用MQTT2.0格式解析成功", deviceCode);
                } else {
                    // MQTT2.0解析失败，尝试MQTT1.0格式（容错机制）
                    log.warn("设备 {} 配置为MQTT2.0，但MQTT2.0格式解析失败，尝试MQTT1.0格式: {}", deviceCode, payload);
                    DeviceReportDTO v1DTO = parseV1Report(payload);
                    if (v1DTO != null) {
                        unifiedData = convertV1ToUnified(v1DTO);
                        parseSuccess = true;
                        log.warn("设备 {} 使用MQTT1.0格式解析成功（容错模式）", deviceCode);
                    }
                }
            } else {
                // 优先使用配置的MQTT1.0格式解析
                DeviceReportDTO v1DTO = parseV1Report(payload);
                if (v1DTO != null) {
                    unifiedData = convertV1ToUnified(v1DTO);
                    parseSuccess = true;
                    log.info("设备 {} 使用MQTT1.0格式解析成功", deviceCode);
                } else {
                    // MQTT1.0解析失败，尝试MQTT2.0格式（容错机制）
                    log.warn("设备 {} 配置为MQTT1.0，但MQTT1.0格式解析失败，尝试MQTT2.0格式: {}", deviceCode, payload);
                    DeviceReportV2DTO v2DTO = parseV2Report(payload);
                    if (v2DTO != null) {
                        unifiedData = convertV2ToUnified(v2DTO);
                        parseSuccess = true;
                        log.warn("设备 {} 使用MQTT2.0格式解析成功（容错模式）", deviceCode);
                    }
                }
            }
            
            // 如果两种格式都解析失败，记录错误并返回
            if (!parseSuccess || unifiedData == null) {
                log.error("设备 {} 数据解析失败，两种协议格式都无法解析: {}", deviceCode, payload);
                return;
            }
            
            // 保存原始payload
            unifiedData.setRawPayload(payload);
            
            // 统一处理
            if (unifiedData.getDeviceCode() != null) {
                deviceDataService.handleUnifiedReport(unifiedData);
            } else {
                log.error("统一数据转换失败，设备编码为空");
            }
            
        } catch (Exception e) {
            log.error("处理设备上报数据失败: {}", payload, e);
        }
    }
    
    /**
     * 获取设备协议类型
     * 通过设备编码查询设备 → 产品 → protocol字段
     */
    private String getDeviceProtocol(String deviceCode) {
        try {
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                log.error("设备不存在: {}", deviceCode);
                return "MQTT1.0"; // 默认使用MQTT1.0
            }
            
            Product product = productService.getById(device.getProductId());
            if (product == null) {
                log.error("产品不存在: {}", device.getProductId());
                return "MQTT1.0"; // 默认使用MQTT1.0
            }
            
            String protocol = product.getProtocol();
            // 兼容旧数据：如果 protocol 是 "MQTT"，当作 "MQTT1.0" 处理
            if ("MQTT".equalsIgnoreCase(protocol)) {
                protocol = "MQTT1.0";
            }
            
            log.info("设备 {} 所属产品 {} 协议类型: {}", deviceCode, product.getProductName(), protocol);
            return protocol != null ? protocol : "MQTT1.0";
        } catch (Exception e) {
            log.error("获取设备协议信息失败，使用默认MQTT1.0 - 设备: {}", deviceCode, e);
            return "MQTT1.0"; // 默认使用MQTT1.0
        }
    }
    
    /**
     * 解析 MQTT1.0 格式的上报数据
     */
    private DeviceReportDTO parseV1Report(String payload) {
        try {
            DeviceReportDTO reportDTO = JSON.parseObject(payload, DeviceReportDTO.class);
            
            // 数据验证
            if (reportDTO == null || reportDTO.getDid() == null || reportDTO.getContent() == null) {
                log.error("MQTT1.0 设备上报数据格式错误: {}", payload);
                return null;
            }
            
            return reportDTO;
        } catch (Exception e) {
            log.error("解析 MQTT1.0 格式失败: {}", payload, e);
            return null;
        }
    }
    
    /**
     * 解析 MQTT2.0 格式的上报数据
     */
    private DeviceReportV2DTO parseV2Report(String payload) {
        try {
            log.debug("开始解析 MQTT2.0 格式数据: {}", payload);
            DeviceReportV2DTO reportDTO = JSON.parseObject(payload, DeviceReportV2DTO.class);
            
            // 数据验证
            if (reportDTO == null) {
                log.error("MQTT2.0 设备上报数据解析为 null: {}", payload);
                return null;
            }
            
            if (reportDTO.getParams() == null || reportDTO.getParams().isEmpty()) {
                log.error("MQTT2.0 设备上报数据格式错误，params 为空: {}", payload);
                return null;
            }
            
            // 验证第一个参数是否有 clientID
            DeviceReportV2DTO.DeviceParam firstParam = reportDTO.getParams().get(0);
            if (firstParam == null) {
                log.error("MQTT2.0 设备上报数据格式错误，第一个 param 为 null: {}", payload);
                return null;
            }
            
            if (firstParam.getClientID() == null || firstParam.getClientID().trim().isEmpty()) {
                log.error("MQTT2.0 设备上报数据缺少 clientID: {}", payload);
                return null;
            }
            
            // 验证 properties
            if (firstParam.getProperties() == null || firstParam.getProperties().isEmpty()) {
                log.warn("MQTT2.0 设备上报数据 properties 为空，设备编码: {}", firstParam.getClientID());
            } else {
                log.info("MQTT2.0 解析成功 - 设备编码: {}, 属性数量: {}, id: {}, version: {}, ack: {}", 
                    firstParam.getClientID(), 
                    firstParam.getProperties().size(),
                    reportDTO.getId(),
                    reportDTO.getVersion(),
                    reportDTO.getAck());
            }
            
            return reportDTO;
        } catch (Exception e) {
            log.error("解析 MQTT2.0 格式失败: {}", payload, e);
            return null;
        }
    }
    
    /**
     * 将 MQTT1.0 格式转换为统一数据格式
     */
    private UnifiedReportData convertV1ToUnified(DeviceReportDTO v1DTO) {
        UnifiedReportData unifiedData = new UnifiedReportData();
        unifiedData.setDeviceCode(v1DTO.getDid());
        
        List<UnifiedReportData.PropertyData> properties = new ArrayList<>();
        for (DeviceReportDTO.ReportContent content : v1DTO.getContent()) {
            UnifiedReportData.PropertyData property = new UnifiedReportData.PropertyData();
            property.setName(content.getAddr());
            property.setValue(content.getAddrv());
            
            // 解析采集时间
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime ctime = LocalDateTime.parse(content.getCtime(), formatter);
                property.setCtime(ctime);
            } catch (Exception e) {
                // 尝试解析另一种格式
                try {
                    java.time.format.DateTimeFormatter altFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime ctime = LocalDateTime.parse(content.getCtime(), altFormatter);
                    property.setCtime(ctime);
                } catch (Exception ex) {
                    log.warn("时间格式解析失败，使用当前时间: {}", content.getCtime());
                    property.setCtime(LocalDateTime.now());
                }
            }
            
            properties.add(property);
        }
        
        unifiedData.setProperties(properties);
        return unifiedData;
    }
    
    /**
     * 将 MQTT2.0 格式转换为统一数据格式
     */
    private UnifiedReportData convertV2ToUnified(DeviceReportV2DTO v2DTO) {
        UnifiedReportData unifiedData = new UnifiedReportData();
        
        // MQTT2.0 格式中，设备编码在 params[0].clientID
        if (v2DTO.getParams() != null && !v2DTO.getParams().isEmpty()) {
            DeviceReportV2DTO.DeviceParam firstParam = v2DTO.getParams().get(0);
            String deviceCode = firstParam.getClientID();
            unifiedData.setDeviceCode(deviceCode);
            
            List<UnifiedReportData.PropertyData> properties = new ArrayList<>();
            if (firstParam.getProperties() != null) {
                for (DeviceReportV2DTO.Property property : firstParam.getProperties()) {
                    UnifiedReportData.PropertyData unifiedProperty = new UnifiedReportData.PropertyData();
                    unifiedProperty.setName(property.getName());
                    
                    // 将 value 转换为字符串（支持数字、字符串等类型）
                    if (property.getValue() != null) {
                        // 如果是数字类型，保持精度；如果是字符串，直接使用
                        if (property.getValue() instanceof Number) {
                            // 对于整数，不显示小数点；对于小数，保留原格式
                            Number numValue = (Number) property.getValue();
                            if (numValue.doubleValue() == numValue.longValue()) {
                                unifiedProperty.setValue(String.valueOf(numValue.longValue()));
                            } else {
                                unifiedProperty.setValue(String.valueOf(numValue));
                            }
                        } else {
                            unifiedProperty.setValue(String.valueOf(property.getValue()));
                        }
                    } else {
                        unifiedProperty.setValue("");
                    }
                    
                    // 解析时间戳（Unix时间戳，秒）
                    if (property.getTimestamp() != null) {
                        try {
                            LocalDateTime ctime = LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(property.getTimestamp()),
                                ZoneId.systemDefault()
                            );
                            unifiedProperty.setCtime(ctime);
                            log.debug("MQTT2.0 属性转换 - 设备: {}, 属性: {}, 值: {}, 时间戳: {} -> {}", 
                                deviceCode, property.getName(), unifiedProperty.getValue(), 
                                property.getTimestamp(), ctime);
                        } catch (Exception e) {
                            log.warn("时间戳解析失败，使用当前时间 - 设备: {}, 属性: {}, 时间戳: {}", 
                                deviceCode, property.getName(), property.getTimestamp(), e);
                            unifiedProperty.setCtime(LocalDateTime.now());
                        }
                    } else {
                        log.warn("MQTT2.0 属性缺少时间戳，使用当前时间 - 设备: {}, 属性: {}", 
                            deviceCode, property.getName());
                        unifiedProperty.setCtime(LocalDateTime.now());
                    }
                    
                    properties.add(unifiedProperty);
                }
                
                log.info("MQTT2.0 格式转换完成 - 设备: {}, 属性数量: {}", deviceCode, properties.size());
            } else {
                log.warn("MQTT2.0 格式转换 - 设备 {} 的 properties 为空", deviceCode);
            }
            
            unifiedData.setProperties(properties);
        } else {
            log.error("MQTT2.0 格式转换失败 - params 为空");
        }
        
        return unifiedData;
    }
}
