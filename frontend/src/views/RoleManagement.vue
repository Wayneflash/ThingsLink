<template>
  <div class="role-management-page">
    <h1 class="page-title">角色管理</h1>
    
    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="never">
      <el-row :gutter="16">
        <el-col :span="18">
          <el-alert
            title="角色说明"
            type="info"
            :closable="false"
            show-icon
          >
            管理系统角色和权限，超级管理员拥有所有权限，其他角色需要分配相应权限
          </el-alert>
        </el-col>
        <el-col :span="6" class="text-right">
          <el-button type="primary" @click="openAddDialog" :disabled="!isCurrentUserSuperAdmin">
            <el-icon><Plus /></el-icon>
            添加角色
          </el-button>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 角色列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="sortedRoles" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
        <el-table-column prop="name" label="角色名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <!-- 超级管理员：可以配置权限、编辑、删除 -->
            <template v-if="isCurrentUserSuperAdmin">
              <el-button size="small" type="primary" link @click="viewPermissions(row)" v-if="!row.isSuperAdmin">
                权限配置
              </el-button>
              <el-button size="small" type="primary" link @click="editRole(row)" v-if="!row.isSuperAdmin">
                编辑
              </el-button>
              <el-button size="small" type="danger" link @click="deleteRole(row)" v-if="!row.isSuperAdmin">
                删除
              </el-button>
              <el-tag v-if="row.isSuperAdmin" size="small" type="info">不可编辑</el-tag>
            </template>
            <!-- 普通用户：只能查看 -->
            <template v-else>
              <el-tooltip content="只有超级管理员才能配置角色权限" placement="top">
                <el-button size="small" type="info" link disabled>
                  仅可查看
                </el-button>
              </el-tooltip>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述（可选）"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="roleForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="isCurrentUserSuperAdmin ? '权限配置' : '查看权限（只读）'"
      width="700px"
    >
      <el-alert
        :title="`正在为「${currentRole?.name}」配置权限`"
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
      />
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="code"
        :default-checked-keys="checkedPermissions"
        :props="{ label: 'name', children: 'children' }"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <span>{{ data.icon }} {{ data.name }}</span>
            <el-tag v-if="data.isButton" size="small" type="info" class="ml-2">按钮</el-tag>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button v-if="isCurrentUserSuperAdmin" type="primary" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRoleList, createRole, updateRole, deleteRole as apiDeleteRole, getRoleDetail } from '@/api/role'

const loading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('添加角色')
const isEditMode = ref(false)
const roleFormRef = ref(null)
const permissionTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])

// 当前登录用户信息
const currentUser = ref(null)

// 判断当前用户是否为超级管理员
const isCurrentUserSuperAdmin = computed(() => {
  if (!currentUser.value) return false
  // 根据 roleId 判断，roleId=1 为超级管理员
  return currentUser.value.roleId === 1
})

// 角色列表（从后端加载）
const roles = ref([])

// 排序后的角色列表：超级管理员永远排在第一位
const sortedRoles = computed(() => {
  return [...roles.value].sort((a, b) => {
    // 超级管理员永远排在第一位
    if (a.isSuperAdmin) return -1
    if (b.isSuperAdmin) return 1
    
    // 其他角色按ID或创建时间排序
    return (a.id || 0) - (b.id || 0)
  })
})

// 序号计算方法
const indexMethod = (index) => {
  return index + 1
}

// 权限树（从后端动态获取）
const permissionTree = ref([])

// 角色表单
const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 打开添加对话框
const openAddDialog = () => {
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能添加角色')
    return
  }
  dialogTitle.value = '添加角色'
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑角色
const editRole = (role) => {
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能编辑角色')
    return
  }
  if (role.isSuperAdmin) {
    ElMessage.warning('超级管理员角色不可编辑')
    return
  }
  dialogTitle.value = '编辑角色'
  isEditMode.value = true
  Object.assign(roleForm, role)
  dialogVisible.value = true
}

