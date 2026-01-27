package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.dto.energy.*;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.entity.Product;
import com.iot.platform.mapper.DeviceDataMapper;
import com.iot.platform.mapper.DeviceMapper;
import com.iot.platform.mapper.ProductMapper;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 能源统计服务
 */
@Slf4j
@Service
public class EnergyStatisticsService {
    
    @Resource
    private DeviceDataMapper deviceDataMapper;
    
    @Resource
    private DeviceMapper deviceMapper;
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private ProductMapper productMapper;
    
    // 产品型号常量（不依赖产品名称）
    private static final String PRODUCT_MODEL_ELECTRIC = "ELEC";  // 单相电表产品型号
    private static final String PRODUCT_MODEL_ELECTRIC_3PHASE = "ELEC_3PHASE";  // 三相电表产品型号
    private static final String PRODUCT_MODEL_WATER = "WATER";    // 水表产品型号
    private static final String PRODUCT_MODEL_GAS = "GAS";        // 气表产品型号
    
    // 物模型属性标识符
    private static final String ATTR_ELECTRIC = "total_energy";  // 电表表底示数属性
    private static final String ATTR_WATER = "total_flow";       // 水表表底示数属性
    private static final String ATTR_GAS = "total_gas";          // 气表表底示数属性
    
    /**
     * 获取能源类型对应的产品型号（基于产品型号，不依赖产品名称）
     */
    private String getProductModelByEnergyType(String energyType) {
        if ("electric".equals(energyType)) {
            return PRODUCT_MODEL_ELECTRIC;
        } else if ("water".equals(energyType)) {
            return PRODUCT_MODEL_WATER;
        } else if ("gas".equals(energyType)) {
            return PRODUCT_MODEL_GAS;
        }
        return null;
    }
    
    /**
     * 获取能源类型对应的表底示数属性标识符
     */
    private String getEnergyAttr(String energyType) {
        if ("electric".equals(energyType)) {
            return ATTR_ELECTRIC;
        } else if ("water".equals(energyType)) {
            return ATTR_WATER;
        } else if ("gas".equals(energyType)) {
            return ATTR_GAS;
        }
        return null;
    }
    
    /**
     * 获取能源类型对应的单位
     */
    private String getEnergyUnit(String energyType) {
        if ("electric".equals(energyType)) {
            return "kWh";
        } else if ("water".equals(energyType) || "gas".equals(energyType)) {
            return "m³";
        }
        return "";
    }
    
