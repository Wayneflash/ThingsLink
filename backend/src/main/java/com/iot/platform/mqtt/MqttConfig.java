package com.iot.platform.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * MQTT 配置类
 */
@Slf4j
@Configuration
public class MqttConfig {
    
    @Value("${mqtt.broker-url}")
    private String brokerUrl;
    
    @Value("${mqtt.client-id}")
    private String clientId;
    
    @Value("${mqtt.username}")
    private String username;
    
    @Value("${mqtt.password}")
    private String password;
    
    @Value("${mqtt.connection-timeout:10}")
    private int connectionTimeout;
    
    @Value("${mqtt.keep-alive-interval:60}")
    private int keepAliveInterval;
    
    @Resource
    private MqttMessageHandler mqttMessageHandler;
    
    /**
     * 创建 MQTT 客户端（延迟连接，避免阻塞应用启动）
     */
    @Bean
    public MqttClient mqttClient() throws MqttException {
        // 创建客户端（不立即连接）
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        
        // 配置连接选项
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        
        // 设置回调
        client.setCallback(new MqttCallback() {
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
        
        // 异步连接到 MQTT Broker（避免阻塞应用启动）
        new Thread(() -> {
            int retryCount = 0;
            int maxRetries = 5;
            while (retryCount < maxRetries) {
                try {
                    Thread.sleep(2000); // 延迟2秒后再连接
                    client.connect(options);
                    log.info("MQTT 客户端连接成功: {}", brokerUrl);
                    
                    // 订阅设备上报主题
                    String topic = "ssc/+/report";
                    client.subscribe(topic, 1);
                    log.info("订阅主题成功: {}", topic);
                    break;
                    
                } catch (Exception e) {
                    retryCount++;
                    log.warn("MQTT 客户端连接失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                    if (retryCount >= maxRetries) {
                        log.error("MQTT 客户端连接失败，已达到最大重试次数");
                    } else {
                        try {
                            Thread.sleep(3000); // 每次重试间隔3秒
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }, "mqtt-connector").start();
        
        return client;
    }
}
