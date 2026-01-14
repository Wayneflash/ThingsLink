package com.iot.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.entity.VideoDevice;
import com.iot.platform.mapper.VideoDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 视频设备服务
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Slf4j
@Service
public class VideoDeviceService extends ServiceImpl<VideoDeviceMapper, VideoDevice> {
    
    @Autowired
    private DeviceGroupService deviceGroupService;
    
    @Autowired
    private WvpService wvpService;
    
    /**
     * 分页查询视频设备列表
     * 
     * @param page 页码
     * @param pageSize 每页数量
     * @param search 搜索关键词
     * @param groupId 分组ID
     * @param userGroupIds 用户可见分组ID列表
     * @return 分页结果
     */
    public IPage<VideoDevice> listPage(int page, int pageSize, String search, Long groupId, List<Long> userGroupIds) {
        Page<VideoDevice> pageQuery = new Page<>(page, pageSize);
        LambdaQueryWrapper<VideoDevice> query = new LambdaQueryWrapper<>();
        
        // 搜索条件：视频名称或编码
        if (StringUtils.hasText(search)) {
            query.and(wrapper -> wrapper
                    .like(VideoDevice::getName, search)
                    .or().like(VideoDevice::getDeviceId, search)
                    .or().like(VideoDevice::getChannelId, search)
            );
        }
        
        // 分组过滤
        if (groupId != null) {
            query.eq(VideoDevice::getGroupId, groupId);
        } else if (userGroupIds != null && !userGroupIds.isEmpty()) {
            // 数据权限：只查询用户可见分组的设备
            query.in(VideoDevice::getGroupId, userGroupIds);
        }
        
        // 按创建时间降序
        query.orderByDesc(VideoDevice::getCreateTime);
        
        return this.page(pageQuery, query);
    }
    