    /**
     * 能耗统计
     */
    public EnergyStatisticsVO getEnergyStatistics(EnergyStatisticsRequest request, List<Long> allowedGroupIds) {
        EnergyStatisticsVO result = new EnergyStatisticsVO();
        
        // 获取能源类型对应的属性
        String energyAttr = getEnergyAttr(request.getEnergyType());
        if (energyAttr == null) {
            log.warn("不支持的能源类型: {}", request.getEnergyType());
            return result;
        }
        
        result.setUnit(getEnergyUnit(request.getEnergyType()));
        
        // 获取符合条件的设备列表（考虑数据权限和产品型号）
        List<Device> devices = getFilteredDevices(request.getDeviceId(), request.getGroupId(), allowedGroupIds, request.getEnergyType());
        if (devices.isEmpty()) {
            log.warn("没有符合条件的设备");
            return result;
        }
        
        List<Long> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        
        // 查询统计期间内的数据
        List<DeviceData> dataList = queryDeviceData(
            deviceIds, 
            energyAttr, 
            request.getStartTime(), 
            request.getEndTime()
        );
        
        if (dataList.isEmpty()) {
            log.warn("查询时间段内没有数据");
            return result;
        }
        
        // 按设备分组计算能耗
        Map<Long, List<DeviceData>> dataByDevice = dataList.stream()
            .collect(Collectors.groupingBy(DeviceData::getDeviceId));
        
        double totalConsumption = 0.0;
        List<EnergyStatisticsVO.TopDeviceVO> topDevices = new ArrayList<>();
        
        for (Map.Entry<Long, List<DeviceData>> entry : dataByDevice.entrySet()) {
            Long deviceId = entry.getKey();
            List<DeviceData> deviceDataList = entry.getValue();
            
            // 排序：按时间升序
            deviceDataList.sort(Comparator.comparing(DeviceData::getCtime));
            
            // 计算该设备的能耗：最后一条数据 - 第一条数据
            double deviceConsumption = calculateConsumption(deviceDataList);
            totalConsumption += deviceConsumption;
            
            Device device = devices.stream()
                .filter(d -> d.getId().equals(deviceId))
                .findFirst()
                .orElse(null);
            
            if (device != null) {
                EnergyStatisticsVO.TopDeviceVO topDevice = new EnergyStatisticsVO.TopDeviceVO();
                topDevice.setDeviceId(device.getId());
                topDevice.setDeviceName(device.getDeviceName());
                topDevice.setDeviceCode(device.getDeviceCode());
                topDevice.setTotalConsumption(deviceConsumption);
                topDevice.setUnit(result.getUnit());
                topDevice.setRecordCount(deviceDataList.size());
                topDevices.add(topDevice);
            }
        }
        
        // 按能耗降序排序，取Top10
        topDevices.sort((a, b) -> Double.compare(b.getTotalConsumption(), a.getTotalConsumption()));
        if (topDevices.size() > 10) {
            topDevices = topDevices.subList(0, 10);
        }
        
        // 计算平均能耗（按天数）
        long days = java.time.temporal.ChronoUnit.DAYS.between(request.getStartTime(), request.getEndTime()) + 1;
        double avgConsumption = days > 0 ? totalConsumption / days : 0.0;
        
        result.setTotalConsumption(totalConsumption);
        result.setAvgConsumption(avgConsumption);
        result.setTop10Devices(topDevices);
        
        // TODO: 同比、环比计算（需要查询历史同期数据）
        result.setYoy(0.0);
        result.setMom(0.0);
        
        return result;
    }
    
    /**
     * 能耗趋势
     */
    public List<EnergyTrendVO> getEnergyTrend(EnergyTrendRequest request, List<Long> allowedGroupIds) {
        List<EnergyTrendVO> result = new ArrayList<>();
        
        // 获取能源类型对应的属性
        String energyAttr = getEnergyAttr(request.getEnergyType());
        if (energyAttr == null) {
            log.warn("不支持的能源类型: {}", request.getEnergyType());
            return result;
        }
        
        String unit = getEnergyUnit(request.getEnergyType());
        String dateFormat = request.getDateFormat() != null ? request.getDateFormat() : "day";
        
        // 验证设备权限
        if (request.getDeviceId() != null) {
            Device device = deviceMapper.selectById(request.getDeviceId());
            if (device == null) {
                log.warn("设备不存在: {}", request.getDeviceId());
                return result;
            }
            
            // 检查数据权限
            if (allowedGroupIds != null && !allowedGroupIds.contains(device.getGroupId())) {
                log.warn("无权限查看设备: {}", request.getDeviceId());
                return result;
            }
        }
        
        // 查询数据（考虑产品型号）
        List<Long> deviceIds = request.getDeviceId() != null ? 
            Collections.singletonList(request.getDeviceId()) : 
            getFilteredDevices(null, null, allowedGroupIds, request.getEnergyType()).stream()
                .map(Device::getId)
                .collect(Collectors.toList());
        
        List<DeviceData> dataList = queryDeviceData(deviceIds, energyAttr, request.getStartTime(), request.getEndTime());
        
        // 按时间分组
        Map<String, List<DeviceData>> dataByTime = new HashMap<>();
        DateTimeFormatter formatter = getDateTimeFormatter(dateFormat);
        
        for (DeviceData data : dataList) {
            String timeLabel = data.getCtime().format(formatter);
            dataByTime.computeIfAbsent(timeLabel, k -> new ArrayList<>()).add(data);
        }
        
        // 计算每个时间段的能耗
        for (Map.Entry<String, List<DeviceData>> entry : dataByTime.entrySet()) {
            String timeLabel = entry.getKey();
            List<DeviceData> timeDataList = entry.getValue();
            
            // 按设备分组
            Map<Long, List<DeviceData>> byDevice = timeDataList.stream()
                .collect(Collectors.groupingBy(DeviceData::getDeviceId));
            
            double totalConsumption = 0.0;
            for (List<DeviceData> deviceDataList : byDevice.values()) {
                deviceDataList.sort(Comparator.comparing(DeviceData::getCtime));
                totalConsumption += calculateConsumption(deviceDataList);
            }
            
            EnergyTrendVO vo = new EnergyTrendVO();
            vo.setTimeLabel(timeLabel);
            vo.setConsumption(totalConsumption);
            vo.setUnit(unit);
            result.add(vo);
        }
        
        // 按时间标签排序
        result.sort(Comparator.comparing(EnergyTrendVO::getTimeLabel));
        
        return result;
    }
    
