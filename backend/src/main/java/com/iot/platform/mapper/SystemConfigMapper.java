package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper接口
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置
     * 
     * @param key 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_config WHERE config_key = #{key}")
    SystemConfig selectByKey(String key);
}
