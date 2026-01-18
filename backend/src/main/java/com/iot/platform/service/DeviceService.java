package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.dto.AlarmConfigDTO;
import com.iot.platform.entity.Attribute;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.mapper.DeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 设备管理服务
 */
@Slf4j
@Service
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {
    
    @Resource
    private ProductService productService;
    
    @Resource
    private DeviceGroupService deviceGroupService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Resource
    private DeviceLogService deviceLogService;
    
    @Autowired
    @Lazy
    private com.iot.platform.service.AlarmLogService alarmLogService;
    
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
     * 获取指定分组及其所有下级分组ID列表
     */
    public List<Long> getAllSubGroupIds(Long groupId) {
        List<Long> result = new ArrayList<>();
        result.add(groupId); // 包含当前分组
        
        // 递归查找所有下级分组
        List<com.iot.platform.entity.DeviceGroup> allGroups = deviceGroupService.getTreeList();
        collectSubGroupIds(groupId, allGroups, result);
        
        return result;
    }
    
    /**
     * 递归收集下级分组ID
     */
    private void collectSubGroupIds(Long parentId, List<com.iot.platform.entity.DeviceGroup> allGroups, List<Long> result) {
        for (com.iot.platform.entity.DeviceGroup group : allGroups) {
            if (parentId.equals(group.getParentId())) {
                result.add(group.getId());
                // 递归查找下级
                collectSubGroupIds(group.getId(), allGroups, result);
            }
        }
    }
    
    /**
     * 分页查询设备列表
     */
    public IPage<Device> getDevicePage(int page, int pageSize, String keyword, 
                                       Long productId, Long groupId, Integer status, List<Long> allowedGroupIds) {
        Page<Device> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        
        // 数据权限过滤：限制分组范围
        if (allowedGroupIds != null && !allowedGroupIds.isEmpty()) {
            query.in(Device::getGroupId, allowedGroupIds);
        }
        
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
        
        // 排序：在线状态优先（在线在前），然后按创建时间降序
        query.orderByDesc(Device::getStatus);  // 在线状态（1=在线）在前
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
        
        // 获取分组信息
        com.iot.platform.entity.DeviceGroup group = null;
        if (device.getGroupId() != null) {
            group = deviceGroupService.getById(device.getGroupId());
        }
        
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
        result.put("groupName", group != null ? group.getGroupName() : ""); // 添加分组名称
        result.put("status", device.getStatus());
        result.put("lastOnlineTime", device.getLastOnlineTime());
        result.put("createTime", device.getCreateTime()); // 添加创建时间
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
        
        // 更新 Redis 缓存，固定3分钟超时
        String key = DEVICE_ONLINE_KEY + deviceCode;
        if (online) {
            stringRedisTemplate.opsForValue().set(key, "1", 180, TimeUnit.SECONDS);
            
            // 设备上线时，将未恢复的离线报警标记为已恢复
            // 这样设备再次离线时可以重新触发报警
            // 使用 @Lazy 注解避免循环依赖
            if (alarmLogService != null) {
                try {
                    alarmLogService.markOfflineAlarmsAsRecovered(device.getId());
                } catch (Exception e) {
                    log.debug("标记离线报警为已恢复失败 - 设备ID: {}", device.getId(), e);
                }
            }
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
            // 设备心跳，3分钟超时
            String key = DEVICE_ONLINE_KEY + deviceCode;
            stringRedisTemplate.opsForValue().set(key, "1", 180, TimeUnit.SECONDS);
            
            // 如果设备状态是离线，更新为在线
            if (device.getStatus() == 0) {
                updateOnlineStatus(deviceCode, true);
                
                // 记录设备上线日志
                recordDeviceLog(device.getId(), deviceCode, "online", "设备上线");
            }
        }
    }
    
    /**
     * 记录设备日志
     */
    private void recordDeviceLog(Long deviceId, String deviceCode, String logType, String logDetail) {
        try {
            com.iot.platform.entity.DeviceLog deviceLog = new com.iot.platform.entity.DeviceLog();
            deviceLog.setDeviceId(deviceId);
            deviceLog.setDeviceCode(deviceCode);
            deviceLog.setLogType(logType);
            deviceLog.setLogDetail(logDetail);
            deviceLog.setCreateTime(LocalDateTime.now());
            deviceLogService.save(deviceLog);
        } catch (Exception e) {
            log.error("记录设备日志失败 - 设备: {}", deviceCode, e);
        }
    }
    
    /**
     * 统计设备数据
     */
    public Map<String, Object> getDeviceStatistics(List<Long> allowedGroupIds) {
        Map<String, Object> stats = new HashMap<>();
        
        // 构建基础查询条件
        LambdaQueryWrapper<Device> baseQuery = new LambdaQueryWrapper<>();
        if (allowedGroupIds != null && !allowedGroupIds.isEmpty()) {
            baseQuery.in(Device::getGroupId, allowedGroupIds);
        }
        
        // 设备总数
        long total = this.count(baseQuery);
        
        // 在线设备数
        LambdaQueryWrapper<Device> onlineQuery = new LambdaQueryWrapper<>();
        if (allowedGroupIds != null && !allowedGroupIds.isEmpty()) {
            onlineQuery.in(Device::getGroupId, allowedGroupIds);
        }
        onlineQuery.eq(Device::getStatus, 1);
        long online = this.count(onlineQuery);
        
        // 离线设备数
        long offline = total - online;
        
        stats.put("totalDevices", total);
        stats.put("onlineDevices", online);
        stats.put("offlineDevices", offline);
        
        // 今日数据量（由 Controller 层补充）
        stats.put("todayDataCount", 0L);
        
        // 产品分布
        try {
            List<Map<String, Object>> productDistribution = new ArrayList<>();
            
            // 获取所有产品
            List<Product> allProducts = productService.list();
            
            // 统计每个产品的设备数
            List<Device> devices = this.list(baseQuery);
            Map<Long, Long> productCountMap = devices.stream()
                .collect(Collectors.groupingBy(Device::getProductId, Collectors.counting()));
            
            // 遍历所有产品，包括没有设备的产品
            for (Product product : allProducts) {
                Map<String, Object> item = new HashMap<>();
                item.put("productName", product.getProductName());
                item.put("count", productCountMap.getOrDefault(product.getId(), 0L));
                productDistribution.add(item);
            }
            
            stats.put("productDistribution", productDistribution);
            
            // 产品总数：统计产品表的总数，而不是设备使用的产品数
            long totalProductCount = productService.count();
            stats.put("productCount", totalProductCount);
        } catch (Exception e) {
            log.error("统计产品分布失败", e);
            stats.put("productDistribution", new ArrayList<>());
            stats.put("productCount", 0);
        }
        
        // 最近更新的设备（最夒5个）
        try {
            LambdaQueryWrapper<Device> recentQuery = new LambdaQueryWrapper<>();
            if (allowedGroupIds != null && !allowedGroupIds.isEmpty()) {
                recentQuery.in(Device::getGroupId, allowedGroupIds);
            }
            recentQuery.orderByDesc(Device::getLastOnlineTime)
                       .last("LIMIT 5");
            List<Device> recentDevices = this.list(recentQuery);
            
            List<Map<String, Object>> recentList = recentDevices.stream().map(device -> {
                Map<String, Object> item = new HashMap<>();
                item.put("deviceCode", device.getDeviceCode());
                item.put("deviceName", device.getDeviceName());
                item.put("lastOnlineTime", device.getLastOnlineTime());
                item.put("status", device.getStatus()); // 添加设备状态
                return item;
            }).collect(Collectors.toList());
            
            stats.put("recentDevices", recentList);
        } catch (Exception e) {
            log.error("统计最近更新设备失败", e);
            stats.put("recentDevices", new ArrayList<>());
        }
        
        return stats;
    }
    
    /**
     * 配置设备告警阈值（单个或批量）
     */
    @Transactional(rollbackFor = Exception.class)
    public int configureAlarm(List<Long> deviceIds, AlarmConfigDTO alarmConfig, Boolean enabled) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            throw new RuntimeException("设备ID列表不能为空");
        }
        
        try {
            // 将配置对象转为JSON字符串
            String configJson = objectMapper.writeValueAsString(alarmConfig);
            
            // 批量更新设备告警配置
            int count = 0;
            for (Long deviceId : deviceIds) {
                Device device = this.getById(deviceId);
                if (device != null) {
                    device.setAlarmConfig(configJson);
                    device.setAlarmEnabled(enabled);
                    device.setUpdateTime(LocalDateTime.now());
                    this.updateById(device);
                    count++;
                }
            }
            
            log.info("批量配置设备告警阈值成功: {} 台设备", count);
            return count;
        } catch (Exception e) {
            log.error("配置设备告警阈值失败", e);
            throw new RuntimeException("配置告警阈值失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备告警配置
     */
    public AlarmConfigDTO getAlarmConfig(Long deviceId) {
        Device device = this.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        if (device.getAlarmConfig() == null || device.getAlarmConfig().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(device.getAlarmConfig(), AlarmConfigDTO.class);
        } catch (Exception e) {
            log.error("解析设备告警配置失败", e);
            return null;
        }
    }
    
    /**
     * 切换设备告警启用状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleAlarmEnabled(Long deviceId, Boolean enabled) {
        Device device = this.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在: " + deviceId);
        }
        
        device.setAlarmEnabled(enabled);
        device.setUpdateTime(LocalDateTime.now());
        this.updateById(device);
        
        log.info("设备 {} 告警状态切换为: {}", device.getDeviceName(), enabled ? "启用" : "禁用");
    }
}
