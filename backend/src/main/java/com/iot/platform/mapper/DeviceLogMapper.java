package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.DeviceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备日志Mapper
 */
@Mapper
public interface DeviceLogMapper extends BaseMapper<DeviceLog> {
}
