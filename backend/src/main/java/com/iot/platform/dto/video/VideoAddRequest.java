package com.iot.platform.dto.video;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 添加视频设备请求DTO
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Data
public class VideoAddRequest {
    
    /**
     * 视频设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(min = 1, max = 50, message = "设备名称长度为1-50字符")
    private String name;
    
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
    
    /**
     * 所属分组ID
     */
    @NotNull(message = "所属分组不能为空")
    private Long groupId;
    
    /**
     * 备注说明
     */
    @Size(max = 500, message = "备注说明最多500字符")
    private String remark;
}