    /**
     * 能耗报表（优化版：使用数据库聚合查询，避免加载大量数据到内存）
     */
    public EnergyReportVO getEnergyReport(EnergyReportRequest request, List<Long> allowedGroupIds) {
        EnergyReportVO result = new EnergyReportVO();
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());
        result.setRecords(new ArrayList<>());
        
        // 获取能源类型对应的属性
        String energyAttr = getEnergyAttr(request.getEnergyType());
        if (energyAttr == null) {
            log.warn("不支持的能源类型: {}", request.getEnergyType());
            result.setTotal(0);
            return result;
        }
        
        String unit = getEnergyUnit(request.getEnergyType());
        String reportType = request.getReportType() != null ? request.getReportType() : "daily";
        
        // 获取符合条件的设备列表（考虑产品型号）
        List<Device> devices = getFilteredDevices(request.getDeviceId(), request.getGroupId(), allowedGroupIds, request.getEnergyType());
        if (devices.isEmpty()) {
            result.setTotal(0);
            return result;
        }
        
        List<Long> deviceIds = devices.stream().map(Device::getId).collect(Collectors.toList());
        
        // 获取日期格式化格式（用于SQL查询）
        String dateFormat = getReportDateFormat(reportType);
        
        // 使用数据库聚合查询，直接获取分组统计结果（避免加载大量数据到内存）
        List<EnergyDailyStatDTO> statList = deviceDataMapper.selectEnergyDailyStats(
                deviceIds, 
                energyAttr, 
                request.getStartTime(), 
                request.getEndTime(),
                dateFormat
        );
        
        // 构建设备ID到设备信息的映射（用于快速查找）
        Map<Long, Device> deviceMap = devices.stream()
                .collect(Collectors.toMap(Device::getId, d -> d));
        
        // 转换为报表记录
        List<EnergyReportVO.ReportRecordVO> records = new ArrayList<>();
        for (EnergyDailyStatDTO stat : statList) {
            Device device = deviceMap.get(stat.getDeviceId());
            if (device == null) {
                continue;
            }
            
            // 处理能耗值（如果为负数或null，返回0）
            BigDecimal consumption = stat.getConsumption();
            if (consumption == null || consumption.compareTo(BigDecimal.ZERO) < 0) {
                if (consumption != null && consumption.compareTo(BigDecimal.ZERO) < 0) {
                    log.warn("计算出负能耗值: {}, 设备ID: {}, 日期: {}", consumption, stat.getDeviceId(), stat.getStatDate());
                }
                consumption = BigDecimal.ZERO;
            }
            
            EnergyReportVO.ReportRecordVO record = new EnergyReportVO.ReportRecordVO();
            record.setDeviceId(device.getId());
            record.setDeviceName(device.getDeviceName());
            record.setDeviceCode(device.getDeviceCode());
            record.setDate(stat.getStatDate());
            record.setConsumption(consumption.doubleValue());
            record.setUnit(unit);
            records.add(record);
        }
        
        // 分页
        int total = records.size();
        int fromIndex = (request.getPage() - 1) * request.getPageSize();
        int toIndex = Math.min(fromIndex + request.getPageSize(), total);
        
        if (fromIndex < total) {
            result.setRecords(records.subList(fromIndex, toIndex));
        }
        
