package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper
 */
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 配置对象
     */
    SystemConfig getByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置键更新配置值
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 影响行数
     */
    int updateByConfigKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
