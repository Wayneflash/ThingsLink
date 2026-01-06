package com.iot.platform.controller;

import com.iot.platform.common.Result;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.entity.User;
import com.iot.platform.entity.Role;
import com.iot.platform.service.DeviceGroupService;
import com.iot.platform.service.UserService;
import com.iot.platform.mapper.UserMapper;
import com.iot.platform.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 设备分组 Controller
 */
@Slf4j
@RestController
@RequestMapping("/device-groups")
@CrossOrigin
public class DeviceGroupController {
    
    @Resource
    private DeviceGroupService deviceGroupService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private RoleMapper roleMapper;
    
    /**
     * 创建分组
     */
    @PostMapping("/create")
    public Result<DeviceGroup> createGroup(@RequestBody Map<String, Object> params) {
        try {
            DeviceGroup group = new DeviceGroup();
            // 支持name和groupName两种参数名
            String groupName = params.get("name") != null ? 
                params.get("name").toString() : 
                (params.get("groupName") != null ? params.get("groupName").toString() : null);
            
            if (groupName == null || groupName.trim().isEmpty()) {
                return Result.error("分组名称不能为空");
            }
            
            group.setGroupName(groupName);
            
            // 验证parentId
            if (params.get("parentId") != null) {
                Long parentId = Long.valueOf(params.get("parentId").toString());
                if (parentId != 0) {  // 0表示顶级分组，不需要验证
                    DeviceGroup parent = deviceGroupService.getById(parentId);
                    if (parent == null) {
                        return Result.error("上级分组不存在");
                    }
                }
                group.setParentId(parentId);
            } else {
                group.setParentId(0L);  // 默认为顶级分组
            }
            
            if (params.get("description") != null) {
                group.setDescription(params.get("description").toString());
            }
            if (params.get("sort") != null) {
                group.setSort(Integer.valueOf(params.get("sort").toString()));
            }
            if (params.get("groupType") != null) {
                group.setGroupType(params.get("groupType").toString());
            } else {
                group.setGroupType("default");
            }
            
            DeviceGroup result = deviceGroupService.createGroup(group);
            return Result.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建设备分组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取分组树形列表（根据用户权限过滤）
     */
    @PostMapping("/tree")  // 修改为tree接口以符合API文档
    public Result<Map<String, Object>> getGroupTree(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取所有分组
            List<DeviceGroup> allGroups = deviceGroupService.getTreeList();
            
            // 默认返回所有分组（无token或超级管理员）
            Long userGroupId = null;
            boolean isSuperAdmin = false;
            
            // 验证token并获取用户信息
            if (token != null && token.startsWith("Bearer ")) {
                String tokenValue = token.substring(7);
                Long userId = userService.validateToken(tokenValue);
                if (userId != null) {
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        userGroupId = user.getGroupId();
                        
                        // 判断是否为超级管理员
                        if (user.getRoleId() != null) {
                            Role role = roleMapper.selectById(user.getRoleId());
                            isSuperAdmin = role != null && "super_admin".equals(role.getRoleCode());
                        }
                    }
                }
            }
            
            // 构建树形结构
            java.util.List<Map<String, Object>> tree;
            if (isSuperAdmin || userGroupId == null || userGroupId == 0) {
                // 超级管理员或无分组：返回所有分组
                tree = buildTree(allGroups, 0L);
            } else {
                // 普通用户：只返回当前用户所属分组及其子分组
                tree = buildUserGroupTree(allGroups, userGroupId);
            }
            
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("tree", tree);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分组树失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 构建用户权限内的分组树（只返回用户所属分组及其子孙分组）
     */
    private java.util.List<Map<String, Object>> buildUserGroupTree(List<DeviceGroup> allGroups, Long userGroupId) {
        java.util.List<Map<String, Object>> tree = new java.util.ArrayList<>();
        
        // 找到用户所属分组
        DeviceGroup userGroup = allGroups.stream()
            .filter(g -> g.getId().equals(userGroupId))
            .findFirst()
            .orElse(null);
        
        if (userGroup != null) {
            // 以用户所属分组为根，构建子树
            Map<String, Object> node = new java.util.LinkedHashMap<>();
            node.put("id", userGroup.getId());
            node.put("name", userGroup.getGroupName());
            node.put("icon", null);
            node.put("parentId", 0L);  // 将用户分组设为根节点，parentId设为0
            node.put("path", userGroup.getGroupName());  // 路径从当前分组开始
            node.put("deviceCount", 0);
            node.put("level", 1);  // 层级设为1（作为根节点）
            node.put("sort", userGroup.getSort());
            node.put("description", userGroup.getDescription());
            // 递归构建子分组
            node.put("children", buildTree(allGroups, userGroup.getId()));
            tree.add(node);
        }
        
        return tree;
    }
    
    /**
     * 获取分组平铺列表
     */
    @PostMapping("/list")  // 添加平铺列表接口以符合API文档
    public Result<Map<String, Object>> getGroupList() {
        try {
            List<DeviceGroup> groups = deviceGroupService.getTreeList();
            
            // 按照API文档规约构造返回数据
            java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (DeviceGroup group : groups) {
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("id", group.getId());
                item.put("name", group.getGroupName());
                item.put("parentId", group.getParentId());
                item.put("path", buildGroupPath(group.getId(), groups));
                item.put("level", calculateLevel(group.getId(), groups));
                list.add(item);
            }
            
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("list", list);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分组列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 构建分组路径
     */
    private String buildGroupPath(Long groupId, List<DeviceGroup> allGroups) {
        DeviceGroup group = allGroups.stream()
            .filter(g -> g.getId().equals(groupId))
            .findFirst()
            .orElse(null);
        
        if (group == null) {
            return "";
        }
        
        if (group.getParentId() == 0) {
            return group.getGroupName();
        }
        
        String parentPath = buildGroupPath(group.getParentId(), allGroups);
        return parentPath + "/" + group.getGroupName();
    }
    
    /**
     * 计算分组层级
     */
    private int calculateLevel(Long groupId, List<DeviceGroup> allGroups) {
        DeviceGroup group = allGroups.stream()
            .filter(g -> g.getId().equals(groupId))
            .findFirst()
            .orElse(null);
        
        if (group == null || group.getParentId() == 0) {
            return 1;
        }
        
        return calculateLevel(group.getParentId(), allGroups) + 1;
    }
    
    /**
     * 构建树形结构
     */
    private java.util.List<Map<String, Object>> buildTree(List<DeviceGroup> allGroups, Long parentId) {
        java.util.List<Map<String, Object>> tree = new java.util.ArrayList<>();
        
        for (DeviceGroup group : allGroups) {
            if (group.getParentId().equals(parentId)) {
                Map<String, Object> node = new java.util.LinkedHashMap<>();
                node.put("id", group.getId());
                node.put("name", group.getGroupName());
                node.put("icon", null);  // 图标字段，可为空
                node.put("parentId", group.getParentId());
                node.put("path", buildGroupPath(group.getId(), allGroups));
                node.put("deviceCount", 0);  // 设备数量，后续实现
                node.put("level", calculateLevel(group.getId(), allGroups));
                node.put("sort", group.getSort());
                node.put("description", group.getDescription());
                node.put("children", buildTree(allGroups, group.getId()));
                tree.add(node);
            }
        }
        
        return tree;
    }
    
    /**
     * 更新分组
     */
    @PostMapping("/update")
    public Result<DeviceGroup> updateGroup(@RequestBody DeviceGroup group) {
        try {
            DeviceGroup result = deviceGroupService.updateGroup(group);
            return Result.success(result, "更新成功");
        } catch (Exception e) {
            log.error("更新设备分组失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除分组
     */
    @PostMapping("/delete")
    public Result<Void> deleteGroup(@RequestBody Map<String, Long> params) {
        try {
            Long id = params.get("id");
            if (id == null) {
                return Result.error("分组ID不能为空");
            }
            deviceGroupService.deleteGroup(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除设备分组失败", e);
            return Result.error(e.getMessage());
        }
    }
}