// 查看权限
const viewPermissions = async (role) => {
  // 超级管理员角色不可配置权限
  if (role.isSuperAdmin) {
    ElMessage.warning('超级管理员角色不可编辑')
    return
  }
  
  try {
    loading.value = true
    currentRole.value = role
    
    // 获取角色详情，包含已配置的权限
    const roleDetail = await getRoleDetail(role.id)
    
    // 设置权限树（从后端获取）
    permissionTree.value = roleDetail.permissions || []
    
    // 从permissions中提取已授权的权限code
    const grantedPermissions = []
    if (roleDetail.permissions && Array.isArray(roleDetail.permissions)) {
      roleDetail.permissions.forEach(permission => {
        if (permission.granted) {
          grantedPermissions.push(permission.code)
        }
      })
    }
    
    // 设置已勾选的权限
    checkedPermissions.value = grantedPermissions
    
    // 打开对话框
    permissionDialogVisible.value = true
    
    // 等待DOM更新后，使用setCheckedKeys设置勾选状态
    await nextTick()
    if (permissionTreeRef.value) {
      permissionTreeRef.value.setCheckedKeys(grantedPermissions)
      // 如果是普通用户，禁用所有复选框
      if (!isCurrentUserSuperAdmin.value) {
        const treeEl = permissionTreeRef.value.$el
        const checkboxes = treeEl.querySelectorAll('.el-checkbox__input')
        checkboxes.forEach(checkbox => {
          checkbox.style.pointerEvents = 'none'
          checkbox.style.opacity = '0.5'
        })
      }
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败')
  } finally {
    loading.value = false
  }
}

// 保存权限
const savePermissions = async () => {
  try {
    loading.value = true
    
    // 获取当前勾选的权限
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    
    console.log('当前勾选的权限:', checkedKeys)
    
    // 调用后端更新接口
    await updateRole({
      id: currentRole.value.id,
      menuIds: checkedKeys.join(',')
    })
    
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
    
    // 重新加载角色列表
    await loadRoles()
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}

// 删除角色
const deleteRole = async (role) => {
  if (!isCurrentUserSuperAdmin.value) {
    ElMessage.warning('只有超级管理员才能删除角色')
    return
  }
  if (role.isSuperAdmin) {
    ElMessage.warning('超级管理员角色不可删除')
    return
  }
  if (role.userCount > 0) {
    ElMessage.warning('该角色下存在用户，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除角色“${role.name}”吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    await apiDeleteRole(role.id)
    ElMessage.success('角色删除成功')
    // 重新加载角色列表
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 保存角色
const saveRole = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        if (isEditMode.value) {
          // 编辑模式：更新角色
          await updateRole({
            id: roleForm.id,
            name: roleForm.name,
            description: roleForm.description,
            status: roleForm.status
          })
          ElMessage.success('角色更新成功')
        } else {
          // 添加模式：新增角色
          await createRole({
            name: roleForm.name,
            description: roleForm.description,
            status: roleForm.status
          })
          ElMessage.success('角色添加成功')
        }
        dialogVisible.value = false
        // 重新加载角色列表
        await loadRoles()
      } catch (error) {
        console.error('保存角色失败:', error)
        ElMessage.error(error.message || '保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(roleForm, {
    id: null,
    name: '',
    code: '',
    description: '',
    status: 1
  })
  roleFormRef.value?.clearValidate()
}

// 加载角色列表
const loadRoles = async () => {
  try {
    loading.value = true
    const res = await getRoleList({ page: 1, pageSize: 100 })
    roles.value = (res.list || []).map(item => ({
      id: item.id,
      name: item.name,
      code: item.roleCode || item.code,
      description: item.description || '',
      userCount: item.userCount || 0,
      status: item.status,
      isSuperAdmin: item.roleCode === 'super_admin' || item.code === 'super_admin',
      createTime: item.createTime
    }))
  } catch (error) {
    console.error('加载角色列表失败:', error)
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 加载当前用户信息
  const userInfoData = localStorage.getItem('userInfo')
  if (userInfoData) {
    currentUser.value = JSON.parse(userInfoData)
  }
  
  // 加载角色列表
  loadRoles()
})
</script>

<style scoped>
.role-management-page {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.toolbar-card {
  margin-bottom: 16px;
}

.table-card {
  border-radius: 12px;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 8px;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-2 {
  margin-left: 8px;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-tree-node__content) {
  height: 36px;
}
</style>
