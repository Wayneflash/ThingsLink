<template>
  <div class="change-password-page">
    <div class="page-header">
      <h2 class="page-title">🔐 修改密码</h2>
    </div>

    <div class="content-container">
      <el-card class="password-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">密码修改</span>
          </div>
        </template>

        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-width="120px"
          class="password-form"
          autocomplete="off"
        >
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码（至少6位）"
              maxlength="50"
              clearable
              autocomplete="new-password"
            />
            <div class="form-tip">密码长度至少6位，建议使用字母、数字和特殊字符组合</div>
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
              maxlength="50"
              clearable
              autocomplete="new-password"
            />
            <div class="form-tip">请再次输入新密码以确认</div>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button type="primary" @click="handleSave" :loading="saving" size="large">
                <el-icon style="margin-right: 6px;"><Check /></el-icon>
                保存修改
              </el-button>
              <el-button @click="handleReset" size="large">
                <el-icon style="margin-right: 6px;"><RefreshLeft /></el-icon>
                重置
              </el-button>
            </div>
          </el-form-item>
        </el-form>

        <!-- 安全提示 -->
        <el-alert
          type="warning"
          :closable="false"
          class="security-alert"
        >
          <template #title>
            <strong>安全提示</strong>
          </template>
          <div class="alert-content">
            <p>1. 密码修改成功后，请使用新密码重新登录</p>
            <p>2. 建议定期更换密码，提高账户安全性</p>
            <p>3. 不要使用过于简单的密码（如123456、password等）</p>
            <p>4. 密码修改后，请妥善保管，避免泄露</p>
          </div>
        </el-alert>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, RefreshLeft } from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const saving = ref(false)

// 表单数据
const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 保存修改
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    // 调用API（响应拦截器已处理错误，成功时返回data部分）
    await changePassword({
      newPassword: form.newPassword
    })
    
    // 如果执行到这里，说明修改成功（错误会被catch捕获）
    ElMessage.success('密码修改成功，请使用新密码重新登录')
    
    // 询问是否立即退出登录
    ElMessageBox.confirm(
      '密码修改成功！为了安全起见，建议您重新登录。是否立即退出登录？',
      '提示',
      {
        confirmButtonText: '退出登录',
        cancelButtonText: '稍后退出',
        type: 'success'
      }
    ).then(() => {
      // 清除登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('menus')
      
      // 跳转到登录页
      router.push('/login')
    }).catch(() => {
      // 用户选择稍后退出，重置表单
      handleReset()
    })
  } catch (error) {
    if (error.message && error.message.includes('validate')) {
      // 表单验证失败，不显示错误消息
      return
    }
    console.error('修改密码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '密码修改失败')
  } finally {
    saving.value = false
  }
}

// 重置表单
const handleReset = () => {
  form.newPassword = ''
  form.confirmPassword = ''
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.change-password-page {
  padding: 24px;
  background: #f5f5f7;
  min-height: calc(100vh - 60px);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 24px;
}

.page-header .page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
  letter-spacing: -0.5px;
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
}

.password-card {
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: none;
}

.password-card :deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;
}

.password-card :deep(.el-card__body) {
  padding: 32px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header .card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.password-form {
  margin-bottom: 24px;
}

.password-form :deep(.el-form-item) {
  margin-bottom: 28px;
}

.password-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1d1d1f;
  font-size: 14px;
}

.password-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d2d2d7 inset;
  transition: all 0.2s;
}

.password-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #86868b inset;
}

.password-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #007aff inset;
}

.password-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #1d1d1f;
}

.form-tip {
  font-size: 12px;
  color: #86868b;
  margin-top: 6px;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  padding: 12px 24px;
  font-size: 14px;
}

.form-actions :deep(.el-button.el-button--primary) {
  background: #007aff;
  border-color: #007aff;
}

.form-actions :deep(.el-button.el-button--primary:hover) {
  background: #0051d5;
  border-color: #0051d5;
}

.security-alert {
  margin-top: 32px;
  border-radius: 10px;
  border: none;
}

.security-alert :deep(.el-alert__content) {
  width: 100%;
}

.security-alert :deep(.el-alert__title) {
  font-size: 14px;
  color: #1d1d1f;
  margin-bottom: 8px;
}

.alert-content {
  font-size: 13px;
  color: #515154;
  line-height: 1.8;
}

.alert-content p {
  margin: 4px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .change-password-page {
    padding: 16px;
  }

  .password-card :deep(.el-card__body) {
    padding: 24px;
  }

  .password-form :deep(.el-form-item__label) {
    width: 100px !important;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
