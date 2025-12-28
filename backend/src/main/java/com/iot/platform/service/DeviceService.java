package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.Attribute;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设备管理服务
 */
@Slf4j
@Service
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {
    
    @Resource
    private ProductService productService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DEVICE_ONLINE_KEY = "device:online:";
    
    /**
     * 创建设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Device createDevice(Device device) {
        // 检查产品是否存在
        Product product = productService.getById(device.getProductId());
        if (product == null) {
            throw new RuntimeException("产品不存在: " + device.getProductId());
        }
        
        // 检查设备编码是否已存在
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        query.eq(Device::getDeviceCode, device.getDeviceCode());
        Device existDevice = this.getOne(query);
        if (existDevice != null) {
            throw new RuntimeException("设备编码已存在: " + device.getDeviceCode());
        }
        
        // 设置默认值
        if (device.getStatus() == null) {
            device.setStatus(0); // 默认离线
        }
        if (device.getOfflineTimeout() == null) {
            device.setOfflineTimeout(300); // 默认5分钟超时
        }
        
        // 保存设备
        this.save(device);
        log.info("创建设备成功: {} ({})", device.getDeviceName(), device.getDeviceCode());
        return device;
    }
    
    /**
     * 分页查询设备列表
     */
    public IPage<Device> getDevicePage(int page, int pageSize, String keyword, 
                                       Long productId, Long groupId, Integer status) {
        Page<Device> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(wrapper -> wrapper
                .like(Device::getDeviceName, keyword)
                .or()
                .like(Device::getDeviceCode, keyword)
            );
        }
        
        // 产品筛选
        if (productId != null) {
            query.eq(Device::getProductId, productId);
        }
        
        // 分组筛选
        if (groupId != null) {
            query.eq(Device::getGroupId, groupId);
        }
        
        // 状态筛选
        if (status != null) {
            query.eq(Device::getStatus, status);
        }
        
        // 按创建时间降序
        query.orderByDesc(Device::getCreateTime);
        
        return this.page(pageParam, query);
    }
    
    /**
     * 获取设备详情（包含产品属性和最新数据）
     */
    public Map<String, Object> getDeviceDetail(Long deviceId) {
        // 获取设备信息
        Device device = this.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        // 获取产品信息
        Product product = productService.getById(device.getProductId());
        
        // 获取产品属性定义
        List<Attribute> productAttrs = productService.getProductAttributes(device.getProductId());
        
        // 从 Redis 获取最新数据
        String redisKey = "device:latest:" + device.getDeviceCode();
        Map<Object, Object> latestDataMap = redisTemplate.opsForHash().entries(redisKey);
        
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", device.getId());
        result.put("deviceName", device.getDeviceName());
        result.put("deviceCode", device.getDeviceCode());
        result.put("productId", device.getProductId());
        result.put("productName", product != null ? product.getProductName() : "");
        result.put("groupId", device.getGroupId());
        result.put("status", device.getStatus());
        result.put("lastOnlineTime", device.getLastOnlineTime());
        result.put("productAttrs", productAttrs);
        result.put("latestData", latestDataMap);
        
        return result;
    }
    
    /**
     * 更新设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Device updateDevice(Device device) {
        Device existDevice = this.getById(device.getId());
        if (existDevice == null) {
            throw new RuntimeException("设备不存在: " + device.getId());
        }
        
        device.setUpdateTime(LocalDateTime.now());
        this.updateById(device);
        log.info("更新设备成功: {}", device.getDeviceName());
        return device;
    }
    
    /**
     * 删除设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long deviceId) {
        Device device = this.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        this.removeById(deviceId);
        
        // 清除 Redis 缓存
        String redisKey = "device:latest:" + device.getDeviceCode();
        redisTemplate.delete(redisKey);
        
        log.info("删除设备成功: {}", device.getDeviceName());
    }
    
    /**
     * 根据设备编码获取设备
     */
    public Device getByDeviceCode(String deviceCode) {
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        query.eq(Device::getDeviceCode, deviceCode);
        return this.getOne(query);
    }
    
    /**
     * 更新设备在线状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOnlineStatus(String deviceCode, boolean online) {
        Device device = getByDeviceCode(deviceCode);
        if (device == null) {
            log.warn("设备不存在: {}", deviceCode);
            return;
        }
        
        device.setStatus(online ? 1 : 0);
        device.setLastOnlineTime(LocalDateTime.now());
        this.updateById(device);
        
        // 更新 Redis 缓存
        String key = DEVICE_ONLINE_KEY + deviceCode;
        if (online) {
            stringRedisTemplate.opsForValue().set(key, "1", 
                device.getOfflineTimeout(), TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.delete(key);
        }
        
        log.info("设备 {} 状态更新: {}", deviceCode, online ? "在线" : "离线");
    }
    
    /**
     * 刷新设备心跳
     */
    public void refreshHeartbeat(String deviceCode) {
        Device device = getByDeviceCode(deviceCode);
        if (device != null) {
            String key = DEVICE_ONLINE_KEY + deviceCode;
            stringRedisTemplate.opsForValue().set(key, "1", 
                device.getOfflineTimeout(), TimeUnit.SECONDS);
            
            // 如果设备状态是离线，更新为在线
            if (device.getStatus() == 0) {
                updateOnlineStatus(deviceCode, true);
            }
        }
    }
    
    /**
     * 统计设备数量
     */
    public Map<String, Long> getDeviceStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        // 设备总数
        long total = this.count();
        
        // 在线设备数
        LambdaQueryWrapper<Device> onlineQuery = new LambdaQueryWrapper<>();
        onlineQuery.eq(Device::getStatus, 1);
        long online = this.count(onlineQuery);
        
        // 离线设备数
        long offline = total - online;
        
        stats.put("total", total);
        stats.put("online", online);
        stats.put("offline", offline);
        
        return stats;
    }
}
