package com.iot.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 视频设备实体类
 * 对应表：tb_video_device
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Data
@TableName("tb_video_device")
public class VideoDevice {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * GB28181设备编码
     */
    @TableField("device_id")
    private String deviceId;
    
    /**
     * GB28181通道编码
     */
    @TableField("channel_id")
    private String channelId;
    
    /**
     * 视频设备名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 所属分组ID
     */
    @TableField("group_id")
    private Long groupId;
    
    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
