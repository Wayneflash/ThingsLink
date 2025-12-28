package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.DeviceData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备数据 Mapper
 */
@Mapper
public interface DeviceDataMapper extends BaseMapper<DeviceData> {
}
