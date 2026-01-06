package com.iot.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iot.platform.common.Result;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.Product;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.service.DeviceDataService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.ProductService;
import com.iot.platform.service.DeviceGroupService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 设备管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/devices")
@CrossOrigin
public class DeviceController {
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private DeviceDataService deviceDataService;
    
    @Resource
    private ProductService productService;
    
    @Resource
    private DeviceGroupService deviceGroupService;
    
    @Resource
    private com.iot.platform.service.UserService userService;
    
    @Resource
    private com.iot.platform.mapper.RoleMapper roleMapper;
    
    @Resource
    private com.iot.platform.mapper.UserMapper userMapper;
    
    /**
     * 获取当前用户信息
     */
    private com.iot.platform.entity.User getCurrentUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String actualToken = token.substring(7);
        Long userId = userService.validateToken(actualToken);
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }
    
    /**
     * 判断用户是否为超级管理员
     */
    private boolean isSuperAdmin(com.iot.platform.entity.User user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        com.iot.platform.entity.Role role = roleMapper.selectById(user.getRoleId());
        return role != null && "super_admin".equals(role.getRoleCode());
    }
    
    /**
     * 创建设备
     */
    @PostMapping
    public Result<Device> createDevice(@RequestBody Map<String, Object> params) {
        try {
            Device device = new Device();
            
            // 支持name和deviceName两种参数名
            String deviceName = params.get("name") != null ? 
                params.get("name").toString() : 
                (params.get("deviceName") != null ? params.get("deviceName").toString() : null);
            
            // 支持code和deviceCode两种参数名
            String deviceCode = params.get("code") != null ? 
                params.get("code").toString() : 
                (params.get("deviceCode") != null ? params.get("deviceCode").toString() : null);
            
            // 验证必填字段
            if (deviceName == null || deviceName.trim().isEmpty()) {
                return Result.error("设备名称不能为空");
            }
            if (deviceCode == null || deviceCode.trim().isEmpty()) {
                return Result.error("设备编码不能为空");
            }
            // 验证必填字段
            Object productIdObj = params.get("productId");
            if (productIdObj == null) {
                return Result.error("产品ID不能为空");
            }
            String productIdStr = productIdObj.toString();
            if (productIdStr == null || productIdStr.trim().isEmpty()) {
                return Result.error("产品ID不能为空");
            }
            
            Object groupIdObj = params.get("groupId");
            if (groupIdObj == null) {
                return Result.error("分组ID不能为空");
            }
            String groupIdStr = groupIdObj.toString();
            if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
                return Result.error("分组ID不能为空");
            }
            
            device.setDeviceName(deviceName);
            device.setDeviceCode(deviceCode);
            device.setProductId(Long.valueOf(productIdStr));
            device.setGroupId(Long.valueOf(groupIdStr));
            
            // 备注为可选字段
            if (params.get("remark") != null) {
                device.setLocation(params.get("remark").toString());
            }
            
            // 设置默认值
            device.setStatus(0); // 默认离线
            device.setOfflineTimeout(300); // 默认5分钟超时
            
            Device result = deviceService.createDevice(device);
            return Result.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建设备失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询设备列表
     */
    @PostMapping("/list")
    public Result<Map<String, Object>> getDeviceList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            // 获取当前用户（可选）
            com.iot.platform.entity.User currentUser = getCurrentUser(token);
            
            // 判断是否为超级管理员
            boolean isSuper = currentUser != null && isSuperAdmin(currentUser);
            
            // 如果params为null，初始化为空对象
            if (params == null) {
                params = new HashMap<>();
            }
            
            Integer page = 1;
            if (params.get("page") != null) {
                String pageStr = params.get("page").toString();
                if (pageStr != null && !pageStr.trim().isEmpty()) {
                    page = Integer.valueOf(pageStr);
                }
            }
            
            Integer pageSize = 20;
            if (params.get("pageSize") != null) {
                String pageSizeStr = params.get("pageSize").toString();
                if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                    pageSize = Integer.valueOf(pageSizeStr);
                }
            }
            
            String keyword = null;
            if (params.get("keyword") != null) {
                String keywordStr = params.get("keyword").toString();
                if (keywordStr != null && !keywordStr.trim().isEmpty()) {
                    keyword = keywordStr;
                }
            }
            
            Long productId = null;
            if (params.get("productId") != null) {
                String productIdStr = params.get("productId").toString();
                if (productIdStr != null && !productIdStr.trim().isEmpty()) {
                    productId = Long.valueOf(productIdStr);
                }
            }
            
            Long groupId = null;
            if (params.get("groupId") != null) {
                String groupIdStr = params.get("groupId").toString();
                if (groupIdStr != null && !groupIdStr.trim().isEmpty()) {
                    groupId = Long.valueOf(groupIdStr);
                }
            }
            
            Integer status = null;
            if (params.get("status") != null) {
                String statusStr = params.get("status").toString();
                if ("online".equals(statusStr)) {
                    status = 1;
                } else if ("offline".equals(statusStr)) {
                    status = 0;
                }
            }
            
            // 数据权限过滤：非超管只能查看本分组及下级分组的设备
            List<Long> allowedGroupIds = null;
            if (!isSuper && currentUser != null) {
                Long userGroupId = currentUser.getGroupId() != null ? currentUser.getGroupId() : 0L;
                // 获取当前用户分组及所有下级分组ID
                allowedGroupIds = deviceService.getAllSubGroupIds(userGroupId);
                log.info("用户 {} 允许查看的分组ID: {}", currentUser.getUsername(), allowedGroupIds);
            }
            
            IPage<Device> devicePage = deviceService.getDevicePage(
                page, pageSize, keyword, productId, groupId, status, allowedGroupIds
            );
            
            // 获取所有产品和分组信息，用于关联查询
            Map<Long, Product> productMap = new HashMap<>();
            Map<Long, DeviceGroup> groupMap = new HashMap<>();
            
            // 预先加载所有产品
            List<Product> allProducts = productService.list();
            for (Product product : allProducts) {
                productMap.put(product.getId(), product);
            }
            
            // 预先加载所有分组
            List<DeviceGroup> allGroups = deviceGroupService.getTreeList();
            for (DeviceGroup group : allGroups) {
                groupMap.put(group.getId(), group);
            }
            
            // 构建分组路径的方法
            java.util.function.Function<Long, String> buildGroupPath = groupId1 -> {
                if (groupId1 == null) return "";
                List<String> pathList = new ArrayList<>();
                Long currentId = groupId1;
                while (currentId != null && currentId > 0) {
                    DeviceGroup group = groupMap.get(currentId);
                    if (group == null) break;
                    pathList.add(0, group.getGroupName());
                    currentId = group.getParentId();
                }
                return String.join("/", pathList);
            };
            
            // 丰富设备列表数据，添加产品名称、分组名称和分组路径
            List<Map<String, Object>> enrichedList = devicePage.getRecords().stream()
                .map(device -> {
                    Map<String, Object> deviceMap = new java.util.LinkedHashMap<>();
                    deviceMap.put("id", device.getId());
                    deviceMap.put("deviceName", device.getDeviceName());
                    deviceMap.put("deviceCode", device.getDeviceCode());
                    deviceMap.put("productId", device.getProductId());
                    
                    // 添加产品名称
                    Product product = productMap.get(device.getProductId());
                    deviceMap.put("productName", product != null ? product.getProductName() : "");
                    deviceMap.put("productModel", product != null ? product.getProductModel() : "");
                    
                    deviceMap.put("groupId", device.getGroupId());
                    
                    // 添加分组名称
                    DeviceGroup group = groupMap.get(device.getGroupId());
                    deviceMap.put("groupName", group != null ? group.getGroupName() : "");
                    
                    // 添加分组路径
                    deviceMap.put("groupPath", buildGroupPath.apply(device.getGroupId()));
                    
                    deviceMap.put("status", device.getStatus());
                    deviceMap.put("lastOnlineTime", device.getLastOnlineTime());
                    deviceMap.put("createTime", device.getCreateTime());
                    deviceMap.put("updateTime", device.getUpdateTime());
                    
                    return deviceMap;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", devicePage.getTotal());
            result.put("page", devicePage.getCurrent());
            result.put("pageSize", devicePage.getSize());
            result.put("totalPages", devicePage.getPages());
            result.put("list", enrichedList);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询设备列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取设备详情
     */
    @PostMapping("/detail")
    public Result<Map<String, Object>> getDeviceDetail(@RequestBody Map<String, String> params) {
        try {
            String deviceCode = params.get("deviceCode");
            if (deviceCode == null || deviceCode.trim().isEmpty()) {
                return Result.error("设备编码不能为空");
            }
            
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            Map<String, Object> result = deviceService.getDeviceDetail(device.getId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取设备详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新设备
     */
    @PostMapping("/update")
    public Result<Device> updateDevice(@RequestBody Device device) {
        try {
            if (device.getId() == null) {
                return Result.error("设备ID不能为空");
            }
            Device result = deviceService.updateDevice(device);
            return Result.success(result, "更新成功");
        } catch (Exception e) {
            log.error("更新设备失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除设备
     */
    @PostMapping("/delete")
    public Result<Void> deleteDevice(@RequestBody Map<String, String> params) {
        try {
            String deviceCode = params.get("deviceCode");
            if (deviceCode == null || deviceCode.trim().isEmpty()) {
                return Result.error("设备编码不能为空");
            }
            
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            deviceService.deleteDevice(device.getId());
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除设备失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据设备编码获取设备
     */
    @GetMapping("/{deviceCode}")
    public Result<Device> getDevice(@PathVariable String deviceCode) {
        try {
            Device device = deviceService.getByDeviceCode(deviceCode);
            return Result.success(device);
        } catch (Exception e) {
            log.error("获取设备详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取设备最新数据
     */
    @PostMapping("/latest-data")
    public Result<Map<String, Object>> getLatestData(@RequestBody Map<String, String> params) {
        try {
            String deviceCode = params.get("deviceCode");
            if (deviceCode == null || deviceCode.trim().isEmpty()) {
                return Result.error("设备编码不能为空");
            }
            
            Device device = deviceService.getByDeviceCode(deviceCode);
            if (device == null) {
                return Result.error("设备不存在");
            }
            
            // 获取最新数据
            List<DeviceData> latestDataList = deviceDataService.getLatestData(deviceCode, 10); // 获取最近10条数据以覆盖所有属性
            
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", device.getId());
            result.put("deviceCode", device.getDeviceCode());
            
            if (!latestDataList.isEmpty()) {
                // 构造data对象
                Map<String, String> dataMap = new HashMap<>();
                
                // 按时间排序，最新的数据优先，填充到dataMap中
                latestDataList.sort((a, b) -> b.getCtime().compareTo(a.getCtime()));
                for (DeviceData dataItem : latestDataList) {
                    dataMap.put(dataItem.getAddr(), dataItem.getAddrv());
                }
                
                result.put("data", dataMap);
                result.put("reportTime", latestDataList.get(0).getCtime());
            } else {
                result.put("data", new HashMap<>());
                result.put("reportTime", null);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取设备最新数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新设备在线状态
     */
    @PostMapping("/{deviceCode}/status")
    public Result<Void> updateStatus(@PathVariable String deviceCode, 
                                     @RequestParam Boolean online) {
        try {
            deviceService.updateOnlineStatus(deviceCode, online);
            return Result.success();
        } catch (Exception e) {
            log.error("更新设备状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取设备统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户（可选）
            com.iot.platform.entity.User currentUser = getCurrentUser(token);
            
            // 判断是否为超级管理员
            boolean isSuper = currentUser != null && isSuperAdmin(currentUser);
            
            // 数据权限过滤：非超管只能查看本分组及下级分组的设备
            List<Long> allowedGroupIds = null;
            if (!isSuper && currentUser != null) {
                Long userGroupId = currentUser.getGroupId() != null ? currentUser.getGroupId() : 0L;
                allowedGroupIds = deviceService.getAllSubGroupIds(userGroupId);
            }
            
            Map<String, Object> statistics = deviceService.getDeviceStatistics(allowedGroupIds);
            
            // 补充今日数据量统计
            try {
                java.time.LocalDateTime todayStart = java.time.LocalDateTime.now()
                    .withHour(0).withMinute(0).withSecond(0);
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.iot.platform.entity.DeviceData> query = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                query.ge(com.iot.platform.entity.DeviceData::getReceiveTime, todayStart);
                long todayDataCount = deviceDataService.count(query);
                statistics.put("todayDataCount", todayDataCount);
            } catch (Exception e) {
                log.error("统计今日数据量失败", e);
                statistics.put("todayDataCount", 0L);
            }
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 设备查询请求参数
     */
    @Data
    public static class DeviceQueryRequest {
        private Integer page;
        private Integer pageSize;
        private String keyword;        // 搜索关键词（设备名称或编码）
        private Long productId;         // 产品ID筛选
        private Long groupId;           // 分组ID筛选
        private String status;          // 状态筛选（online/offline）
    }
}