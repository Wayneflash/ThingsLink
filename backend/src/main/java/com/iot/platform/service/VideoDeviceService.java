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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
            if (status != null) {
                // 转换UTC时间为中国时区（UTC+8）
                convertUtcTimeToChinaTime(status);
                result.put("status", status);
            } else {
                // WVP查询返回null，可能是设备不存在或WVP服务异常
                log.warn("WVP设备状态查询返回null: deviceId={}", device.getDeviceId());
                result.put("status", null);
                result.put("statusError", "无法获取设备状态，可能是设备未在WVP平台注册或WVP服务异常");
            }
        } catch (Exception e) {
            log.error("查询WVP设备状态失败: deviceId={}", device.getDeviceId(), e);
            result.put("status", null);
            result.put("statusError", "查询设备状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 通过设备编码获取视频设备详情（包含WVP状态）
     * 
     * @param deviceId GB28181设备编码
     * @param channelId GB28181通道编码
     * @param userGroupIds 用户可见分组ID列表
     * @return 设备详情（含状态）
     */
    public JSONObject getVideoDeviceDetailByCode(String deviceId, String channelId, List<Long> userGroupIds) {
        // 通过设备编码和通道编码查询设备
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
            if (status != null) {
                // 转换UTC时间为中国时区（UTC+8）
                convertUtcTimeToChinaTime(status);
                result.put("status", status);
            } else {
                // WVP查询返回null，可能是设备不存在或WVP服务异常
                log.warn("WVP设备状态查询返回null: deviceId={}", device.getDeviceId());
                result.put("status", null);
                result.put("statusError", "无法获取设备状态，可能是设备未在WVP平台注册或WVP服务异常");
            }
        } catch (Exception e) {
            log.error("查询WVP设备状态失败: deviceId={}", device.getDeviceId(), e);
            result.put("status", null);
            result.put("statusError", "查询设备状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 将WVP返回的UTC时间转换为中国时区（UTC+8）
     * 
     * @param status WVP设备状态JSON对象
     */
    private void convertUtcTimeToChinaTime(JSONObject status) {
        if (status == null) {
            return;
        }
        
        // 常见的时间字段名
        String[] timeFields = {"registerTime", "keepaliveTime", "createTime", "updateTime", "time"};
        
        for (String field : timeFields) {
            Object timeValue = status.get(field);
            if (timeValue != null) {
                try {
                    String timeStr = timeValue.toString();
                    if (timeStr != null && !timeStr.isEmpty()) {
                        // 尝试解析时间字符串（支持多种格式）
                        LocalDateTime utcTime = parseTimeString(timeStr);
                        if (utcTime != null) {
                            // 转换为中国时区（UTC+8）
                            ZonedDateTime utcZoned = utcTime.atZone(ZoneId.of("UTC"));
                            ZonedDateTime chinaZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
                            LocalDateTime chinaTime = chinaZoned.toLocalDateTime();
                            
                            // 格式化为 yyyy-MM-dd HH:mm:ss 格式
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            String chinaTimeStr = chinaTime.format(formatter);
                            status.put(field, chinaTimeStr);
                            
                            log.debug("时间字段 {} 转换: UTC {} -> China {}", field, timeStr, chinaTimeStr);
                        }
                    }
                } catch (Exception e) {
                    log.warn("转换时间字段失败: field={}, value={}, error={}", field, timeValue, e.getMessage());
                }
            }
        }
    }
    
    /**
     * 解析时间字符串（支持多种格式）
     * 
     * @param timeStr 时间字符串
     * @return LocalDateTime对象（UTC时区），解析失败返回null
     */
    private LocalDateTime parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        
        // 先尝试解析带时区的格式（ISO格式，如 2026-01-16T10:30:00Z）
        try {
            // 如果包含Z或+/-时区标识，使用ZonedDateTime解析
            if (timeStr.contains("Z") || timeStr.contains("+") || timeStr.matches(".*\\d{2}:\\d{2}$")) {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
                // 转换为UTC时区的LocalDateTime
                return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }
        
        // 支持的格式列表（假设都是UTC时间）
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(timeStr, formatter);
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }
        
        log.warn("无法解析时间字符串: {}", timeStr);
        return null;
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
    
    /**
     * 查询录像列表（含权限校验）
     * 
     * @param deviceId GB28181设备编码
     * @param channelId GB28181通道编码
     * @param startTime 开始时间（设备时间，格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（设备时间，格式：yyyy-MM-dd HH:mm:ss）
     * @param userGroupIds 用户可见分组ID列表
     * @return 录像信息
     */
    public JSONObject queryRecord(String deviceId, String channelId, String startTime, String endTime, List<Long> userGroupIds) {
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
        
        // 调用WVP查询录像（时间直接传递，不做转换）
        return wvpService.queryRecord(deviceId, channelId, startTime, endTime);
    }
    
    /**
     * 获取录像回放流地址（含权限校验）
     * 
     * @param deviceId GB28181设备编码
     * @param channelId GB28181通道编码
     * @param startTime 开始时间（设备时间，格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（设备时间，格式：yyyy-MM-dd HH:mm:ss）
     * @param userGroupIds 用户可见分组ID列表
     * @return 回放流地址信息
     */
    public JSONObject getPlaybackUrl(String deviceId, String channelId, String startTime, String endTime, List<Long> userGroupIds) {
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
        
        // 调用WVP获取回放地址（时间直接传递，不做转换）
        JSONObject playbackInfo = wvpService.getPlaybackUrl(deviceId, channelId, startTime, endTime);
        
        // 提取HTTPS HLS地址（优先使用HTTPS HLS，与实时播放保持一致）
        String hlsUrl = playbackInfo.getString("https_hls");
        if (!StringUtils.hasText(hlsUrl)) {
            // 如果没有HTTPS HLS，尝试使用普通HLS
            hlsUrl = playbackInfo.getString("hls");
        }
        
        // 构建响应
        JSONObject result = new JSONObject();
        result.put("hlsUrl", hlsUrl);
        result.put("deviceId", deviceId);
        result.put("channelId", channelId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        
        return result;
    }
}
