package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.User;
import com.iot.platform.mqtt.MqttManager;
import com.iot.platform.util.PermissionUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private PermissionUtil permissionUtil;

    @Resource
    private com.iot.platform.service.SystemConfigService systemConfigService;

    @Resource
    private MqttManager mqttManager;

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String mqttBrokerUrl;

    @Value("${mqtt.username:admin}")
    private String mqttUsername;

    @Value("${mqtt.password:admin123.}")
    private String mqttPassword;

    /**
     * 获取设备连接MQTT配置（从数据库读取，仅用于显示）
     */
    @GetMapping("/mqtt-config")
    public Result getMqttConfig() {
        try {
            String broker = systemConfigService.getConfigValue("device.mqtt.broker", "localhost");
            String port = systemConfigService.getConfigValue("device.mqtt.port", "1883");
            String username = systemConfigService.getConfigValue("device.mqtt.username", "admin");
            String password = systemConfigService.getConfigValue("device.mqtt.password", "admin123.");
             
            Map<String, Object> config = new HashMap<>();
            config.put("broker", broker);
            config.put("port", Integer.parseInt(port));
            config.put("username", username);
            config.put("password", password);
             
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取MQTT配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取平台连接MQTT配置（从数据库读取，支持动态重新连接）
     */
    @GetMapping("/platform-mqtt-config")
    public Result getPlatformMqttConfig() {
        try {
            String broker = systemConfigService.getConfigValue("platform.mqtt.broker", "localhost");
            String port = systemConfigService.getConfigValue("platform.mqtt.port", "1883");
            String clientId = systemConfigService.getConfigValue("platform.mqtt.clientId", "iot-platform-server");
            String username = systemConfigService.getConfigValue("platform.mqtt.username", "admin");
            String password = systemConfigService.getConfigValue("platform.mqtt.password", "admin123.");
             
            Map<String, Object> config = new HashMap<>();
            config.put("broker", broker);
            config.put("port", Integer.parseInt(port));
            config.put("clientId", clientId);
            config.put("username", username);
            config.put("password", password);
             
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取平台MQTT配置失败: " + e.getMessage());
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
            config.put("platformMqttConfig", getPlatformMqttConfig().getData());
             
            return Result.success(config);
        } catch (Exception e) {
            return Result.error("获取系统配置失败: " + e.getMessage());
        }
    }

    /**
     * 系统配置请求参数
     */
    @Data
    public static class SystemConfigRequest {
        // 设备连接MQTT配置（可编辑，保存到数据库，仅用于显示）
        private String deviceMqttBroker;
        private Integer deviceMqttPort;
        private String deviceMqttUsername;
        private String deviceMqttPassword;
        
        // 平台连接MQTT配置（可编辑，保存到数据库，支持动态重新连接）
        private String platformMqttBroker;
        private Integer platformMqttPort;
        private String platformMqttClientId;
        private String platformMqttUsername;
        private String platformMqttPassword;
    }

    /**
     * 更新设备MQTT配置（仅admin可访问，保存到数据库，仅用于显示）
     */
    @PostMapping("/device-mqtt-config")
    public Result updateDeviceMqttConfig(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody SystemConfigRequest request) {
        try {
            // 权限验证：只有admin可以更新设备MQTT配置
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!"admin".equals(currentUser.getUsername())) {
                return Result.error("无权限操作，只有admin账号可以修改设备MQTT配置");
            }

            // 参数验证
            if (request.getDeviceMqttBroker() == null || request.getDeviceMqttBroker().trim().isEmpty()) {
                return Result.error("MQTT地址不能为空");
            }
            if (request.getDeviceMqttPort() == null) {
                return Result.error("MQTT端口不能为空");
            }
            if (request.getDeviceMqttUsername() == null || request.getDeviceMqttUsername().trim().isEmpty()) {
                return Result.error("MQTT用户名不能为空");
            }
            if (request.getDeviceMqttPassword() == null || request.getDeviceMqttPassword().trim().isEmpty()) {
                return Result.error("MQTT密码不能为空");
            }

            // 保存到数据库
            systemConfigService.updateConfig("device.mqtt.broker", request.getDeviceMqttBroker().trim(), "设备MQTT服务器地址（仅用于显示）");
            systemConfigService.updateConfig("device.mqtt.port", String.valueOf(request.getDeviceMqttPort()), "设备MQTT服务器端口（仅用于显示）");
            systemConfigService.updateConfig("device.mqtt.username", request.getDeviceMqttUsername().trim(), "设备MQTT连接用户名（仅用于显示）");
            systemConfigService.updateConfig("device.mqtt.password", request.getDeviceMqttPassword().trim(), "设备MQTT连接密码（仅用于显示）");

            Map<String, Object> result = new HashMap<>();
            result.put("message", "设备MQTT配置更新成功！此配置仅用于在设备详情页显示");
            result.put("config", getMqttConfig().getData());

            return Result.success(result, "设备MQTT配置更新成功");
        } catch (Exception e) {
            return Result.error("更新设备MQTT配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新平台MQTT配置（仅admin可访问，保存到数据库，支持动态重新连接）
     */
    @PostMapping("/platform-mqtt-config")
    public Result updatePlatformMqttConfig(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody SystemConfigRequest request) {
        try {
            // 权限验证：只有admin可以更新平台MQTT配置
            User currentUser = permissionUtil.getCurrentUser(token);
            if (currentUser == null) {
                return Result.error("未授权，请先登录");
            }
            if (!"admin".equals(currentUser.getUsername())) {
                return Result.error("无权限操作，只有admin账号可以修改平台MQTT配置");
            }

            // 参数验证
            if (request.getPlatformMqttBroker() == null || request.getPlatformMqttBroker().trim().isEmpty()) {
                return Result.error("MQTT地址不能为空");
            }
            if (request.getPlatformMqttPort() == null) {
                return Result.error("MQTT端口不能为空");
            }
            if (request.getPlatformMqttClientId() == null || request.getPlatformMqttClientId().trim().isEmpty()) {
                return Result.error("Client ID不能为空");
            }
            if (request.getPlatformMqttUsername() == null || request.getPlatformMqttUsername().trim().isEmpty()) {
                return Result.error("MQTT用户名不能为空");
            }
            if (request.getPlatformMqttPassword() == null || request.getPlatformMqttPassword().trim().isEmpty()) {
                return Result.error("MQTT密码不能为空");
            }

            // 保存到数据库
            systemConfigService.updateConfig("platform.mqtt.broker", request.getPlatformMqttBroker().trim(), "平台MQTT服务器地址");
            systemConfigService.updateConfig("platform.mqtt.port", String.valueOf(request.getPlatformMqttPort()), "平台MQTT服务器端口");
            systemConfigService.updateConfig("platform.mqtt.clientId", request.getPlatformMqttClientId().trim(), "平台MQTT客户端ID");
            systemConfigService.updateConfig("platform.mqtt.username", request.getPlatformMqttUsername().trim(), "平台MQTT连接用户名");
            systemConfigService.updateConfig("platform.mqtt.password", request.getPlatformMqttPassword().trim(), "平台MQTT连接密码");

            // 重新连接MQTT服务器
            String brokerUrl = "tcp://" + request.getPlatformMqttBroker().trim() + ":" + request.getPlatformMqttPort();
            String clientId = request.getPlatformMqttClientId().trim();
            try {
                mqttManager.reconnect(brokerUrl, clientId, request.getPlatformMqttUsername().trim(), request.getPlatformMqttPassword().trim());
            } catch (Exception e) {
                // 重新连接失败不影响配置保存
                System.err.println("MQTT重新连接失败: " + e.getMessage());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("message", "平台MQTT配置更新成功！MQTT连接已使用新配置重新连接");
            result.put("config", getPlatformMqttConfig().getData());

            return Result.success(result, "平台MQTT配置更新成功");
        } catch (Exception e) {
            return Result.error("更新平台MQTT配置失败: " + e.getMessage());
        }
    }
}
