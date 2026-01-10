package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.entity.SystemConfig;
import com.iot.platform.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 系统配置服务
 * 用于管理系统配置（如平台MQTT连接配置）
 */
@Slf4j
@Service
public class SystemConfigService {
    
    @Resource
    private SystemConfigMapper systemConfigMapper;
    
    /**
     * 根据配置键获取配置
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        try {
            SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, configKey)
            );
            if (config != null) {
                return config.getConfigValue();
            }
            return defaultValue;
        } catch (Exception e) {
            log.error("获取配置失败，configKey: {}", configKey, e);
            return defaultValue;
        }
    }
    
    /**
     * 根据配置键获取配置对象
     * @param configKey 配置键
     * @return 配置对象
     */
    public SystemConfig getConfig(String configKey) {
        try {
            return systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, configKey)
            );
        } catch (Exception e) {
            log.error("获取配置失败，configKey: {}", configKey, e);
            return null;
        }
    }
    
    /**
     * 更新配置（如果不存在则创建）
     * @param configKey 配置键
     * @param configValue 配置值
     * @param description 配置描述
     */
    public void updateConfig(String configKey, String configValue, String description) {
        try {
            SystemConfig existingConfig = getConfig(configKey);
            if (existingConfig != null) {
                // 更新现有配置
                existingConfig.setConfigValue(configValue);
                existingConfig.setDescription(description);
                existingConfig.setUpdateTime(LocalDateTime.now());
                systemConfigMapper.updateById(existingConfig);
                log.info("更新配置成功: {} = {}", configKey, configValue);
            } else {
                // 创建新配置
                SystemConfig newConfig = new SystemConfig();
                newConfig.setConfigKey(configKey);
                newConfig.setConfigValue(configValue);
                newConfig.setDescription(description);
                newConfig.setCreateTime(LocalDateTime.now());
                newConfig.setUpdateTime(LocalDateTime.now());
                systemConfigMapper.insert(newConfig);
                log.info("创建配置成功: {} = {}", configKey, configValue);
            }
        } catch (Exception e) {
            log.error("更新配置失败，configKey: {}, configValue: {}", configKey, configValue, e);
            throw new RuntimeException("更新配置失败: " + e.getMessage());
        }
    }
}
