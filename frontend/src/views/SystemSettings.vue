<template>
  <div class="system-settings-page">
    <div class="page-header">
      <h2 class="page-title">⚙️ 系统设置</h2>
      <div class="admin-badge">
        <el-icon><Lock /></el-icon>
        <span>仅管理员可访问</span>
      </div>
    </div>

    <!-- 设备连接MQTT配置（可编辑） -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">设备连接MQTT配置</span>
          <el-tag type="info" size="small">设备连接信息（仅用于显示）</el-tag>
        </div>
      </template>

      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="deviceRules"
        label-width="120px"
        class="settings-form"
      >
        <el-form-item label="MQTT地址" prop="mqttBroker">
          <el-input
            v-model="deviceForm.mqttBroker"
            placeholder="例如: 192.168.1.100 或 mqtt.example.com"
            maxlength="255"
          >
            <template #prepend>tcp://</template>
          </el-input>
          <div class="form-tip">设备连接的MQTT服务器地址（仅用于在设备详情页显示）</div>
        </el-form-item>

        <el-form-item label="MQTT端口" prop="mqttPort">
          <el-input-number
            v-model="deviceForm.mqttPort"
            :min="1"
            :max="65535"
            placeholder="例如: 1883"
            style="width: 100%;"
          />
          <div class="form-tip">MQTT服务器端口号（仅用于在设备详情页显示）</div>
        </el-form-item>

        <el-form-item label="用户名" prop="mqttUsername">
          <el-input
            v-model="deviceForm.mqttUsername"
            placeholder="MQTT连接用户名"
            maxlength="50"
          />
          <div class="form-tip">设备连接时使用的用户名（仅用于在设备详情页显示）</div>
        </el-form-item>

        <el-form-item label="密码" prop="mqttPassword">
          <el-input
            v-model="deviceForm.mqttPassword"
            type="password"
            show-password
            placeholder="MQTT连接密码"
            maxlength="50"
          />
          <div class="form-tip">设备连接时使用的密码（仅用于在设备详情页显示）</div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="handleSaveDevice" :loading="saving" size="large">
              <el-icon style="margin-right: 6px;"><Check /></el-icon>
              保存配置
            </el-button>
            <el-button @click="handleResetDevice" size="large">
              <el-icon style="margin-right: 6px;"><RefreshLeft /></el-icon>
              重置为默认
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 配置说明 -->
      <el-alert
        type="info"
        :closable="false"
        class="config-alert"
      >
        <template #title>
          <strong>配置说明</strong>
        </template>
        <div class="alert-content">
          <p>1. 此配置为<strong>设备连接MQTT服务器</strong>时使用的连接信息</p>
          <p>2. 配置信息保存到数据库，仅用于在设备详情页显示给用户查看</p>
          <p>3. 用户根据此配置在设备端设置MQTT连接参数</p>
          <p>4. 部署到公网时，请将MQTT地址修改为公网IP或域名</p>
          <p>5. 确保防火墙已开放MQTT端口（默认1883）</p>
        </div>
      </el-alert>

      <!-- 当前配置预览 -->
      <div class="config-preview">
        <div class="preview-title">当前配置预览</div>
        <div class="preview-content">
          <div class="preview-item">
            <span class="preview-label">连接地址：</span>
            <code class="preview-value">tcp://{{ deviceForm.mqttBroker }}:{{ deviceForm.mqttPort }}</code>
          </div>
          <div class="preview-item">
            <span class="preview-label">认证信息：</span>
            <code class="preview-value">{{ deviceForm.mqttUsername }} / {{ maskPassword(deviceForm.mqttPassword) }}</code>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 平台连接MQTT配置（可编辑） -->
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">平台连接MQTT配置</span>
          <el-tag type="success" size="small">支持动态重新连接</el-tag>
        </div>
      </template>

      <el-form 
        ref="platformFormRef" 
        :model="platformForm" 
        :rules="platformRules" 
        label-width="120px"
        class="settings-form"
      >
        <el-form-item label="MQTT地址" prop="mqttBroker">
          <el-input 
            v-model="platformForm.mqttBroker" 
            placeholder="例如: 192.168.1.100 或 mqtt.example.com"
            maxlength="255"
          >
            <template #prepend>tcp://</template>
          </el-input>
          <div class="form-tip">平台连接的MQTT服务器地址，用于订阅设备上报数据</div>
        </el-form-item>

        <el-form-item label="MQTT端口" prop="mqttPort">
          <el-input-number
            v-model="platformForm.mqttPort"
            :min="1"
            :max="65535"
            placeholder="例如: 1883"
            style="width: 100%;"
          />
          <div class="form-tip">MQTT服务器端口号，用于订阅设备上报数据</div>
        </el-form-item>

        <el-form-item label="Client ID" prop="mqttClientId">
          <el-input
            v-model="platformForm.mqttClientId"
            placeholder="例如: iot-platform-server"
            maxlength="100"
          />
          <div class="form-tip">平台连接MQTT时使用的客户端ID</div>
        </el-form-item>

        <el-form-item label="用户名" prop="mqttUsername">
          <el-input 
            v-model="platformForm.mqttUsername" 
            placeholder="MQTT连接用户名"
            maxlength="50"
          />
          <div class="form-tip">平台连接时使用的用户名，用于订阅设备上报数据</div>
        </el-form-item>

        <el-form-item label="密码" prop="mqttPassword">
          <el-input 
            v-model="platformForm.mqttPassword" 
            type="password" 
            show-password
            placeholder="MQTT连接密码"
            maxlength="50"
          />
          <div class="form-tip">平台连接时使用的密码，用于订阅设备上报数据</div>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="handleSavePlatform" :loading="saving" size="large">
              <el-icon style="margin-right: 6px;"><Check /></el-icon>
              保存配置
            </el-button>
            <el-button @click="handleResetPlatform" size="large">
              <el-icon style="margin-right: 6px;"><RefreshLeft /></el-icon>
              重置为默认
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 配置说明 -->
      <el-alert
        type="success"
        :closable="false"
        class="config-alert"
      >
        <template #title>
          <strong>配置说明</strong>
        </template>
        <div class="alert-content">
          <p>1. 此配置为<strong>平台连接MQTT服务器</strong>时使用的连接信息</p>
          <p>2. 配置已<strong>持久化保存到数据库</strong>，重启后端服务后配置依然保留</p>
          <p>3. 保存配置后，MQTT连接会<strong>自动使用新配置重新连接</strong>，无需重启后端</p>
          <p>4. 部署到公网时，请将MQTT地址修改为公网IP或域名</p>
          <p>5. 确保防火墙已开放MQTT端口（默认1883）</p>
        </div>
      </el-alert>

      <!-- 当前配置预览 -->
      <div class="config-preview">
        <div class="preview-title">当前配置预览</div>
        <div class="preview-content">
          <div class="preview-item">
            <span class="preview-label">连接地址：</span>
            <code class="preview-value">tcp://{{ platformForm.mqttBroker }}:{{ platformForm.mqttPort }}</code>
          </div>
          <div class="preview-item">
            <span class="preview-label">认证信息：</span>
            <code class="preview-value">{{ platformForm.mqttUsername }} / {{ maskPassword(platformForm.mqttPassword) }}</code>
          </div>
          <div class="preview-item">
            <span class="preview-label">Client ID：</span>
            <code class="preview-value">{{ platformForm.mqttClientId }}</code>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Check, RefreshLeft } from '@element-plus/icons-vue'
