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
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加角色
          </el-button>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 角色列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="roles" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" min-width="150" />
        <el-table-column prop="code" label="角色编码" min-width="150" />
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
            <el-button size="small" type="primary" link @click="viewPermissions(row)">
              权限配置
            </el-button>
            <el-button size="small" type="primary" link @click="editRole(row)" v-if="!row.isSuperAdmin">
              编辑
            </el-button>
            <el-button size="small" type="danger" link @click="deleteRole(row)" v-if="!row.isSuperAdmin">
              删除
            </el-button>
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
        <el-form-item label="角色编码" prop="code">
          <el-input
            v-model="roleForm.code"
            placeholder="请输入角色编码（如：device_admin）"
            :disabled="isEditMode"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
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
      title="权限配置"
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
        <el-button type="primary" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('添加角色')
const isEditMode = ref(false)
const roleFormRef = ref(null)
const permissionTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])

// 角色列表
const roles = ref([
  {
    id: 1,
    name: '超级管理员',
    code: 'super_admin',
    description: '系统最高权限角色',
    userCount: 1,
    status: 1,
    isSuperAdmin: true,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    name: '设备管理员',
    code: 'device_admin',
    description: '负责设备管理相关操作',
    userCount: 3,
    status: 1,
    isSuperAdmin: false,
    createTime: '2024-01-05 14:30:00'
  },
  {
    id: 3,
    name: '普通用户',
    code: 'user',
    description: '仅查看权限',
    userCount: 5,
    status: 1,
    isSuperAdmin: false,
    createTime: '2024-01-10 09:15:00'
  }
])

// 权限树
const permissionTree = ref([
  {
    code: 'dashboard',
    name: '数据监控',
    icon: '📊',
    children: null
  },
  {
    code: 'device_group',
    name: '设备分组',
    icon: '📋',
    children: [
      { code: 'device_group:view', name: '查看分组', isButton: true },
      { code: 'device_group:create', name: '创建分组', isButton: true },
      { code: 'device_group:edit', name: '编辑分组', isButton: true },
      { code: 'device_group:delete', name: '删除分组', isButton: true }
    ]
  },
  {
    code: 'device',
    name: '设备管理',
    icon: '📱',
    children: [
      { code: 'device:view', name: '查看设备', isButton: true },
      { code: 'device:create', name: '创建设备', isButton: true },
      { code: 'device:edit', name: '编辑设备', isButton: true },
      { code: 'device:delete', name: '删除设备', isButton: true }
    ]
  },
  {
    code: 'product',
    name: '产品管理',
    icon: '📦',
    children: null
  },
  {
    code: 'user',
    name: '用户管理',
    icon: '👥',
    children: null
  },
  {
    code: 'role',
    name: '角色管理',
    icon: '🎭',
    children: [
      { code: 'role:view', name: '查看角色', isButton: true },
      { code: 'role:create', name: '创建角色', isButton: true },
      { code: 'role:edit', name: '编辑角色', isButton: true },
      { code: 'role:delete', name: '删除角色', isButton: true }
    ]
  },
  {
    code: 'menu',
    name: '菜单管理',
    icon: '📋',
    children: null
  },
  {
    code: 'log',
    name: '操作日志',
    icon: '📝',
    children: null
  }
])

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
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '角色编码只能包含小写字母和下划线', trigger: 'blur' }
  ],
  description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加角色'
  isEditMode.value = false
  dialogVisible.value = true
}

// 编辑角色
const editRole = (role) => {
  dialogTitle.value = '编辑角色'
  isEditMode.value = true
  Object.assign(roleForm, role)
  dialogVisible.value = true
}

// 查看权限
const viewPermissions = (role) => {
  currentRole.value = role
  // 模拟已授权权限
  if (role.isSuperAdmin) {
    // 超级管理员拥有所有权限
    const allPermissions = []
    permissionTree.value.forEach(p => {
      allPermissions.push(p.code)
      if (p.children) {
        p.children.forEach(c => allPermissions.push(c.code))
      }
    })
    checkedPermissions.value = allPermissions
  } else if (role.code === 'device_admin') {
    checkedPermissions.value = ['dashboard', 'device_group', 'device_group:view', 'device_group:create', 
                                  'device', 'device:view', 'device:create', 'device:edit']
  } else {
    checkedPermissions.value = ['dashboard']
  }
  permissionDialogVisible.value = true
}

// 保存权限
const savePermissions = () => {
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]
  
  console.log('保存权限:', allKeys)
  ElMessage.success('权限配置成功')
  permissionDialogVisible.value = false
}

// 删除角色
const deleteRole = (role) => {
  if (role.userCount > 0) {
    ElMessage.warning('该角色下存在用户，无法删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除角色"${role.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = roles.value.findIndex(r => r.id === role.id)
    if (index > -1) {
      roles.value.splice(index, 1)
      ElMessage.success('角色删除成功')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 保存角色
const saveRole = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate((valid) => {
    if (valid) {
      if (isEditMode.value) {
        // 编辑模式：更新角色
        const index = roles.value.findIndex(r => r.id === roleForm.id)
        if (index > -1) {
          roles.value[index] = { ...roles.value[index], ...roleForm }
          ElMessage.success('角色更新成功')
        }
      } else {
        // 添加模式：新增角色
        const newRole = {
          ...roleForm,
          id: roles.value.length > 0 ? Math.max(...roles.value.map(r => r.id)) + 1 : 1,
          userCount: 0,
          isSuperAdmin: false,
          createTime: new Date().toLocaleString('zh-CN')
        }
        roles.value.push(newRole)
        ElMessage.success('角色添加成功')
      }
      dialogVisible.value = false
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

onMounted(() => {
  // 初始化
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
