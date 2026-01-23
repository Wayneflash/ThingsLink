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
     * 获取MQTT配置信息（供系统设置页面显示）
     */
    public static class MqttConfigInfo {
        private String broker;
        private int port;
        private String username;
        private String password;
        
        public MqttConfigInfo(String broker, int port, String username, String password) {
            this.broker = broker;
            this.port = port;
            this.username = username;
            this.password = password;
        }
        
        public String getBroker() { return broker; }
        public int getPort() { return port; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
    
    /**
     * 获取MQTT配置信息
     */
    public MqttConfigInfo getMqttConfigInfo() {
        // 从brokerUrl中提取地址和端口
        String broker = brokerUrl.replace("tcp://", "").split(":")[0];
        int port = 1883;
        try {
            String[] parts = brokerUrl.split(":");
            if (parts.length > 1) {
                port = Integer.parseInt(parts[parts.length - 1]);
            }
        } catch (Exception e) {
            port = 1883;
        }
        
        return new MqttConfigInfo(broker, port, username, password);
    }
    
    /**
     * 创建 MQTT 客户端（延迟连接，避免阻塞应用启动）
     * 
     * 重连策略：
     * 1. 启动后延迟2秒开始连接
     * 2. 快速重试阶段：前10次，每次间隔3秒
     * 3. 慢速重试阶段：之后每60秒重试一次，持续重试直到连接成功
     * 4. 连接成功后如果断开，Paho客户端会自动重连
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
        options.setAutomaticReconnect(true);  // 连接成功后断开时自动重连
        options.setCleanSession(false);
        
        // 设置回调
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("MQTT 连接断开，Paho客户端将自动重连: {}", cause.getMessage());
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
            final int FAST_RETRY_MAX = 10;        // 快速重试次数
            final int FAST_RETRY_INTERVAL = 3000; // 快速重试间隔（毫秒）
            final int SLOW_RETRY_INTERVAL = 60000; // 慢速重试间隔（毫秒）
            
            // 延迟2秒后开始连接
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            // 持续重试直到连接成功
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 如果已经连接，跳出循环
                    if (client.isConnected()) {
                        log.info("MQTT 客户端已连接");
                        break;
                    }
                    
                    client.connect(options);
                    log.info("MQTT 客户端连接成功: {}", brokerUrl);
                    
                    // 订阅设备上报主题
                    String topic = "ssc/+/report";
                    client.subscribe(topic, 1);
                    log.info("订阅主题成功: {}", topic);
                    break; // 连接成功，跳出循环
                    
                } catch (Exception e) {
                    retryCount++;
                    
                    // 判断使用快速还是慢速重试间隔
                    int interval;
                    if (retryCount <= FAST_RETRY_MAX) {
                        interval = FAST_RETRY_INTERVAL;
                        log.warn("MQTT 连接失败 (快速重试 {}/{}): {}，{}秒后重试", 
                                retryCount, FAST_RETRY_MAX, e.getMessage(), interval / 1000);
                    } else {
                        interval = SLOW_RETRY_INTERVAL;
                        log.warn("MQTT 连接失败 (慢速重试中，第{}次): {}，{}秒后重试", 
                                retryCount, e.getMessage(), interval / 1000);
                    }
                    
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.info("MQTT 连接线程被中断，停止重试");
                        break;
                    }
                }
            }
        }, "mqtt-connector").start();
        
        return client;
    }
}
