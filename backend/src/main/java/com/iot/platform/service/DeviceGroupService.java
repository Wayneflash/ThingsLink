package com.iot.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.mapper.DeviceGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备分组服务
 */
@Slf4j
@Service
public class DeviceGroupService extends ServiceImpl<DeviceGroupMapper, DeviceGroup> {
    
    /**
     * 创建分组
     */
    @Transactional(rollbackFor = Exception.class)
    public DeviceGroup createGroup(DeviceGroup group) {
        // 检查父分组是否存在
        if (group.getParentId() != null && group.getParentId() > 0) {
            DeviceGroup parentGroup = this.getById(group.getParentId());
            if (parentGroup == null) {
                throw new RuntimeException("父分组不存在: " + group.getParentId());
            }
        }
        
        // 设置默认值
        if (group.getSort() == null) {
            group.setSort(0);
        }
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());
        
        this.save(group);
        log.info("创建设备分组成功: {}", group.getGroupName());
        return group;
    }
    
    /**
     * 获取树形结构分组列表
     */
    public List<DeviceGroup> getTreeList() {
        LambdaQueryWrapper<DeviceGroup> query = new LambdaQueryWrapper<>();
        query.orderByAsc(DeviceGroup::getSort)
             .orderByAsc(DeviceGroup::getId);
        return this.list(query);
    }
    
    /**
     * 更新分组
     */
    @Transactional(rollbackFor = Exception.class)
    public DeviceGroup updateGroup(DeviceGroup group) {
        DeviceGroup existGroup = this.getById(group.getId());
        if (existGroup == null) {
            throw new RuntimeException("分组不存在: " + group.getId());
        }
        
        group.setUpdateTime(LocalDateTime.now());
        this.updateById(group);
        log.info("更新设备分组成功: {}", group.getGroupName());
        return group;
    }
    
    /**
     * 删除分组
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long groupId) {
        DeviceGroup group = this.getById(groupId);
        if (group == null) {
            throw new RuntimeException("分组不存在: " + groupId);
        }
        
        // 检查是否有子分组
        LambdaQueryWrapper<DeviceGroup> query = new LambdaQueryWrapper<>();
        query.eq(DeviceGroup::getParentId, groupId);
        long childCount = this.count(query);
        if (childCount > 0) {
            throw new RuntimeException("该分组下还有子分组，无法删除");
        }
        
        this.removeById(groupId);
        log.info("删除设备分组成功: {}", group.getGroupName());
    }
}
