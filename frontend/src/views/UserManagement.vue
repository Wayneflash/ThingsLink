<template>
  <div class="user-management-page">
    <h1 class="page-title">用户管理</h1>
    
    <div class="user-container">
      <!-- 左侧分组树 -->
      <el-card class="tree-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <strong style="font-size: 15px;">设备分组</strong>
          </div>
        </template>
        
        <!-- 默认分组 -->
        <div class="tree-item" :class="{ active: currentGroupId === 'all' }" @click="selectGroup('all')">
          <span>默认分组</span>
          <span class="user-count">{{ users.length }}</span>
        </div>
        
        <div v-for="group in topGroups" :key="group.id">
          <div class="tree-item" :class="{ active: currentGroupId === group.id }" @click="selectGroup(group.id)">
            <span>{{ group.name }}</span>
            <span class="user-count">{{ getUserCount(group.id) }}</span>
          </div>
          
          <!-- 子分组 -->
          <div v-for="child in getChildren(group.id)" :key="child.id" class="tree-item child">
            <div class="tree-item-content" :class="{ active: currentGroupId === child.id }" @click="selectGroup(child.id)">
              <span>{{ child.name }}</span>
              <span class="user-count">{{ getUserCount(child.id) }}</span>
            </div>
          </div>
        </div>
        
        <div class="tree-hint">
          💡 点击分组查看该分组下的用户
        </div>
      </el-card>

      <!-- 右侧用户列表 -->
      <el-card class="user-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <h3 style="margin: 0; font-size: 16px;">{{ currentGroupTitle }}</h3>
              <el-button size="small" :type="currentGroupId === 'all' ? 'primary' : ''" @click="selectGroup('all')">
                全部用户
              </el-button>
            </div>
            <div style="display: flex; gap: 12px;">
              <el-input
                v-model="searchQuery"
                placeholder="🔍 搜索用户名或姓名"
                clearable
                style="width: 200px;"
                @input="handleSearch"
              />
              <el-button type="primary" @click="openAddDialog">
                <el-icon><Plus /></el-icon>
                创建用户
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table :data="filteredUsers" stripe v-loading="loading">
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="姓名" min-width="120" />
          <el-table-column prop="groupName" label="所属分组" min-width="120" />
          <el-table-column prop="roleName" label="角色" min-width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.roleId)" size="small">{{ row.roleName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <template v-if="row.isSuper">
                <el-button size="small" text disabled style="opacity: 0.5;">不可操作</el-button>
              </template>
              <template v-else>
                <el-button size="small" text @click="editUser(row)">编辑</el-button>
                <el-button size="small" text type="warning" @click="toggleUserStatus(row)">
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" text type="danger" @click="deleteUser(row)">
                  删除
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 创建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="如：zhangsan、lisi" :disabled="isEditMode" />
          <div class="form-hint">💡 用于登录的账号，建议使用英文，创建后不可修改</div>
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
          <div class="form-hint">💡 密码将采用 BCrypt 加密存储，建议 6 位以上</div>
        </el-form-item>
        
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="如：张三、李四" />
          <div class="form-hint">💡 用户的真实姓名，用于显示和识别</div>
        </el-form-item>
        
        <el-form-item label="所属分组" prop="groupId">
          <el-select v-model="userForm.groupId" placeholder="请选择所属分组" style="width: 100%" :disabled="isSuperAdminRole">
            <el-option label="默认分组" :value="0" />
            <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id" />
          </el-select>
          <div class="form-hint" :class="{ 'hint-super': isSuperAdminRole }">
            {{ groupHintText }}
          </div>
        </el-form-item>
        
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色" style="width: 100%" @change="handleRoleChange">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
          <div class="form-hint">💡 选择用户的角色，角色决定了用户的菜单权限</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getGroupList } from '@/api/group'