    /**
     * 添加视频设备
     * 
     * @param videoDevice 视频设备
     * @param userGroupIds 用户可见分组ID列表
     * @return 设备ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addVideoDevice(VideoDevice videoDevice, List<Long> userGroupIds) {
        // 校验分组权限
        if (userGroupIds != null && !userGroupIds.isEmpty() && !userGroupIds.contains(videoDevice.getGroupId())) {
            throw new RuntimeException("无权在该分组下添加设备");
        }
        
        // 校验分组是否存在
        DeviceGroup group = deviceGroupService.getById(videoDevice.getGroupId());
        if (group == null) {
            throw new RuntimeException("分组不存在");
        }
        
        // 校验唯一性：(deviceId, channelId) 组合必须唯一
        LambdaQueryWrapper<VideoDevice> query = new LambdaQueryWrapper<>();
        query.eq(VideoDevice::getDeviceId, videoDevice.getDeviceId())
             .eq(VideoDevice::getChannelId, videoDevice.getChannelId());
        VideoDevice existDevice = this.getOne(query);
        if (existDevice != null) {
            throw new RuntimeException("该设备和通道已存在");
        }
        
        // 保存设备
        this.save(videoDevice);
        log.info("添加视频设备成功: id={}, deviceId={}, channelId={}", 
                videoDevice.getId(), videoDevice.getDeviceId(), videoDevice.getChannelId());
        
        return videoDevice.getId();
    }
    
    /**
     * 更新视频设备
     * 
     * @param videoDevice 视频设备
     * @param userGroupIds 用户可见分组ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateVideoDevice(VideoDevice videoDevice, List<Long> userGroupIds) {
        // 校验设备是否存在
        VideoDevice existDevice = this.getById(videoDevice.getId());
        if (existDevice == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 校验数据权限
        if (userGroupIds != null && !userGroupIds.isEmpty() && !userGroupIds.contains(existDevice.getGroupId())) {
            throw new RuntimeException("无权修改该设备");
        }
        
        // 校验新分组权限
        if (userGroupIds != null && !userGroupIds.isEmpty() && !userGroupIds.contains(videoDevice.getGroupId())) {
            throw new RuntimeException("无权将设备移动到该分组");
        }
        
        // 校验分组是否存在
        DeviceGroup group = deviceGroupService.getById(videoDevice.getGroupId());
        if (group == null) {
            throw new RuntimeException("分组不存在");
        }
        
        // 不允许修改 deviceId 和 channelId
        videoDevice.setDeviceId(null);
        videoDevice.setChannelId(null);
        
        // 更新设备
        this.updateById(videoDevice);
        log.info("更新视频设备成功: id={}", videoDevice.getId());
    }
    
    /**
     * 删除视频设备
     * 
     * @param id 设备ID
     * @param userGroupIds 用户可见分组ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideoDevice(Long id, List<Long> userGroupIds) {
        // 校验设备是否存在
        VideoDevice existDevice = this.getById(id);
        if (existDevice == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 校验数据权限
        if (userGroupIds != null && !userGroupIds.isEmpty() && !userGroupIds.contains(existDevice.getGroupId())) {
            throw new RuntimeException("无权删除该设备");
        }
        
        // 删除设备
        this.removeById(id);
        log.info("删除视频设备成功: id={}, deviceId={}, channelId={}", 
                id, existDevice.getDeviceId(), existDevice.getChannelId());
    }
    
    /**
     * 获取视频设备详情（包含WVP状态）
     * 
     * @param id 设备数据库主键ID
     * @param userGroupIds 用户可见分组ID列表
     * @return 设备详情（含状态）
     */
    public JSONObject getVideoDeviceDetail(Long id, List<Long> userGroupIds) {
        // 通过主键ID查询设备
        VideoDevice device = this.getById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        
        // 数据权限校验
        if (userGroupIds != null && !userGroupIds.isEmpty()) {
            if (!userGroupIds.contains(device.getGroupId())) {
                throw new RuntimeException("无权限访问该设备");
            }
        }
        
        // 转换为JSON
        JSONObject result = (JSONObject) JSONObject.toJSON(device);
        
        // 添加分组名称
        DeviceGroup group = deviceGroupService.getById(device.getGroupId());
        if (group != null) {
            result.put("groupName", group.getGroupName());
        }
        
        // 查询WVP设备状态
        try {
            JSONObject status = wvpService.queryDeviceStatus(device.getDeviceId());
            result.put("status", status);
        } catch (Exception e) {
            log.error("查询WVP设备状态失败: deviceId={}", device.getDeviceId(), e);
            result.put("status", null);
        }
        
        return result;
    }
    
    /**
     * 获取视频播放地址
     * 
     * @param deviceId GB28181设备编码
     * @param channelId GB28181通道编码
     * @param userGroupIds 用户可见分组ID列表
     * @return 播放地址信息
     */
    public JSONObject getPlayUrl(String deviceId, String channelId, List<Long> userGroupIds) {
        // 校验设备权限
        LambdaQueryWrapper<VideoDevice> query = new LambdaQueryWrapper<>();
        query.eq(VideoDevice::getDeviceId, deviceId)
             .eq(VideoDevice::getChannelId, channelId);
        
        // 数据权限过滤
        if (userGroupIds != null && !userGroupIds.isEmpty()) {
            query.in(VideoDevice::getGroupId, userGroupIds);
        }
        
        VideoDevice device = this.getOne(query);
        if (device == null) {
            throw new RuntimeException("设备不存在或无权限访问");
        }
        
        // 调用WVP获取播放地址
        JSONObject playInfo = wvpService.getPlayUrl(deviceId, channelId);
        
        // 提取HTTPS HLS地址
        String hlsUrl = playInfo.getString("https_hls");
        if (!StringUtils.hasText(hlsUrl)) {
            // 如果没有HTTPS HLS，尝试使用普通HLS
            hlsUrl = playInfo.getString("hls");
        }
        
        // 构建响应
        JSONObject result = new JSONObject();
        result.put("hlsUrl", hlsUrl);
        result.put("deviceId", deviceId);
        result.put("channelId", channelId);
        
        return result;
    }
}
