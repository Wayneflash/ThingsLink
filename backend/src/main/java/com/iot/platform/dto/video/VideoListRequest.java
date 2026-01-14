package com.iot.platform.dto.video;

import lombok.Data;

/**
 * 视频设备列表请求DTO
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Data
public class VideoListRequest {
    
    /**
     * 页码（从1开始）
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 20;
    
    /**
     * 搜索关键词（视频名称或编码）
     */
    private String search;
    
    /**
     * 分组ID
     */
    private Long groupId;
}
