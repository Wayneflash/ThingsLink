<template>
  <div class="profile-page">
    <div class="page-header">
      <h2 class="page-title">👤 个人资料</h2>
    </div>

    <div class="content-container">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">基本信息</span>
          </div>
        </template>

        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="用户名">
            <el-input v-model="form.username" disabled />
            <div class="form-tip">用户名不可修改</div>
          </el-form-item>

          <el-form-item label="姓名" prop="realName">
            <el-input 
              v-model="form.realName" 
              placeholder="请输入姓名"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input 
              v-model="form.phone" 
              placeholder="请输入手机号"
              maxlength="11"
              show-word-limit
            />
            <div class="form-tip">用于接收短信通知</div>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input 
              v-model="form.email" 
              placeholder="请输入邮箱地址"
              maxlength="100"
              show-word-limit
            />
            <div class="form-tip">用于接收邮件通知</div>
          </el-form-item>

          <el-form-item label="所属分组">
            <el-input v-model="form.groupName" disabled />
          </el-form-item>

          <el-form-item label="角色">
            <el-input v-model="form.roleName" disabled />
          </el-form-item>

          <el-form-item label="状态">
            <el-tag :type="form.status === 1 ? 'success' : 'danger'">
              {{ form.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-form-item>

          <el-form-item label="创建时间">
            <span class="form-text">{{ formatTime(form.createTime) }}</span>
          </el-form-item>

          <el-form-item label="更新时间">
            <span class="form-text">{{ formatTime(form.updateTime) }}</span>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存修改
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/profile'

const formRef = ref(null)
const saving = ref(false)

// 表单数据
const form = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  groupId: null,
  groupName: '',
  roleId: null,
  roleName: '',
  status: 1,
  createTime: null,
  updateTime: null,
  isSuper: false
})

// 原始数据备份
const originalForm = ref({})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { 
      validator: (rule, value, callback) => {
        if (value && value.trim()) {
          const phoneReg = /^1[3-9]\d{9}$/
          if (!phoneReg.test(value)) {
            callback(new Error('请输入正确的手机号码'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { 
      validator: (rule, value, callback) => {
        if (value && value.trim()) {
          const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
          if (!emailReg.test(value)) {
            callback(new Error('请输入正确的邮箱地址'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载个人资料
const loadProfile = async () => {
  try {
    const res = await getProfile()
    if (res) {
      Object.assign(form, res)
      // 备份原始数据
      originalForm.value = JSON.parse(JSON.stringify(res))
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
    ElMessage.error('加载个人资料失败')
  }
}

// 保存修改
const handleSave = async () => {
  try {
    // 验证表单
    await formRef.value.validate()
    
    saving.value = true
    
    const data = {
      realName: form.realName,
      phone: form.phone,
      email: form.email
    }
    
    await updateProfile(data)
    
    ElMessage.success('个人资料更新成功')
    
    // 重新加载数据
    await loadProfile()
  } catch (error) {
    if (error !== false) { // 表单验证失败时 error 为 false
      console.error('更新个人资料失败:', error)
      const errorMsg = error?.response?.data?.message || error?.message || '更新失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    saving.value = false
  }
}

// 重置表单
const handleReset = () => {
  Object.assign(form, originalForm.value)
  formRef.value.clearValidate()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 页面加载时获取数据
onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  background: #f5f5f7;
  min-height: 100vh;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.profile-form {
  padding: 10px 0;
}

.form-tip {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
}

.form-text {
  color: #606266;
  font-size: 14px;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e7;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1d1d1f;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: none;
}

:deep(.el-input__wrapper:hover) {
  border-color: #86868b;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f5f5f7;
  border-color: #e5e5e7;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

:deep(.el-button--default) {
  border: 1px solid #d2d2d7;
  color: #1d1d1f;
  background: white;
}

:deep(.el-button--default:hover) {
  background: #fafafa;
  border-color: #86868b;
}

:deep(.el-button--primary) {
  background: #007AFF;
  border-color: #007AFF;
}

:deep(.el-button--primary:hover) {
  background: #0051D5;
  border-color: #0051D5;
}
</style>