import { getUserList, createUser, updateUser, deleteUser as apiDeleteUser, updateUserStatus } from '@/api/user'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('创建用户')
const isEditMode = ref(false)
const userFormRef = ref(null)
const searchQuery = ref('')
const currentGroupId = ref('all')

// 分组数据
const groups = ref([])

// 用户列表
const users = ref([])

// 角色列表
const roles = ref([
  { id: 1, name: '超级管理员', isSuperAdmin: true },
  { id: 2, name: '设备管理员', isSuperAdmin: false },
  { id: 3, name: '数据查看员', isSuperAdmin: false },
  { id: 4, name: '运维人员', isSuperAdmin: false }
])

// 用户表单
const userForm = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  groupId: null,
  roleId: null,
  status: 1
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: ['blur', 'change'] },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: ['blur', 'change'] }
  ],
  realName: [
    { required: true, message: '姓名不能为空', trigger: ['blur', 'change'] },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
    { min: 6, message: '密码长度至少 6 个字符', trigger: ['blur', 'change'] }
  ],
  groupId: [{ required: true, message: '请选择所属分组', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 顶级分组
const topGroups = computed(() => groups.value.filter(g => g.parentId === 0))

// 获取子分组
const getChildren = (parentId) => {
  return groups.value.filter(g => g.parentId === parentId)
}

// 获取分组用户数
const getUserCount = (groupId) => {
  return users.value.filter(u => u.groupId === groupId).length
}

// 当前分组标题
const currentGroupTitle = computed(() => {
  if (currentGroupId.value === 'all') {
    return `默认分组 (${users.value.length}人)`
  }
  const group = groups.value.find(g => g.id === currentGroupId.value)
  if (group) {
    const count = getUserCount(group.id)
    return `${group.name} (${count}人)`
  }
  return '请选择分组'
})

// 筛选后的用户列表
const filteredUsers = computed(() => {
  let result = users.value
  
  // 按分组筛选
  if (currentGroupId.value !== 'all') {
    const groupIds = getGroupWithChildren(currentGroupId.value)
    result = result.filter(u => groupIds.includes(u.groupId))
  }
  
  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(u =>
      u.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.realName.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  
  // 添加分组名称
  return result.map(u => {
    const group = groups.value.find(g => g.id === u.groupId)
    return {
      ...u,
      groupName: group ? group.name : '全部'
    }
  })
})

// 获取分组及其所有子分组的ID
const getGroupWithChildren = (groupId) => {
  let result = [groupId]
  const children = groups.value.filter(g => g.parentId === groupId)
  children.forEach(child => {
    result = result.concat(getGroupWithChildren(child.id))
  })
  return result
}

// 选择分组
const selectGroup = (id) => {
  currentGroupId.value = id
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在 computed 中
}

// 获取角色类型
const getRoleType = (roleId) => {
  const role = roles.value.find(r => r.id === roleId)
  return role?.isSuperAdmin ? 'danger' : 'info'
}

// 是否为超级管理员角色
const isSuperAdminRole = computed(() => {
  const role = roles.value.find(r => r.id === userForm.roleId)
  return role?.isSuperAdmin || false
})

// 分组提示文本
const groupHintText = computed(() => {
  if (isSuperAdminRole.value) {
    return '🔒 超级管理员自动归属最顶级分组，拥有全局数据权限'
  }
  return '💡 决定用户能看到哪些设备数据，用户可查看所属分组及其子分组的设备'
})

// 角色变化处理
const handleRoleChange = () => {
  if (isSuperAdminRole.value) {
    // 超级管理员默认归属“默认分组”（groupId = 0）
    userForm.groupId = 0
  }
}

// 打开添加对话框
const openAddDialog = async () => {
  dialogTitle.value = '创建用户'
  isEditMode.value = false
  
  // 先重置表单
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    password: '',
    groupId: null,
    roleId: null,
    status: 1
  })
  
  dialogVisible.value = true
  
  // 等待对话框渲染完成后，如果当前选中了某个分组，默认选中
  await nextTick()
  if (currentGroupId.value !== 'all') {
    userForm.groupId = currentGroupId.value
  }
  userFormRef.value?.clearValidate()
}

// 编辑用户
const editUser = (user) => {
  dialogTitle.value = '编辑用户'
  isEditMode.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    groupId: user.groupId,
    roleId: user.roleId,
    status: user.status
  })
  dialogVisible.value = true
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const action = user.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `确定要${action}用户“${user.realName}”吗？`,
    `${action}确认`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await updateUserStatus({ id: user.id, status: newStatus })
    user.status = newStatus
    ElMessage.success(`已${action}用户`)
  } catch (error) {
    ElMessage.error(error.message || `${action}失败`)
  }
}

