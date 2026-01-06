package com.iot.platform.mqtt;

import com.alibaba.fastjson.JSON;
import com.iot.platform.dto.DeviceReportDTO;
import com.iot.platform.service.DeviceDataService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * MQTT 消息处理器
 */
@Slf4j
@Component
public class MqttMessageHandler {
    
    @Resource
    private DeviceDataService deviceDataService;
    
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
                handleDeviceReport(payload);
            } else {
                log.warn("未知的消息主题: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("处理 MQTT 消息失败 - Topic: {}", topic, e);
        }
    }
    
    /**
     * 处理设备上报数据
     */
    private void handleDeviceReport(String payload) {
        try {
            // 解析 JSON
            DeviceReportDTO reportDTO = JSON.parseObject(payload, DeviceReportDTO.class);
            
            // 数据验证
            if (reportDTO == null || reportDTO.getDid() == null || reportDTO.getContent() == null) {
                log.error("设备上报数据格式错误: {}", payload);
                return;
            }
            
            // 处理数据
            deviceDataService.handleDeviceReport(reportDTO);
            
        } catch (Exception e) {
            log.error("处理设备上报数据失败: {}", payload, e);
        }
    }
}
