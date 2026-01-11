package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.DeviceLog;
import com.iot.platform.mapper.DeviceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备日志服务
 */
@Slf4j
@Service
public class DeviceLogService extends ServiceImpl<DeviceLogMapper, DeviceLog> {

    /**
     * 分页查询设备日志
     */
    public Page<DeviceLog> pageQuery(int page, int pageSize, String deviceCode,
                                     String logType,
                                     LocalDateTime startTime, LocalDateTime endTime,
                                     List<Long> allowedDeviceIds) {
        Page<DeviceLog> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<DeviceLog> query = new LambdaQueryWrapper<>();

        // 设备编码筛选
        if (deviceCode != null && !deviceCode.isEmpty()) {
            query.eq(DeviceLog::getDeviceCode, deviceCode);
        }

        // 日志类型筛选
        if (logType != null && !logType.isEmpty()) {
            query.eq(DeviceLog::getLogType, logType);
        }

        // 时间范围筛选
        if (startTime != null) {
            query.ge(DeviceLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            query.le(DeviceLog::getCreateTime, endTime);
        }

        // 数据权限过滤
        if (allowedDeviceIds != null && !allowedDeviceIds.isEmpty()) {
            query.in(DeviceLog::getDeviceId, allowedDeviceIds);
        }

        // 按时间倒序排列
        query.orderByDesc(DeviceLog::getCreateTime);

        return this.page(pageParam, query);
    }
}
