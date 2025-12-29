<template>
  <div class="device-groups-page">
    <!-- 页面布局：左侧分组树 + 右侧设备列表 -->
    <div class="group-container">
      <!-- 左侧：分组树 -->
      <el-card class="tree-panel" shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold">分组树</span>
            <el-button 
              v-if="isCurrentUserSuperAdmin" 
              type="primary" 
              size="small" 
              @click="openGroupDialog"
            >
              <el-icon><Plus /></el-icon>
              新建
            </el-button>
            <el-tooltip v-else content="只有超级管理员才能创建分组" placement="top">
              <el-button type="primary" size="small" disabled>
                <el-icon><Plus /></el-icon>
                新建
              </el-button>
            </el-tooltip>
          </div>
        </template>
        
        <!-- 分组树 -->
        <GroupTree 
          :groups="groups" 
          :current-group-id="currentGroupId"
          :show-actions="isCurrentUserSuperAdmin"
          @select="selectGroup"
          @edit="editGroup"
          @delete="deleteGroup"
        />
      </el-card>
      
      <!-- 右侧：设备列表 -->
      <el-card class="device-panel" shadow="hover">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold">{{ currentGroupTitle }}</span>
          </div>
        </template>
        
        <el-table :data="deviceList" stripe v-loading="loading">
          <el-table-column prop="name" label="设备名称" min-width="150" />
          <el-table-column prop="code" label="设备编码" min-width="150" />
          <el-table-column prop="product" label="产品类型" min-width="120" />
          <el-table-column prop="group" label="所属分组" min-width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'online' ? 'success' : 'info'" size="small">
                {{ row.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 创建/编辑分组对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="如：办公区域、生产车间、A栋1楼" />
          <div class="form-hint">💡 同一父分组下名称必须唯一，但不同父分组下可以有同名子分组</div>
        </el-form-item>
        
       <el-form-item label="父级分组">
          <el-input value="总分组" disabled />
          <div class="form-hint">💡 所有分组都属于总分组，暂不支持多级嵌套</div>
        </el-form-item>
        
        <el-form-item label="分组描述">
          <el-input
            v-model="groupForm.desc"
            type="textarea"
            :rows="3"
            placeholder="描述该分组的用途、范围等..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getGroupTree, createGroup, updateGroup, deleteGroup as apiDeleteGroup } from '@/api/group'
import { getDeviceList } from '@/api/device'
import GroupTree from '@/components/GroupTree.vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('创建分组')
const isEditMode = ref(false)
const groupFormRef = ref(null)
const currentGroupId = ref(null)

// 当前登录用户信息
const currentUser = ref(null)

// 判断当前用户是否为超级管理员
const isCurrentUserSuperAdmin = computed(() => {
  if (!currentUser.value) return false
  // 根据 roleId 判断，roleId=1 为超级管理员
  return currentUser.value.roleId === 1
})

// 分组数据
const groups = ref([])

// 设备数据
const mockDevices = ref([])

// 分组表单
const groupForm = reactive({
  id: null,
  name: '',
  parentId: 0, // 默认父级为0（顶级分组）
  desc: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }]
}

// 顶级分组（包括总分组和其他顶级分组）
const topLevelGroups = computed(() => {
  return groups.value.filter(g => g.parentId === 0)
})

// 总设备数
const totalDevices = computed(() => {
  return groups.value.reduce((sum, g) => sum + (g.deviceCount || 0), 0)
})

// 当前分组标题
const currentGroupTitle = computed(() => {
  if (!currentGroupId.value) {
    return '请选择分组'
  }
  const group = groups.value.find(g => g.id === currentGroupId.value)
  return group ? group.name : ''
})

// 设备列表
const deviceList = computed(() => {
  if (!currentGroupId.value) return []
  // 根据 currentGroupId 过滤设备
  return mockDevices.value.filter(d => d.groupId === currentGroupId.value)
})

// 获取子分组
const getChildren = (parentId) => {
  return groups.value.filter(g => g.parentId === parentId)
}



// 选择分组
const selectGroup = (id) => {
  currentGroupId.value = id
}

// 打开对话框
const openGroupDialog = () => {
  // 检查权限
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能创建分组')
    return
  }
  
  dialogTitle.value = '创建分组'
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑分组
const editGroup = (group) => {
  // 检查权限
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能编辑分组')
    return
  }
  
  // 总分组（ID=1）不允许编辑
  if (group.id === 1) {
    ElMessage.warning('总分组不允许编辑')
    return
  }
  
  dialogTitle.value = '编辑分组'
  isEditMode.value = true
  Object.assign(groupForm, group)
  dialogVisible.value = true
}

