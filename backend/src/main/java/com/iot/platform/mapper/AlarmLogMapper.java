package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.AlarmLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警日志Mapper
 */
@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {
}
