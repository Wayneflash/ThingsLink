package com.iot.platform.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.dto.DeviceReportDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceData;
import com.iot.platform.mapper.DeviceDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 设备数据服务
 */
@Slf4j
@Service
public class DeviceDataService extends ServiceImpl<DeviceDataMapper, DeviceData> {
    
    @Resource
    private DeviceService deviceService;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String DEVICE_LATEST_DATA_KEY = "device:latest:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
    /**
     * 处理设备上报数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleDeviceReport(DeviceReportDTO reportDTO) {
        log.info("收到设备上报数据: {}", JSON.toJSONString(reportDTO));
        
        String deviceCode = reportDTO.getDid();
        
        // 检查设备是否存在
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            log.warn("设备不存在，忽略数据: {}", deviceCode);
            return;
        }
        
        // 刷新设备心跳
        deviceService.refreshHeartbeat(deviceCode);
        
        // 批量保存数据
        List<DeviceData> dataList = new ArrayList<>();
        for (DeviceReportDTO.ReportContent content : reportDTO.getContent()) {
            DeviceData data = new DeviceData();
            data.setDeviceId(device.getId());
            data.setDeviceCode(deviceCode);
            data.setAddr(content.getAddr());
            data.setAddrv(content.getAddrv());
            
            // 解析采集时间
            try {
                LocalDateTime ctime = LocalDateTime.parse(content.getCtime(), FORMATTER);
                data.setCtime(ctime);
            } catch (Exception e) {
                log.error("时间格式解析失败: {}", content.getCtime(), e);
                data.setCtime(LocalDateTime.now());
            }
            
            data.setReceiveTime(LocalDateTime.now());
            dataList.add(data);
            
            // 缓存最新数据到 Redis
            String key = DEVICE_LATEST_DATA_KEY + deviceCode + ":" + content.getAddr();
            stringRedisTemplate.opsForValue().set(key, content.getAddrv(), 1, TimeUnit.HOURS);
        }
        
        // 批量插入数据库
        this.saveBatch(dataList);
        log.info("设备 {} 数据保存成功，共 {} 条", deviceCode, dataList.size());
    }
    
    /**
     * 查询设备最新数据
     */
    public List<DeviceData> getLatestData(String deviceCode, int limit) {
        LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
        query.eq(DeviceData::getDeviceCode, deviceCode)
             .orderByDesc(DeviceData::getCtime)
             .last("LIMIT " + limit);
        return this.list(query);
    }
    
    /**
     * 查询设备历史数据
     */
    public List<DeviceData> getHistoryData(String deviceCode, String addr, 
                                           LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<DeviceData> query = new LambdaQueryWrapper<>();
        query.eq(DeviceData::getDeviceCode, deviceCode)
             .eq(addr != null, DeviceData::getAddr, addr)
             .ge(startTime != null, DeviceData::getCtime, startTime)
             .le(endTime != null, DeviceData::getCtime, endTime)
             .orderByDesc(DeviceData::getCtime);
        return this.list(query);
    }
}
