package com.iot.platform.service;

import com.iot.platform.entity.SystemConfig;
import com.iot.platform.mapper.SystemConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WVP配置服务
 * 用于读取WVP服务器配置信息
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Slf4j
@Service
public class WvpConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    /**
     * 配置缓存，避免频繁查询数据库
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    /**
     * 获取WVP服务器地址
     * 
     * @return WVP服务器地址
     */
    public String getWvpServerUrl() {
        return getConfig("wvp.server.url", "https://lxs.fjqiaolong.com:18082");
    }
    
    /**
     * 获取WVP登录用户名
     * 
     * @return WVP用户名
     */
    public String getWvpUsername() {
        return getConfig("wvp.server.username", "admin");
    }
    
    /**
     * 获取WVP登录密码（MD5加密）
     * 
     * @return WVP密码（MD5）
     */
    public String getWvpPassword() {
        return getConfig("wvp.server.password", "b59c67bf196a4758191e42f76670ceba");
    }
    
    /**
     * 获取配置（带缓存和默认值）
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private String getConfig(String key, String defaultValue) {
        // 从缓存读取
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        
        try {
            // 从数据库查询
            SystemConfig config = systemConfigMapper.selectByKey(key);
            String value = (config != null) ? config.getConfigValue() : defaultValue;
            
            // 存入缓存
            configCache.put(key, value);
            log.debug("配置加载成功: {} = {}", key, value);
            
            return value;
        } catch (Exception e) {
            log.error("加载配置失败: {}, 使用默认值: {}", key, defaultValue, e);
            return defaultValue;
        }
    }
    
    /**
     * 刷新配置缓存
     */
    public void refreshCache() {
        configCache.clear();
        log.info("配置缓存已刷新");
    }
}
