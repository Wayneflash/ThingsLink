package com.iot.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 告警配置DTO
 */
@Data
public class AlarmConfigDTO {
    
    /**
     * 告警级别：critical(严重), warning(警告), info(提示)
     */
    private String level;
    
    /**
     * 监控条件列表
     */
    private List<AlarmCondition> conditions;
    
    /**
     * 通知人员ID列表
     */
    private List<Long> notifyUsers;
    
    /**
     * 告警堆叠：true=开启（恢复前不重复告警），false=关闭
     */
    private Boolean stackMode;
    
    /**
     * 监控条件
     */
    @Data
    public static class AlarmCondition {
        /**
         * 监控指标（对应物模型属性标识符）
         */
        private String metric;
        
        /**
         * 运算符：> < =
         */
        private String operator;
        
        /**
         * 阈值
         */
        private Double threshold;
    }
}
