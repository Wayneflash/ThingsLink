package com.iot.platform.mqtt;

import com.alibaba.fastjson.JSON;
import com.iot.platform.dto.DeviceCommandDTO;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MQTT 发布服务
 */
@Slf4j
@Component
public class MqttPublisher {
    
    @Resource
    private MqttClient mqttClient;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
    /**
     * 发送命令给设备（异步，不等待设备响应）
     * 
     * @param deviceCode 设备编码
     * @param addr 属性标识符
     * @param addrv 控制值
     * @throws RuntimeException 如果MQTT消息发送失败
     */
    public void sendCommand(String deviceCode, String addr, String addrv) {
        try {
            // 构建命令 DTO
            DeviceCommandDTO commandDTO = new DeviceCommandDTO();
            commandDTO.setDid(deviceCode);
            commandDTO.setUtime(LocalDateTime.now().format(FORMATTER));
            
            DeviceCommandDTO.CommandContent content = new DeviceCommandDTO.CommandContent();
            content.setAddr(addr);
            content.setAddrv(addrv);
            content.setPid(deviceCode);
            
            commandDTO.setContent(java.util.Collections.singletonList(content));
            
            // 发布到 MQTT（异步，QoS=1确保至少送达一次）
            String topic = "ssc/" + deviceCode + "/command";
            String payload = JSON.toJSONString(commandDTO);
            
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(1);  // QoS=1：至少一次送达
            message.setRetained(false);  // 不保留消息
            
            // MQTT发布（异步操作，立即返回）
            mqttClient.publish(topic, message);
            
            log.info("✅ 命令已发送到MQTT - Topic: {}, Payload: {}", topic, payload);
            log.info("📤 设备端订阅此Topic即可接收命令: ssc/{}/command", deviceCode);
            
        } catch (MqttException e) {
            log.error("❌ MQTT命令发送失败 - DeviceCode: {}, Addr: {}, Addrv: {}", 
                     deviceCode, addr, addrv, e);
            throw new RuntimeException("MQTT命令发送失败: " + e.getMessage(), e);
        }
    }
}