// 删除用户
const deleteUser = async (user) => {
  await ElMessageBox.confirm(
    `确定要删除用户“${user.realName}”吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  try {
    await apiDeleteUser(user.id)
    await loadUsers()
    ElMessage.success('用户删除成功')
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 保存用户
const saveUser = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          // 编辑模式：更新用户
          await updateUser({
            id: userForm.id,
            realName: userForm.realName,
            groupId: userForm.groupId,
            roleId: userForm.roleId,
            status: userForm.status
          })
          ElMessage.success('用户更新成功')
        } else {
          // 添加模式：新增用户
          await createUser({
            username: userForm.username,
            password: userForm.password,
            realName: userForm.realName,
            groupId: userForm.groupId,
            roleId: userForm.roleId,
            status: userForm.status
          })
          ElMessage.success('用户创建成功')
        }
        dialogVisible.value = false
        await loadUsers()
      } catch (error) {
        ElMessage.error(error.message || '操作失败')
      }
    } else {
      // 验证失败，打印详细错误
      console.log('表单验证失败:', userForm)
      console.log('验证错误字段:', fields)
      
      // 找到第一个错误字段并提示
      const firstError = Object.values(fields)[0]
      if (firstError && firstError[0]) {
        ElMessage.error(firstError[0].message)
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(userForm, {
    id: null,
    username: '',
    realName: '',
    password: '',
    groupId: null,
    roleId: null,
    status: 1
  })
  userFormRef.value?.clearValidate()
}

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true
    const res = await getUserList({
      page: 1,
      pageSize: 1000
    })
    // 直接使用后端返回的数据，不做任何逼辑处理
    users.value = res.list || []
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const res = await getGroupList()
    groups.value = (res.list || []).map(item => ({
      id: item.id,
      name: item.name,
      parentId: item.parentId || 0,
      path: item.path,
      level: item.level
    }))
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

onMounted(() => {
  loadGroups()
  loadUsers()
})
</script>

<style scoped>
.user-management-page {
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding: 0 20px;
}

.user-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  padding: 0 20px;
  height: calc(100vh - 160px);
}

.tree-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.user-panel {
  background: white;
  border-radius: 12px;
  border: 0.5px solid rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
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

.tree-item:hover {
  background: #f5f7fa;
}

.tree-item.active {
  background: #409eff;
  color: white;
}

.tree-item.child {
  margin-left: 0;
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
  margin-left: 24px;
}

.tree-item-content:hover {
  background: #f5f7fa;
}

.tree-item-content.active {
  background: #409eff;
  color: white;
}

.user-count {
  font-size: 13px;
  color: #86868b;
  font-weight: 400;
}

.tree-item.active .user-count,
.tree-item-content.active .user-count {
  color: rgba(255, 255, 255, 0.8);
}

.tree-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.form-hint {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
}

.form-hint.hint-super {
  color: #409eff;
  font-weight: 500;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e5e7;
  background: #fafafa;
  min-height: 56px;
  display: flex;
  align-items: center;
}

:deep(.el-card__body) {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-button--small) {
  font-size: 12px;
  padding: 4px 8px;
}
</style>
