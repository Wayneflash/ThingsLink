package com.iot.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备命令下发 DTO
 */
@Data
public class DeviceCommandDTO {
    
    /**
     * 设备ID
     */
    private String did;
    
    /**
     * 命令内容数组
     */
    private List<CommandContent> content;
    
    /**
     * 平台发送时间
     */
    private String utime;
    
    @Data
    public static class CommandContent {
        /**
         * 属性标识符
         */
        private String addr;
        
        /**
         * 控制值
         */
        private String addrv;
        
        /**
         * 设备编码
         */
        private String pid;
    }
}
