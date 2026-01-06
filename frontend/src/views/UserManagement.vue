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
        
        <!-- 全部用户按钮 -->
        <div 
          class="all-users-btn"
          :class="{ active: currentGroupId === null }"
          @click="selectAllUsers"
        >
          <div class="btn-left">
            <el-icon><User /></el-icon>
            <span>全部用户</span>
          </div>
          <span class="user-count">{{ totalUserCount }}</span>
        </div>
        
        <!-- 分组树 -->
        <GroupTree 
          :groups="groups" 
          :current-group-id="currentGroupId"
          :show-actions="false"
          :show-count="false"
          @select="selectGroup"
        />
        
        <div class="tree-hint">
          💡 点击分组查看该分组下的用户
        </div>
      </el-card>

      <!-- 右侧用户列表 -->
      <el-card class="user-panel" shadow="hover">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 20px;">
            <h3 style="margin: 0; font-size: 16px;">{{ currentGroupTitle }}</h3>
            <div style="display: flex; gap: 16px;">
              <el-input
                v-model="searchQuery"
                placeholder="🔍 搜索用户名或姓名"
                clearable
                style="width: 240px;"
                @input="handleSearch"
              />
              <el-button 
                v-if="isCurrentUserSuperAdmin" 
                type="primary" 
                size="default" 
                @click="openAddDialog"
              >
                <el-icon><Plus /></el-icon>
                创建用户
              </el-button>
              <el-tooltip v-else content="只有超级管理员才能创建用户" placement="top">
                <el-button type="primary" size="default" disabled>
                  <el-icon><Plus /></el-icon>
                  创建用户
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
        
        <el-table :data="filteredUsers" stripe v-loading="loading">
          <el-table-column type="index" label="序号" width="80" :index="indexMethod" />
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
              <template v-else-if="!isCurrentUserSuperAdmin">
                <el-tooltip content="只有超级管理员才能管理用户" placement="top">
                  <el-button size="small" text disabled style="opacity: 0.5;">编辑</el-button>
                </el-tooltip>
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
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      @opened="handleDialogOpened"
    >
      <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="userForm.username" 
            placeholder="如：zhangsan、lisi" 
            :disabled="isEditMode"
            autocomplete="off"
          />
          <div class="form-hint">💡 用于登录的账号，建议使用英文，创建后不可修改</div>
        </el-form-item>
        
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            autocomplete="new-password"
          />
          <div class="form-hint">💡 密码将采用 BCrypt 加密存储，建议 6 位以上</div>
        </el-form-item>
        
        <el-form-item label="姓名" prop="realName">
          <el-input 
            v-model.trim="userForm.realName" 
            placeholder="如：张三、李四"
            autocomplete="off"
            clearable
          />
          <div class="form-hint">💡 用户的真实姓名，用于显示和识别</div>
        </el-form-item>
        
        <el-form-item label="所属分组" prop="groupId">
          <GroupSelector
            ref="groupSelectorRef"
            v-model="userForm.groupId"
            :disabled="isSuperAdminRole"
          />
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
import { Plus, User } from '@element-plus/icons-vue'
import { getGroupTree } from '@/api/group'
import { getUserList, createUser, updateUser, deleteUser as apiDeleteUser, updateUserStatus } from '@/api/user'
import { getRoleList } from '@/api/role'
import GroupTree from '@/components/GroupTree.vue'
import GroupSelector from '@/components/GroupSelector.vue'
import { flattenTree } from '@/utils/tree'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('创建用户')
const isEditMode = ref(false)
const userFormRef = ref(null)
const groupSelectorRef = ref(null) // 分组选择器引用
const searchQuery = ref('')
const currentGroupId = ref(null)
const showAllGroup = ref(false) // 总分组展开状态

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

// 用户列表
const users = ref([])

// 总用户数（用于显示“全部用户”按钮的数量）
const totalUserCount = ref(0)

