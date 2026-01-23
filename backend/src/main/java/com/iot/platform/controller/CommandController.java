package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Command;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.mapper.CommandMapper;
import com.iot.platform.mqtt.MqttPublisher;
import com.iot.platform.service.DeviceLogService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.ProductService;
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
 * 支持 MQTT1.0 和 MQTT2.0 两种协议格式（根据产品 protocol 字段自动选择）
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
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private ProductService productService;
    
    @Resource
    private DeviceLogService deviceLogService;
    
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
            log.info("========== 命令下发接口被调用 ==========");
            log.info("接收到命令下发请求 - 设备: {}, 命令数量: {}",
                    request.getDeviceCode(), request.getCommands() != null ? request.getCommands().size() : 0);
            
            if (request.getCommands() == null || request.getCommands().isEmpty()) {
                return Result.error("命令列表不能为空");
            }
            
            // 获取设备信息
            Device device = deviceService.getByDeviceCode(request.getDeviceCode());
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 获取产品信息，确定协议类型（MQTT1.0 或 MQTT2.0）
            String protocol = "MQTT1.0"; // 默认使用 MQTT1.0（兼容旧数据）
            if (device.getProductId() != null) {
                Product product = productService.getById(device.getProductId());
                if (product != null && product.getProtocol() != null) {
                    String productProtocol = product.getProtocol();
                    // 兼容旧数据：如果 protocol 是 "MQTT"，当作 "MQTT1.0" 处理
                    if ("MQTT".equalsIgnoreCase(productProtocol)) {
                        protocol = "MQTT1.0";
                    } else if ("MQTT1.0".equalsIgnoreCase(productProtocol) || "MQTT2.0".equalsIgnoreCase(productProtocol)) {
                        protocol = productProtocol;
                    } else {
                        // 未知协议类型，默认使用 MQTT1.0
                        protocol = "MQTT1.0";
                    }
                }
            }
            log.info("设备 {} 使用 {} 协议格式下发命令", request.getDeviceCode(), protocol);
            
            // 生成命令唯一ID
            String commandId = "CMD" + System.currentTimeMillis();
            
            // 批量下发命令到MQTT（不等待设备响应，根据产品配置选择协议格式）
            for (CommandItem cmd : request.getCommands()) {
                mqttPublisher.sendCommand(request.getDeviceCode(), cmd.getAddr(), cmd.getAddrv(), protocol);
            }
            
            // 记录命令下发日志
            try {
                log.info("========== 开始记录命令下发日志 ==========");
                // 获取第一个命令的名称
                CommandItem firstCmd = request.getCommands().get(0);
                String commandAddr = firstCmd.getAddr();
                log.info("命令地址: {}", commandAddr);
                
                // 根据命令地址和命令值查询命令名称
                String commandValue = firstCmd.getAddrv();
                LambdaQueryWrapper<Command> commandQuery = new LambdaQueryWrapper<>();
                commandQuery.eq(Command::getProductId, device.getProductId())
                          .eq(Command::getAddr, commandAddr)
                          .eq(Command::getCommandValue, commandValue);
                Command command = commandMapper.selectOne(commandQuery);
                String commandName = command != null ? command.getCommandName() : commandAddr;
                log.info("命令名称: {}", commandName);
                 
                com.iot.platform.entity.DeviceLog deviceLog = new com.iot.platform.entity.DeviceLog();
                deviceLog.setDeviceId(device.getId());
                deviceLog.setDeviceCode(request.getDeviceCode());
                deviceLog.setLogType("command");
                deviceLog.setLogDetail(commandName);
                deviceLog.setCreateTime(LocalDateTime.now());
                deviceLogService.save(deviceLog);
                log.info("========== 命令下发日志记录成功，ID: {} ==========", deviceLog.getId());
            } catch (Exception e) {
                log.error("记录设备日志失败", e);
                e.printStackTrace();
            }
            
            // 立即返回下发成功响应
            Map<String, Object> data = new HashMap<>();
            data.put("commandId", commandId);
            data.put("status", "已下发");
            data.put("deviceCode", request.getDeviceCode());
            data.put("sendTime", LocalDateTime.now().format(FORMATTER));
            data.put("message", "命令已通过MQTT下发到设备，设备收到后会自行执行");
            
            log.info("========== 命令下发成功 - CommandId: {}, 设备: {} ==========", commandId, request.getDeviceCode());
            
            return Result.success(data, "success");
            
        } catch (Exception e) {
            log.error("========== 命令下发失败 - 设备: {} ==========", request.getDeviceCode(), e);
            e.printStackTrace();
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