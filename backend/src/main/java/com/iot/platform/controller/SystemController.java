package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.mqtt.MqttConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String mqttBrokerUrl;

    @Value("${mqtt.username:admin}")
    private String mqttUsername;

    @Value("${mqtt.password:admin123.}")
    private String mqttPassword;

    /**
     * 获取MQTT配置
     */
    @GetMapping("/mqtt-config")
    public Result getMqttConfig() {
        try {
            // 解析broker URL，提取主机名和端口
            String broker = "localhost";
            int port = 1883;

            if (mqttBrokerUrl != null && !mqttBrokerUrl.isEmpty()) {
                // 从tcp://host:port中提取host和port
                if (mqttBrokerUrl.startsWith("tcp://")) {
                    String address = mqttBrokerUrl.substring(6); // 去掉"tcp://"
                    String[] parts = address.split(":");
                    if (parts.length >= 2) {
                        broker = parts[0];
                        try {
                            port = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            port = 1883; // 默认端口
                        }
                    }
                }
            }

            Map<String, Object> config = new HashMap<>();
            config.put("broker", broker);
            config.put("port", port);
            config.put("username", mqttUsername);
            config.put("password", mqttPassword);

            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取MQTT配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统配置
     */
    @GetMapping("/config")
    public Result getSystemConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("systemName", "IoT物联网平台");
            config.put("version", "1.0.0");
            config.put("mqttConfig", getMqttConfig().getData());

            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }
}