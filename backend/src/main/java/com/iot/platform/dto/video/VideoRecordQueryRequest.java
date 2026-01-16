package com.iot.platform.dto.video;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 录像查询请求DTO
 * 
 * @author IoT Platform
 * @since 2026-01-16
 */
@Data
public class VideoRecordQueryRequest {
    
    @NotBlank(message = "设备编码不能为空")
    @Pattern(regexp = "^\\d+$", message = "设备编码必须为数字")
    private String deviceId;
    
    @NotBlank(message = "通道编码不能为空")
    @Pattern(regexp = "^\\d+$", message = "通道编码必须为数字")
    private String channelId;
    
    @NotBlank(message = "开始时间不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "开始时间格式错误，应为：yyyy-MM-dd HH:mm:ss")
    private String startTime;
    
    @NotBlank(message = "结束时间不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$", message = "结束时间格式错误，应为：yyyy-MM-dd HH:mm:ss")
    private String endTime;
}
