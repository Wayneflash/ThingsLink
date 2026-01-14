package com.iot.platform.dto.video;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 视频播放请求DTO
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Data
public class VideoPlayRequest {
    
    /**
     * GB28181设备编码（20位数字）
     */
    @NotBlank(message = "设备编码不能为空")
    @Pattern(regexp = "^\\d{20}$", message = "设备编码必须为20位数字")
    private String deviceId;
    
    /**
     * GB28181通道编码（20位数字）
     */
    @NotBlank(message = "通道编码不能为空")
    @Pattern(regexp = "^\\d{20}$", message = "通道编码必须为20位数字")
    private String channelId;
}