// 角色列表（从后端加载）
const roles = ref([])

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
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '姓名不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  groupId: [{ required: true, message: '请选择所属分组', trigger: 'change' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 当前分组标题
const currentGroupTitle = computed(() => {
  if (!currentGroupId.value) {
    // 显示全部用户
    const count = filteredUsers.value.length
    return `全部用户 (${count}人)`
  }
  const group = groups.value.find(g => g.id === currentGroupId.value)
  if (group) {
    // 显示当前列表的用户数量（filteredUsers的数量）
    const count = filteredUsers.value.length
    return `${group.name} (${count}人)`
  }
  return '请选择分组'
})

// 筛选后的用户列表
const filteredUsers = computed(() => {
  let result = users.value
  
  // 搜索过滤
  if (searchQuery.value) {
    result = result.filter(u =>
      u.username.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      u.realName.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  }
  
  // 添加分组名称
  const mappedResult = result.map(u => {
    const group = groups.value.find(g => g.id === u.groupId)
    const groupName = group ? group.name : '未知分组'
    return {
      ...u,
      groupName
    }
  })
  
  // 排序：admin永远在第一位，其他用户按创建时间倒序
  return mappedResult.sort((a, b) => {
    // admin 用户永远排在第一位
    if (a.username === 'admin') return -1
    if (b.username === 'admin') return 1
    
    // 其他用户按创建时间倒序排列（最新的在最前面）
    const timeA = new Date(a.createTime || 0).getTime()
    const timeB = new Date(b.createTime || 0).getTime()
    return timeB - timeA  // 倒序
  })
})

// 序号计算方法
const indexMethod = (index) => {
  return index + 1
}

// 选择全部用户
const selectAllUsers = () => {
  currentGroupId.value = null
  console.log('选中全部用户')
  loadUsers()
}

// 选择分组
const selectGroup = (id) => {
  currentGroupId.value = id
  console.log('选中分组:', id)
  // 清空搜索条件
  searchQuery.value = ''
  // 重新加载用户列表，传递分组ID参数
  loadUsers()
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
    // 超级管理员默认归属总分组（动态获取parentId=0的分组）
    const rootGroup = groups.value.find(g => g.parentId === 0 || g.parentId === null)
    userForm.groupId = rootGroup ? rootGroup.id : null
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '创建用户'
  isEditMode.value = false
  
  // 完全重置表单
  userForm.id = null
  userForm.username = ''
  userForm.realName = ''
  userForm.password = ''
  // 默认选中当前分组，如果没有则默认总分组（动态获取）
  if (currentGroupId.value) {
    userForm.groupId = currentGroupId.value
  } else {
    const rootGroup = groups.value.find(g => g.parentId === 0 || g.parentId === null)
    userForm.groupId = rootGroup ? rootGroup.id : null
  }
  userForm.roleId = null
  userForm.status = 1
  
  // 打开对话框
  dialogVisible.value = true
}

// 打开对话框时的处理
const handleDialogOpened = async () => {
  await nextTick()
  // 确保分组数据已加载
  if (groupSelectorRef.value && groupSelectorRef.value.groupsData.length === 0) {
    await groupSelectorRef.value.loadGroups()
  }
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
  
  // 打印当前表单数据
  console.log('当前表单数据:', JSON.parse(JSON.stringify(userForm)))
  
  try {
    // 手动验证表单
    const valid = await userFormRef.value.validate()
    
    console.log('验证结果:', valid)
    
    if (valid) {
      if (isEditMode.value) {
        // 编辑模式：更新用户
        await updateUser({
          id: userForm.id,
          realname: userForm.realName,  // 后端字段名是realname
          groupId: userForm.groupId,
          roleId: userForm.roleId,
          status: userForm.status
        })
        dialogVisible.value = false
        await loadUsers()  // 先关闭对话框，再刷新列表
        ElMessage.success('用户更新成功')
      } else {
        // 添加模式：新增用户
        await createUser({
          username: userForm.username,
          password: userForm.password,
          realname: userForm.realName,  // 后端字段名是realname，不是realName
          groupId: userForm.groupId,
          roleId: userForm.roleId,
          status: userForm.status
        })
        dialogVisible.value = false
        await loadUsers()  // 先关闭对话框，再刷新列表
        ElMessage.success('用户创建成功')
      }
    }
  } catch (error) {
    console.error('验证或提交错误:', error)
    
    // 如果是验证错误
    if (error?.message) {
      ElMessage.error(error.message)
    } else if (typeof error === 'object') {
      // 找到第一个验证错误
      const firstErrorField = Object.keys(error)[0]
      console.log('错误字段:', firstErrorField, error[firstErrorField])
      
      if (firstErrorField && error[firstErrorField]) {
        const firstError = error[firstErrorField][0]
        if (firstError?.message) {
          ElMessage.error(firstError.message)
        }
      }
    } else {
      ElMessage.error('操作失败')
    }
  }
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
    console.log('loadUsers 被调用, currentGroupId:', currentGroupId.value)
    
    // 传递分组ID参数
    const params = {
      page: 1,
      pageSize: 1000
    }
    // 如果选中了分组，传递groupId参数
    if (currentGroupId.value) {
      params.groupId = currentGroupId.value
      console.log('查询指定分组的用户, groupId:', currentGroupId.value)
    } else {
      console.log('查询全部用户')
    }
    
    const res = await getUserList(params)
    console.log('用户列表响应:', res)
    
    // 直接使用后端返回的数据，不做任何逻辑处理
    users.value = res.list || []
    console.log('用户数量:', users.value.length)
    
    // 更新总用户数（只在查询全部用户时更新）
    if (!currentGroupId.value) {
      totalUserCount.value = res.total || users.value.length
    }
  } catch (error) {
    console.error('加载用户失败:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

// 加载分组列表
const loadGroups = async () => {
  try {
    const res = await getGroupTree()
    // 后端返回的是树形结构 {tree: [...]}
    // 需要扁平化树形数据为列表
    groups.value = flattenTree(res.tree || [])
    
    // 默认选中第一个分组
    if (groups.value.length > 0 && !currentGroupId.value) {
      currentGroupId.value = groups.value[0].id
    }
    
    // 加载一次全部用户数量
    await loadTotalUserCount()
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

// 使用统一的工具函数扁平化树形数据（已在顶部import）

// 加载总用户数量
const loadTotalUserCount = async () => {
  try {
    const res = await getUserList({ page: 1, pageSize: 1 })
    totalUserCount.value = res.total || 0
  } catch (error) {
    console.error('加载总用户数失败:', error)
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 })
    roles.value = (res.list || []).map(item => ({
      id: item.id,
      name: item.name,
      isSuperAdmin: item.roleCode === 'super_admin' || item.code === 'super_admin'
    }))
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

onMounted(async () => {
  // 加载当前用户信息
  const userInfoData = localStorage.getItem('userInfo')
  if (userInfoData) {
    currentUser.value = JSON.parse(userInfoData)
  }
  
  await loadRoles()  // 先加载角色列表
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

.tree-item.parent-item {
  font-weight: 500;
}

.tree-children {
  margin-left: 12px;
  border-left: 2px solid #e5e5e7;
  padding-left: 8px;
  margin-top: 4px;
}

.tree-group {
  margin-bottom: 4px;
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

/* 全部用户按钮样式 */
.all-users-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  margin-bottom: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #303133;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  user-select: none;
}

.all-users-btn:hover {
  background: #e0f2fe;
  border-color: #93c5fd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.all-users-btn.active {
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%);
  color: white;
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.all-users-btn .btn-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.all-users-btn .user-count {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 8px;
  border-radius: 12px;
}

.all-users-btn.active .user-count {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
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
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e7;
  background: #fafafa;
  min-height: 64px;
  display: flex;
  align-items: center;
}

:deep(.el-card__body) {
  padding: 24px;
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
