package com.iot.platform.mqtt;

import com.alibaba.fastjson.JSON;
import com.iot.platform.dto.DeviceCommandDTO;
import com.iot.platform.dto.DeviceCommandV2DTO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MQTT 发布服务
 * 支持 MQTT1.0 和 MQTT2.0 两种协议格式（根据产品 protocol 字段选择）
 */
@Slf4j
@Component
public class MqttPublisher {
    
    @Resource
    private MqttClient mqttClient;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
    // 消息ID生成器（用于MQTT2.0格式）
    private static final AtomicLong messageIdCounter = new AtomicLong(0);
    
    /**
     * 发送命令给设备（异步，不等待设备响应）
     * 默认使用 MQTT1.0 格式（兼容旧代码）
     * 
     * @param deviceCode 设备编码
     * @param addr 属性标识符
     * @param addrv 控制值
     * @throws RuntimeException 如果MQTT消息发送失败
     */
    public void sendCommand(String deviceCode, String addr, String addrv) {
        // 默认使用 MQTT1.0 格式
        sendCommand(deviceCode, addr, addrv, "MQTT1.0");
    }
    
    /**
     * 发送命令给设备（支持指定协议版本）
     * 
     * @param deviceCode 设备编码
     * @param addr 属性标识符
     * @param addrv 控制值
     * @param protocol 协议类型：MQTT1.0 或 MQTT2.0
     * @throws RuntimeException 如果MQTT消息发送失败
     */
    public void sendCommand(String deviceCode, String addr, String addrv, String protocol) {
        try {
            String payload;
            
            if ("MQTT2.0".equalsIgnoreCase(protocol)) {
                // MQTT2.0 格式
                payload = buildV2Payload(deviceCode, addr, addrv);
                log.debug("使用 MQTT2.0 格式发送命令");
            } else {
                // MQTT1.0 格式（默认）
                payload = buildV1Payload(deviceCode, addr, addrv);
                log.debug("使用 MQTT1.0 格式发送命令");
            }
            
            // 发布到 MQTT（异步，QoS=1确保至少送达一次）
            String topic = "ssc/" + deviceCode + "/command";
            
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);  // QoS=1：至少一次送达
            message.setRetained(false);  // 不保留消息
            
            // MQTT发布（异步操作，立即返回）
            mqttClient.publish(topic, message);
            
            log.info("✅ 命令已发送到MQTT ({}) - Topic: {}, Payload: {}", protocol, topic, payload);
            
        } catch (MqttException e) {
            log.error("❌ MQTT命令发送失败 - DeviceCode: {}, Addr: {}, Addrv: {}, Protocol: {}", 
                     deviceCode, addr, addrv, protocol, e);
            throw new RuntimeException("MQTT命令发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建 MQTT1.0 格式的命令消息
     */
    private String buildV1Payload(String deviceCode, String addr, String addrv) {
        DeviceCommandDTO commandDTO = new DeviceCommandDTO();
        commandDTO.setDid(deviceCode);
        commandDTO.setUtime(LocalDateTime.now().format(FORMATTER));
        
        DeviceCommandDTO.CommandContent content = new DeviceCommandDTO.CommandContent();
        content.setAddr(addr);
        content.setAddrv(addrv);
        content.setPid(deviceCode);
        
        commandDTO.setContent(Collections.singletonList(content));
        
        return JSON.toJSONString(commandDTO);
    }
    
    /**
     * 构建 MQTT2.0 格式的命令消息
     */
    private String buildV2Payload(String deviceCode, String addr, String addrv) {
        DeviceCommandV2DTO commandDTO = new DeviceCommandV2DTO();
        
        // 生成唯一消息ID（范围：0-4294967295）
        long msgId = messageIdCounter.incrementAndGet() % 4294967296L;
        commandDTO.setId(String.valueOf(msgId));
        commandDTO.setVersion("1.0");  // 下发时版本号没有V前缀
        commandDTO.setAck(0);  // 下发时 ack=0
        
        DeviceCommandV2DTO.Property property = new DeviceCommandV2DTO.Property();
        property.setName(addr);
        property.setValue(addrv);  // 下发时 value 为字符串类型
        
        DeviceCommandV2DTO.DeviceParam param = new DeviceCommandV2DTO.DeviceParam();
        param.setClientID(deviceCode);
        param.setProperties(Collections.singletonList(property));
        
        commandDTO.setParams(Collections.singletonList(param));
        
        String payload = JSON.toJSONString(commandDTO);
        log.debug("构建 MQTT2.0 下发格式 - 设备: {}, 属性: {}, 值: {}, 消息ID: {}, Payload: {}", 
            deviceCode, addr, addrv, msgId, payload);
        
        return payload;
    }
}
