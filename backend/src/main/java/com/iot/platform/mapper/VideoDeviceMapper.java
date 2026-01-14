package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.VideoDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频设备Mapper接口
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Mapper
public interface VideoDeviceMapper extends BaseMapper<VideoDevice> {
    // MyBatis Plus 已提供基础CRUD方法
    // 如需自定义SQL，可在此添加方法
}
