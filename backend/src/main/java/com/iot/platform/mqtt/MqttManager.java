package com.iot.platform.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MQTT 连接管理器
 * 支持动态重新连接MQTT服务器
 */
@Slf4j
@Component
public class MqttManager {
    
    private MqttClient mqttClient;
    
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;
    private int connectionTimeout = 10;
    private int keepAliveInterval = 60;
    
    @Resource
    private MqttMessageHandler mqttMessageHandler;
    
    /**
     * 初始化MQTT连接
     */
    public void init(String brokerUrl, String clientId, String username, String password) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        connect();
    }
    
    /**
     * 连接MQTT服务器
     */
    public void connect() {
        try {
            // 如果已有连接，先断开
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
            
            // 创建新连接
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            
            // 配置连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(connectionTimeout);
            options.setKeepAliveInterval(keepAliveInterval);
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            
            // 设置回调
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 连接断开，将自动重连: {}", cause.getMessage());
                }
                
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    mqttMessageHandler.handleMessage(topic, message);
                }
                
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.debug("消息发送完成: {}", token.getMessageId());
                }
            });
            
            // 连接
            mqttClient.connect(options);
            log.info("MQTT 客户端连接成功: {}", brokerUrl);
            
            // 订阅设备上报主题
            String topic = "ssc/+/report";
            mqttClient.subscribe(topic, 1);
            log.info("订阅主题成功: {}", topic);
            
        } catch (Exception e) {
            log.error("MQTT 客户端连接失败: {}", e.getMessage(), e);
            throw new RuntimeException("MQTT连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新连接MQTT服务器
     * @param brokerUrl MQTT服务器地址
     * @param clientId 客户端ID
     * @param username 用户名
     * @param password 密码
     */
    public void reconnect(String brokerUrl, String clientId, String username, String password) {
        log.info("开始重新连接MQTT服务器: {}", brokerUrl);
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        connect();
    }
    
    /**
     * 获取MQTT客户端
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