        result.setTotal(total);
        return result;
    }
    
    /**
     * 获取实时能耗（Dashboard用）
     */
    public RealtimeEnergyVO getRealtimeEnergy(RealtimeEnergyRequest request) {
        RealtimeEnergyVO result = new RealtimeEnergyVO();
        
        // 根据timeRange计算时间范围
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = null;
        
        if ("today".equals(request.getTimeRange())) {
            startTime = now.toLocalDate().atStartOfDay();
        } else if ("week".equals(request.getTimeRange())) {
            startTime = now.minusDays(7);
        } else if ("month".equals(request.getTimeRange())) {
            startTime = now.minusDays(30);
        } else {
            startTime = now.toLocalDate().atStartOfDay(); // 默认今天
        }
        
        // 查询电、水、气三种能源的实时数据
        RealtimeEnergyVO.EnergyData electricData = getRealtimeEnergyData("electric", startTime, now);
        RealtimeEnergyVO.EnergyData waterData = getRealtimeEnergyData("water", startTime, now);
        RealtimeEnergyVO.EnergyData gasData = getRealtimeEnergyData("gas", startTime, now);
        
        result.setElectric(electricData);
        result.setWater(waterData);
        result.setGas(gasData);
        
        return result;
    }
    
    /**
     * 获取单个能源类型的实时数据
     */
    private RealtimeEnergyVO.EnergyData getRealtimeEnergyData(String energyType, LocalDateTime startTime, LocalDateTime endTime) {
        RealtimeEnergyVO.EnergyData result = new RealtimeEnergyVO.EnergyData();
        result.setUnit(getEnergyUnit(energyType));
        result.setTopDevices(new ArrayList<>());
        
        String energyAttr = getEnergyAttr(energyType);
        if (energyAttr == null) {
            return result;
        }
        
        // 查询所有相关设备的数据
        List<DeviceData> dataList = queryDeviceData(null, energyAttr, startTime, endTime);
        
        // 按设备分组计算
        Map<Long, List<DeviceData>> byDevice = dataList.stream()
            .collect(Collectors.groupingBy(DeviceData::getDeviceId));
        
        double totalConsumption = 0.0;
        List<RealtimeEnergyVO.TopDeviceVO> topDevices = new ArrayList<>();
        
        for (Map.Entry<Long, List<DeviceData>> entry : byDevice.entrySet()) {
            List<DeviceData> deviceDataList = entry.getValue();
            deviceDataList.sort(Comparator.comparing(DeviceData::getCtime));
            
            double consumption = calculateConsumption(deviceDataList);
            totalConsumption += consumption;
            
            Device device = deviceMapper.selectById(entry.getKey());
            if (device != null) {
                RealtimeEnergyVO.TopDeviceVO topDevice = new RealtimeEnergyVO.TopDeviceVO();
                topDevice.setDeviceId(device.getId());
                topDevice.setDeviceName(device.getDeviceName());
                topDevice.setConsumption(consumption);
                topDevice.setUnit(result.getUnit());
                topDevices.add(topDevice);
            }
        }
        
        // 排序取Top10
        topDevices.sort((a, b) -> Double.compare(b.getConsumption(), a.getConsumption()));
        if (topDevices.size() > 10) {
            topDevices = topDevices.subList(0, 10);
        }
        
        result.setTotalConsumption(totalConsumption);
        result.setTopDevices(topDevices);
        
        return result;
    }
    
    /**
     * 获取过滤后的设备列表（考虑数据权限和产品型号）
     * 能源统计专用：根据能源类型过滤对应产品型号的设备
     */
    private List<Device> getFilteredDevices(Long deviceId, Long groupId, List<Long> allowedGroupIds, String energyType) {
        LambdaQueryWrapper<Device> query = new LambdaQueryWrapper<>();
        
        if (deviceId != null) {
            query.eq(Device::getId, deviceId);
        }
        
        if (groupId != null) {
            query.eq(Device::getGroupId, groupId);
        }
        
        // 数据权限过滤
        if (allowedGroupIds != null && !allowedGroupIds.isEmpty()) {
            query.in(Device::getGroupId, allowedGroupIds);
        }
        
        // 根据能源类型过滤产品型号（基于产品型号，不依赖产品名称）
        if (energyType != null) {
            // 电表类型支持单相和三相两种产品型号
            if ("electric".equals(energyType)) {
                // 查询单相电表和三相电表
                LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
                productQuery.in(Product::getProductModel, Arrays.asList(PRODUCT_MODEL_ELECTRIC, PRODUCT_MODEL_ELECTRIC_3PHASE));
                List<Product> products = productMapper.selectList(productQuery);
                
                if (!products.isEmpty()) {
                    List<Long> productIds = products.stream()
                        .map(Product::getId)
                        .collect(Collectors.toList());
                    query.in(Device::getProductId, productIds);
                } else {
                    // 如果没有找到对应产品型号，返回空列表
                    return new ArrayList<>();
                }
            } else {
                // 水表、气表等其他类型，使用原有逻辑
                String productModel = getProductModelByEnergyType(energyType);
                if (productModel != null) {
                    // 先查询产品ID列表
                    LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
                    productQuery.eq(Product::getProductModel, productModel);
                    List<Product> products = productMapper.selectList(productQuery);
                    
                    if (!products.isEmpty()) {
                        List<Long> productIds = products.stream()
                            .map(Product::getId)
                            .collect(Collectors.toList());
                        query.in(Device::getProductId, productIds);
                    } else {
                        // 如果没有找到对应产品型号，返回空列表
                        return new ArrayList<>();
                    }
                }
            }
        }
        
        return deviceMapper.selectList(query);
    }
    
    /**
     * 获取过滤后的设备列表（考虑数据权限，不按能源类型过滤）
     * 用于非能源统计场景
     */
    private List<Device> getFilteredDevices(Long deviceId, Long groupId, List<Long> allowedGroupIds) {
        return getFilteredDevices(deviceId, groupId, allowedGroupIds, null);
    }
    
    /**
     * 查询设备数据
     */
    private List<DeviceData> queryDeviceData(List<Long> deviceIds, String addr, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
        
        if (deviceIds != null && !deviceIds.isEmpty()) {
            query.in(DeviceData::getDeviceId, deviceIds);
        }
        
        query.eq(DeviceData::getAddr, addr)
             .ge(startTime != null, DeviceData::getCtime, startTime)
             .le(endTime != null, DeviceData::getCtime, endTime)
             .orderByAsc(DeviceData::getCtime);
        
        return deviceDataMapper.selectList(query);
    }
    
    /**
     * 计算能耗（周期能耗 = 最后一条数据 - 第一条数据）
     */
    private double calculateConsumption(List<DeviceData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0.0;
        }
        
        if (dataList.size() == 1) {
            // 只有一条数据，无法计算差值，返回0
            return 0.0;
        }
        
        // 排序确保时间顺序
        dataList.sort(Comparator.comparing(DeviceData::getCtime));
        
        try {
            double firstValue = Double.parseDouble(dataList.get(0).getAddrv());
            double lastValue = Double.parseDouble(dataList.get(dataList.size() - 1).getAddrv());
            
            // 能耗 = 最后值 - 初始值
            double consumption = lastValue - firstValue;
            
            // 如果出现负值（可能是表重置），记录日志但不报错
            if (consumption < 0) {
                log.warn("计算出负能耗值: {}, 设备ID: {}", consumption, dataList.get(0).getDeviceId());
                return 0.0;
            }
            
            return consumption;
        } catch (NumberFormatException e) {
            log.error("能耗值解析失败: {}", dataList.get(0).getAddrv(), e);
            return 0.0;
        }
    }
    
    /**
     * 获取时间格式化器
     */
    private DateTimeFormatter getDateTimeFormatter(String format) {
        if ("hour".equals(format)) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        } else {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
    }
    
    /**
     * 获取报表时间格式化器（用于Java代码）
     */
    private DateTimeFormatter getReportFormatter(String reportType) {
        if ("monthly".equals(reportType)) {
            return DateTimeFormatter.ofPattern("yyyy-MM");
        } else if ("yearly".equals(reportType)) {
            return DateTimeFormatter.ofPattern("yyyy");
        } else {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }
    }
    
    /**
     * 获取报表日期格式化格式（用于MySQL DATE_FORMAT函数）
     */
    private String getReportDateFormat(String reportType) {
        if ("monthly".equals(reportType)) {
            return "%Y-%m";  // 2026-01
        } else if ("yearly".equals(reportType)) {
            return "%Y";     // 2026
        } else {
            return "%Y-%m-%d"; // 2026-01-01
        }
    }
}