import { getSystemConfig, updatePlatformMqttConfig, updateDeviceMqttConfig } from '@/api/system'

const deviceFormRef = ref(null)
const platformFormRef = ref(null)
const saving = ref(false)

// 设备连接MQTT配置（只读，从application.yml读取）
const deviceForm = reactive({
  mqttBroker: 'localhost',
  mqttPort: 1883,
  mqttUsername: 'admin',
  mqttPassword: 'admin123.'
})

// 平台连接MQTT配置（可编辑，保存到数据库）
const platformForm = reactive({
  mqttBroker: 'localhost',
  mqttPort: 1883,
  mqttClientId: 'iot-platform-server',
  mqttUsername: 'admin',
  mqttPassword: 'admin123.'
})

// 设备连接表单验证规则
const deviceRules = {
  mqttBroker: [
    { required: true, message: 'MQTT地址不能为空', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && value.trim()) {
          const addrRegex = /^([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}|(\d{1,3}\.){3}\d{1,3}$/
          if (!addrRegex.test(value.trim())) {
            callback(new Error('请输入有效的IP地址或域名'))
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
  mqttPort: [
    { required: true, message: 'MQTT端口不能为空', trigger: 'blur' },
    { type: 'number', message: '端口必须是数字', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && (value < 1 || value > 65535)) {
          callback(new Error('端口范围必须在1-65535之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  mqttUsername: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在1-50个字符', trigger: 'blur' }
  ],
  mqttPassword: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '密码长度在1-50个字符', trigger: 'blur' }
  ],
  mqttClientId: [
    { required: true, message: 'Client ID不能为空', trigger: 'blur' },
    { min: 1, max: 100, message: 'Client ID长度在1-100个字符', trigger: 'blur' }
  ]
}

// 平台连接表单验证规则
const platformRules = {
  mqttBroker: [
    { required: true, message: 'MQTT地址不能为空', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && value.trim()) {
          const addrRegex = /^([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}|(\d{1,3}\.){3}\d{1,3}$/
          if (!addrRegex.test(value.trim())) {
            callback(new Error('请输入有效的IP地址或域名'))
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
  mqttPort: [
    { required: true, message: 'MQTT端口不能为空', trigger: 'blur' },
    { type: 'number', message: '端口必须是数字', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && (value < 1 || value > 65535)) {
          callback(new Error('端口范围必须在1-65535之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  mqttUsername: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '用户名长度在1-50个字符', trigger: 'blur' }
  ],
  mqttPassword: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 1, max: 50, message: '密码长度在1-50个字符', trigger: 'blur' }
  ],
  mqttClientId: [
    { required: true, message: 'Client ID不能为空', trigger: 'blur' },
    { min: 1, max: 100, message: 'Client ID长度在1-100个字符', trigger: 'blur' }
  ]
}

// 加载系统配置
const loadSystemConfig = async () => {
  try {
    const res = await getSystemConfig()
    if (res) {
      // 设备连接MQTT配置（从application.yml读取）
      if (res.mqttConfig) {
        const mqttConfig = res.mqttConfig
        deviceForm.mqttBroker = mqttConfig.broker || 'localhost'
        deviceForm.mqttPort = mqttConfig.port || 1883
        deviceForm.mqttUsername = mqttConfig.username || 'admin'
        deviceForm.mqttPassword = mqttConfig.password || 'admin123.'
      }
      
      // 平台连接MQTT配置（从数据库读取）
      if (res.platformMqttConfig) {
        const platformMqttConfig = res.platformMqttConfig
        platformForm.mqttBroker = platformMqttConfig.broker || 'localhost'
        platformForm.mqttPort = platformMqttConfig.port || 1883
        platformForm.mqttClientId = platformMqttConfig.clientId || 'iot-platform-server'
        platformForm.mqttUsername = platformMqttConfig.username || 'admin'
        platformForm.mqttPassword = platformMqttConfig.password || 'admin123.'
      }
    }
  } catch (error) {
    console.error('加载系统配置失败:', error)
    ElMessage.error('加载系统配置失败')
  }
}

// 保存设备MQTT配置
const handleSaveDevice = async () => {
  try {
    // 验证表单
    await deviceFormRef.value.validate()
    
    await ElMessageBox.confirm(
      '确定要保存设备MQTT配置吗？此配置仅用于在设备详情页显示。',
      '保存确认',
      {
        confirmButtonText: '确定保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    saving.value = true
    
    const data = {
      deviceMqttBroker: deviceForm.mqttBroker,
      deviceMqttPort: deviceForm.mqttPort,
      deviceMqttUsername: deviceForm.mqttUsername,
      deviceMqttPassword: deviceForm.mqttPassword
    }
    
    await updateDeviceMqttConfig(data)
    
    ElMessage.success('设备MQTT配置保存成功！')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存设备MQTT配置失败:', error)
      const errorMsg = error?.response?.data?.message || error?.message || '保存失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    saving.value = false
  }
}

// 重置为默认值
const handleResetDevice = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置为默认配置吗？',
      '重置确认',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    deviceForm.mqttBroker = 'localhost'
    deviceForm.mqttPort = 1883
    deviceForm.mqttUsername = 'admin'
    deviceForm.mqttPassword = 'admin123.'
    
    deviceFormRef.value.clearValidate()
    ElMessage.success('已重置为默认配置')
  } catch {
    // 用户取消
  }
}

// 保存平台MQTT配置
const handleSavePlatform = async () => {
  try {
    // 验证表单
    await platformFormRef.value.validate()
    
    await ElMessageBox.confirm(
      '确定要保存平台MQTT配置吗？保存后MQTT连接会自动使用新配置重新连接。',
      '保存确认',
      {
        confirmButtonText: '确定保存',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    saving.value = true
    
    const data = {
      platformMqttBroker: platformForm.mqttBroker,
      platformMqttPort: platformForm.mqttPort,
      platformMqttClientId: platformForm.mqttClientId,
      platformMqttUsername: platformForm.mqttUsername,
      platformMqttPassword: platformForm.mqttPassword
    }
    
    await updatePlatformMqttConfig(data)
    
    ElMessage.success('平台MQTT配置保存成功！MQTT连接已使用新配置重新连接')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存平台MQTT配置失败:', error)
      const errorMsg = error?.response?.data?.message || error?.message || '保存失败'
      ElMessage.error(errorMsg)
    }
  } finally {
    saving.value = false
  }
}

// 重置为默认值
const handleResetPlatform = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置为默认配置吗？',
      '重置确认',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    platformForm.mqttBroker = 'localhost'
    platformForm.mqttPort = 1883
    platformForm.mqttUsername = 'admin'
    platformForm.mqttPassword = 'admin123.'
    
    platformFormRef.value.clearValidate()
    ElMessage.success('已重置为默认配置')
  } catch {
    // 用户取消
  }
}

// 密码掩码
const maskPassword = (password) => {
  if (!password) return '***'
  return '*'.repeat(Math.min(password.length, 8))
}

// 页面加载时获取数据
onMounted(() => {
  loadSystemConfig()
})
</script>

<style scoped>
.system-settings-page {
  background: #f5f5f7;
  min-height: 100vh;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  background: white;
  padding: 18px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff3e0;
  color: #e6a23c;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.settings-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
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

.settings-form {
  padding: 10px 0;
}

.form-tip {
  font-size: 12px;
  color: #86868b;
  margin-top: 4px;
  line-height: 1.5;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 20px;
}

.config-alert {
  margin: 24px 0;
  border-radius: 8px;
}

.alert-content p {
  margin: 8px 0;
  line-height: 1.6;
  font-size: 14px;
}

.alert-content strong {
  color: #409eff;
  font-weight: 600;
}

.alert-content code {
  background: #f5f5f7;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  color: #e6a23c;
}

.config-preview {
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e5e5e7;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.preview-label {
  color: #86868b;
  font-weight: 500;
  min-width: 100px;
}

.preview-value {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  color: #007AFF;
  background: white;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #d2d2d7;
  font-weight: 500;
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

:deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #007AFF;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
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
