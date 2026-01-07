package com.iot.platform.dto;

import lombok.Data;

import java.util.Map;

/**
 * 报警配置DTO
 * 每个物模型属性作为独立的报警规则，独立触发、独立恢复、独立堆叠
 */
@Data
public class AlarmConfigDTO {
    
    /**
     * 处理人ID（全局配置，一个设备只配置一个处理人）
     */
    private Long notifyUser;
    
    /**
     * 报警堆叠：true=开启（恢复前不重复报警），false=关闭（全局配置）
     */
    private Boolean stackMode;
    
    /**
     * 物模型属性监控配置（Map结构）
     * key: 物模型属性标识符（如：temperature）
     * value: 该属性的报警配置
     */
    private Map<String, MetricConfig> metrics;
    
    /**
     * 单个物模型属性的报警配置
     */
    @Data
    public static class MetricConfig {
        /**
         * 是否启用监控
         */
        private Boolean enabled;
        
        /**
         * 运算符：> < =
         */
        private String operator;
        
        /**
         * 阈值
         */
        private Double threshold;
        
        /**
         * 报警级别：critical(严重), warning(警告), info(提示)
         */
        private String level;
    }
}
