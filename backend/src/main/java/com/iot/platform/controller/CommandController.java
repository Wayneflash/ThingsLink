package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Command;
import com.iot.platform.mapper.CommandMapper;
import com.iot.platform.mqtt.MqttPublisher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令下发 Controller
 * 异步下发模式：接口仅负责将命令发送到MQTT，不等待设备响应
 */
@Slf4j
@RestController
@RequestMapping("/commands")
@CrossOrigin
public class CommandController {
    
    @Resource
    private MqttPublisher mqttPublisher;
    
    @Resource
    private CommandMapper commandMapper;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 查询产品命令列表
     */
    @PostMapping("/product")
    public Result<Map<String, Object>> getProductCommands(@RequestBody Map<String, Long> request) {
        try {
            Long productId = request.get("productId");
            if (productId == null) {
                return Result.error("产品ID不能为空");
            }
            
            LambdaQueryWrapper<Command> query = new LambdaQueryWrapper<>();
            query.eq(Command::getProductId, productId);
            List<Command> commands = commandMapper.selectList(query);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", commands);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询产品命令列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 下发命令给设备（异步，不等待设备响应）
     * 
     * @param request 命令请求参数
     * @return 命令下发结果（仅表示MQTT发送成功，不代表设备执行成功）
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendCommand(@RequestBody SendCommandRequest request) {
        try {
            log.info("接收到命令下发请求 - 设备: {}, 命令数量: {}", 
                    request.getDeviceCode(), request.getCommands() != null ? request.getCommands().size() : 0);
            
            if (request.getCommands() == null || request.getCommands().isEmpty()) {
                return Result.error("命令列表不能为空");
            }
            
            // 生成命令唯一ID
            String commandId = "CMD" + System.currentTimeMillis();
            
            // 批量下发命令到MQTT（不等待设备响应）
            for (CommandItem cmd : request.getCommands()) {
                mqttPublisher.sendCommand(request.getDeviceCode(), cmd.getAddr(), cmd.getAddrv());
            }
            
            // 立即返回下发成功响应
            Map<String, Object> data = new HashMap<>();
            data.put("commandId", commandId);
            data.put("status", "已下发");
            data.put("deviceCode", request.getDeviceCode());
            data.put("sendTime", LocalDateTime.now().format(FORMATTER));
            data.put("message", "命令已通过MQTT下发到设备，设备收到后会自行执行");
            
            log.info("命令下发成功 - CommandId: {}, 设备: {}", commandId, request.getDeviceCode());
            
            return Result.success(data, "success");
            
        } catch (Exception e) {
            log.error("命令下发失败 - 设备: {}", request.getDeviceCode(), e);
            return Result.error("命令下发失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送命令请求参数
     */
    @Data
    public static class SendCommandRequest {
        private String deviceCode;
        private List<CommandItem> commands;
    }
    
    /**
     * 命令项
     */
    @Data
    public static class CommandItem {
        private String addr;
        private String addrv;
    }
}