package com.iot.platform.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.mqtt.MqttPublisher;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 继电器状态定时拉取
 * 设备不主动上报，每 3 分钟向 RELAY 协议在线设备发送 getDevStatus
 */
@Slf4j
@Component
public class RelayStatusPoller {

    @Resource
    private DeviceService deviceService;

    @Resource
    private ProductService productService;

    @Resource
    private MqttPublisher mqttPublisher;

    /**
     * 每 3 分钟拉取一次 RELAY 设备状态
     */
    @Scheduled(fixedRate = 180000)
    public void pollRelayStatus() {
        try {
            log.info("========== RelayStatusPoller 开始执行 ==========");
            
            // 获取 protocol=RELAY 的产品 ID 列表
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.eq(Product::getProtocol, "RELAY");
            List<Product> relayProducts = productService.list(productQuery);
            if (relayProducts.isEmpty()) {
                log.info("RelayStatusPoller: 未找到 RELAY 协议产品，跳过本次轮询");
                return;
            }
            List<Long> productIds = relayProducts.stream().map(Product::getId).collect(Collectors.toList());
            log.info("RelayStatusPoller: 找到 {} 个 RELAY 产品，产品ID: {}", relayProducts.size(), productIds);

            // 获取在线的 RELAY 设备
            LambdaQueryWrapper<Device> deviceQuery = new LambdaQueryWrapper<>();
            deviceQuery.in(Device::getProductId, productIds)
                    .eq(Device::getStatus, 1);
            List<Device> relayDevices = deviceService.list(deviceQuery);

            if (relayDevices.isEmpty()) {
                log.info("RelayStatusPoller: 未找到在线的 RELAY 设备，跳过本次轮询");
                return;
            }

            log.info("RelayStatusPoller: 开始拉取 {} 台在线 RELAY 设备状态", relayDevices.size());
            int successCount = 0;
            for (Device device : relayDevices) {
                try {
                    log.info("RelayStatusPoller: 向设备 {} ({}) 发送 getDevStatus", device.getDeviceCode(), device.getDeviceName());
                    mqttPublisher.publishGetDevStatus(device.getDeviceCode());
                    successCount++;
                } catch (Exception e) {
                    log.warn("RelayStatusPoller: 发送 getDevStatus 失败 - 设备: {}", device.getDeviceCode(), e);
                }
            }
            log.info("RelayStatusPoller: 完成，成功发送 {} 个 getDevStatus 命令", successCount);
            log.info("========== RelayStatusPoller 执行完成 ==========");
        } catch (Exception e) {
            log.error("RelayStatusPoller 执行失败", e);
        }
    }
}