// 删除分组
const deleteGroup = async (group) => {
  // 检查权限
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能删除分组')
    return
  }
  
  // 总分组（ID=1）不允许删除
  if (group.id === 1) {
    ElMessage.warning('总分组不允许删除')
    return
  }
  
  // 检查是否有子分组
  const hasChildren = groups.value.some(g => g.parentId === group.id)
  
  let confirmMessage = `确定要删除分组「${group.name}」吗？`
  if (hasChildren) {
    confirmMessage = `分组「${group.name}」下还有子分组，删除后所有子分组也将被删除，确定继续吗？`
  }
  
  try {
    await ElMessageBox.confirm(confirmMessage, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用接口删除
    await apiDeleteGroup({ id: group.id })
    
    // 重新加载分组列表
    await loadGroups()
    
    ElMessage.success('分组删除成功')
    
    if (currentGroupId.value === group.id) {
      currentGroupId.value = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 保存分组
const saveGroup = async () => {
  if (!groupFormRef.value) return
  
  await groupFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          // 调用编辑接口
          await updateGroup({
            id: groupForm.id,
            groupName: groupForm.name,
            parentId: groupForm.parentId,
            description: groupForm.desc
          })
          ElMessage.success('分组修改成功')
        } else {
          // 调用创建接口
          await createGroup({
            name: groupForm.name,
            parentId: groupForm.parentId,
            description: groupForm.desc
          })
          ElMessage.success('分组创建成功')
        }
        
        // 重新加载分组列表
        await loadGroups()
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      }
    }
  })
}

// 加载分组列表
const loadGroups = async () => {
  try {
    loading.value = true
    const res = await getGroupTree()
    // 后端返回的是树形结构 {tree: [...]}
    // 需要扁平化树形数据为列表
    groups.value = flattenTree(res.tree || [])
    
    console.log('=== 分组数据加载 ===', groups.value)
    console.log('顶级分组 (parentId=0):', groups.value.filter(g => g.parentId === 0))
    
    // 如果有分组且没有选中，默认选中第一个顶级分组
    if (groups.value.length > 0 && !currentGroupId.value) {
      const firstTopGroup = groups.value.find(g => g.parentId === 0)
      if (firstTopGroup) {
        currentGroupId.value = firstTopGroup.id
        await loadDevices()
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载分组失败')
  } finally {
    loading.value = false
  }
}

// 扁平化树形数据
const flattenTree = (tree) => {
  const result = []
  const flatten = (nodes) => {
    if (!Array.isArray(nodes)) return
    nodes.forEach(node => {
      result.push({
        id: node.id,
        name: node.name,
        parentId: node.parentId || 0,
        path: node.path,
        level: node.level,
        deviceCount: node.deviceCount || 0,
        desc: node.description || ''
      })
      if (node.children && node.children.length > 0) {
        flatten(node.children)
      }
    })
  }
  flatten(tree)
  return result
}

// 加载设备列表
const loadDevices = async () => {
  if (!currentGroupId.value) {
    mockDevices.value = []
    return
  }
  
  try {
    const res = await getDeviceList({
      page: 1,
      pageSize: 100,
      groupId: currentGroupId.value
    })
    mockDevices.value = res.list || []
  } catch (error) {
    console.error('加载设备失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(groupForm, {
    id: null,
    name: '',
    parentId: 1, // 默认父级为总分组（ID=1）
    desc: ''
  })
  groupFormRef.value?.clearValidate()
}

onMounted(() => {
  // 加载当前用户信息
  const userInfoData = localStorage.getItem('userInfo')
  if (userInfoData) {
    currentUser.value = JSON.parse(userInfoData)
  }
  
  // 加载分组和设备数据
  loadGroups()
  loadDevices()
})
</script>

<style scoped>
.device-groups-page {
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.group-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.tree-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
}

.device-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
}

.tree-item {
  padding: 10px 14px;
  margin: 3px 0;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s;
  font-size: 14px;
  color: #333;
}

.tree-item.root-group {
  background: #f5f7fa;
  font-weight: 500;
  font-size: 15px;
  margin-bottom: 10px;
}

.tree-item-content-root {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
}

.expand-icon {
  transition: transform 0.2s;
  font-size: 14px;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.tree-children {
  margin-left: 16px;
  padding-left: 12px;
  border-left: 2px solid #e5e7eb;
}

.tree-item:hover {
  background: #f5f7fa;
}

.tree-item.active {
  background: #409eff;
  color: white;
}

.tree-item.child {
  margin-left: 24px;
}

.tree-item-content {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.tree-item-content:hover {
  background: #f5f7fa;
}

.tree-item-content.active {
  background: #409eff;
  color: white;
}

.tree-item-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.device-count {
  font-size: 13px;
  color: #86868b;
  margin-right: 8px;
  font-weight: 400;
}

.form-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e5e7;
  background: #fafafa;
}

:deep(.el-card__header .font-semibold) {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background: #f5f5f7;
  color: #1d1d1f;
  font-weight: 600;
}

:deep(.el-button--small) {
  font-size: 12px;
  padding: 4px 8px;
}
</style>
