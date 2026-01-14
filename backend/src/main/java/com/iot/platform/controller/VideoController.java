package com.iot.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iot.platform.common.Result;
import com.iot.platform.dto.video.*;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.entity.Role;
import com.iot.platform.entity.User;
import com.iot.platform.entity.VideoDevice;
import com.iot.platform.mapper.RoleMapper;
import com.iot.platform.mapper.UserMapper;
import com.iot.platform.service.DeviceGroupService;
import com.iot.platform.service.UserService;
import com.iot.platform.service.VideoDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频管理Controller
 * 
 * @author IoT Platform
 * @since 2026-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/video")
@CrossOrigin
public class VideoController {
    
    @Autowired
    private VideoDeviceService videoDeviceService;
    
    @Autowired
    private DeviceGroupService deviceGroupService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private RoleMapper roleMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 获取当前用户信息
     */
    private User getCurrentUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String actualToken = token.substring(7);
        Long userId = userService.validateToken(actualToken);
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }
    
    /**
     * 判断用户是否为超级管理员
     */
    private boolean isSuperAdmin(User user) {
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        Role role = roleMapper.selectById(user.getRoleId());
        return role != null && "super_admin".equals(role.getRoleCode());
    }
    
    /**
     * 获取用户可见分组ID列表
     */
    private List<Long> getUserVisibleGroupIds(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        
        // 超级管理员查看所有分组
        if (isSuperAdmin(user)) {
            return null; // null表示不过滤
        }
        
        // 普通用户只能查看本分组及下级分组
        // 注意：这里需要自己实现获取子分组的逻辑，或者简单处理只返回用户所在分组
        if (user.getGroupId() != null) {
            // 简单实现：只返回用户所在分组（如需要包含子分组，需要单独实现）
            return Collections.singletonList(user.getGroupId());
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 16.1 视频设备列表
     */
    @PostMapping("/list")
    public Result<?> list(@RequestBody VideoListRequest request, 
                          @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);
            
            // 分页查询
            IPage<VideoDevice> page = videoDeviceService.listPage(
                    request.getPage(),
                    request.getPageSize(),
                    request.getSearch(),
                    request.getGroupId(),
                    userGroupIds
            );
            
            // 组装响应数据
            List<Map<String, Object>> list = page.getRecords().stream().map(device -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", device.getId());
                map.put("deviceId", device.getDeviceId());
                map.put("channelId", device.getChannelId());
                map.put("name", device.getName());
                map.put("groupId", device.getGroupId());
                map.put("remark", device.getRemark());
                map.put("createTime", device.getCreateTime());
                map.put("updateTime", device.getUpdateTime());
                
                // 添加分组名称
                if (device.getGroupId() != null) {
                    DeviceGroup group = deviceGroupService.getById(device.getGroupId());
                    map.put("groupName", group != null ? group.getGroupName() : "");
                }
                
                return map;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", page.getTotal());
            result.put("page", page.getCurrent());
            result.put("pageSize", page.getSize());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询视频设备列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 16.2 视频设备详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id,
                            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);
            
            // 查询设备详情（含WVP状态）
            JSONObject detail = videoDeviceService.getVideoDeviceDetail(id, userGroupIds);
            
            return Result.success(detail);
        } catch (Exception e) {
            log.error("查询视频设备详情失败: id={}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 16.3 获取视频流
     */
    @PostMapping("/play")
    public Result<?> play(@Validated @RequestBody VideoPlayRequest request,
                          @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);
            
            // 获取播放地址
            JSONObject playInfo = videoDeviceService.getPlayUrl(
                    request.getDeviceId(),
                    request.getChannelId(),
                    userGroupIds
            );
            
            return Result.success(playInfo);
        } catch (Exception e) {
            log.error("获取视频流失败: deviceId={}, channelId={}", 
                    request.getDeviceId(), request.getChannelId(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 16.4 添加视频设备
     */
    @PostMapping("/add")
    public Result<?> add(@Validated @RequestBody VideoAddRequest request,
                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);
            
            // 转换为实体
            VideoDevice device = new VideoDevice();
            BeanUtils.copyProperties(request, device);
            
            // 添加设备
            Long id = videoDeviceService.addVideoDevice(device, userGroupIds);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            
            return Result.success(result, "添加成功");
        } catch (Exception e) {
            log.error("添加视频设备失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 16.5 编辑视频设备
     */
    @PostMapping("/update")
    public Result<?> update(@Validated @RequestBody VideoUpdateRequest request,
                            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);
            
            // 转换为实体
            VideoDevice device = new VideoDevice();
            BeanUtils.copyProperties(request, device);
            
            // 更新设备
            videoDeviceService.updateVideoDevice(device, userGroupIds);
            
            return Result.success("修改成功");
        } catch (Exception e) {
            log.error("编辑视频设备失败: id={}", request.getId(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 16.6 删除视频设备
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取当前用户
            User user = getCurrentUser(token);
            if (user == null) {
                return Result.error("未登录或登录已过期");
            }

            // 获取用户可见分组
            List<Long> userGroupIds = getUserVisibleGroupIds(user);

            // 删除设备
            videoDeviceService.deleteVideoDevice(id, userGroupIds);

            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除视频设备失败", e);
            return Result.error(e.getMessage());
        }
    }
}
