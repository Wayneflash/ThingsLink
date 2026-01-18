package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可视化公共接口 Controller（供外部智能体使用）
 * 
 * @author IoT Platform
 * @since 2026-01-17
 */
@Slf4j
@RestController
@RequestMapping("/visualization")
@CrossOrigin(origins = "*")
public class VisualizationPublicController {
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private ProductService productService;
    
    @Resource
    private com.iot.platform.service.DeviceDataService deviceDataService;
    
    /**
     * 获取设备数据（免token，通过deviceCode）- 供外部智能体生成的HTML调用
     * ⚠️ 注意：此接口不需要token，但存在安全风险，建议仅在内网或受信任环境使用
     */
    @GetMapping("/data/public/{deviceCode}")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS}, 
                 allowedHeaders = "*", maxAge = 3600)
    public Result<Map<String, Object>> getDeviceDataPublic(@PathVariable String deviceCode) {
        try {
            // 验证设备是否存在
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 获取产品属性（包含单位信息）
            List<com.iot.platform.entity.Attribute> attributes = productService.getProductAttributes(device.getProductId());
            Map<String, String> unitMap = new HashMap<>();
            Map<String, String> attrNameMap = new HashMap<>();
            for (com.iot.platform.entity.Attribute attr : attributes) {
                unitMap.put(attr.getAddr(), attr.getUnit() != null ? attr.getUnit() : "");
                attrNameMap.put(attr.getAddr(), attr.getAttrName());
            }
            
            // 获取设备最新数据
            List<DeviceData> dataList = deviceDataService.getLatestData(deviceCode, 10);
            
            // 转换数据格式
            Map<String, String> dataMap = new HashMap<>();
            // 按时间排序，最新的数据优先
            dataList.sort((a, b) -> b.getCtime().compareTo(a.getCtime()));
            for (DeviceData dataItem : dataList) {
                // 如果key已存在，不覆盖（保留最新的值）
                if (!dataMap.containsKey(dataItem.getAddr())) {
                    dataMap.put(dataItem.getAddr(), dataItem.getAddrv());
                }
            }
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", device.getId());
            result.put("deviceCode", device.getDeviceCode());
            result.put("data", dataMap);
            result.put("units", unitMap);  // 单位映射：{"tem": "°C", "hum": "%"}
            result.put("attrNames", attrNameMap);  // 属性名称映射：{"tem": "温度", "hum": "湿度"}
            result.put("reportTime", dataList.isEmpty() ? null : dataList.get(0).getCtime());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取设备数据失败: deviceCode={}", deviceCode, e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }
}
