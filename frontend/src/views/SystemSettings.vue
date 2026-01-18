<template>
  <div class="settings-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-wrapper">
          <el-icon :size="24" class="header-icon">
            <Setting />
          </el-icon>
        </div>
        <div class="header-content">
          <h1 class="page-title">系统设置</h1>
          <p class="page-subtitle">设备连接与平台MQTT配置管理</p>
        </div>
      </div>
      <div class="header-badge">
        <el-icon :size="16" class="badge-icon">
          <Lock />
        </el-icon>
        <span class="badge-text">仅管理员可访问</span>
      </div>
    </div>

    <!-- 设备连接MQTT配置 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon-wrapper card-icon-info">
              <el-icon :size="20" class="card-icon">
                <Connection />
              </el-icon>
            </div>
            <div>
              <h3 class="card-title">设备连接MQTT配置</h3>
              <p class="card-subtitle">设备端连接参数（仅用于显示）</p>
            </div>
          </div>
          <el-tag type="info" size="small" class="status-tag">
            仅用于显示
          </el-tag>
        </div>
      </template>

      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="deviceRules"
        label-width="100px"
        label-position="left"
        class="config-form"
      >
        <div class="form-grid">
          <el-form-item label="MQTT地址" prop="mqttBroker" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="deviceForm.mqttBroker"
                placeholder="例如: 192.168.1.100"
                maxlength="255"
                class="form-input"
              >
                <template #prepend>
                  <span class="input-prepend">tcp://</span>
                </template>
              </el-input>
              <span class="form-hint-inline">设备连接的MQTT服务器地址（仅用于在设备详情页显示）</span>
            </div>
          </el-form-item>

          <el-form-item label="MQTT端口" prop="mqttPort" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input-number
                v-model="deviceForm.mqttPort"
                :min="1"
                :max="65535"
                placeholder="例如: 1883"
                controls-position="right"
                class="form-input-number"
              />
              <span class="form-hint-inline">MQTT服务器端口号（仅用于在设备详情页显示）</span>
            </div>
          </el-form-item>

          <el-form-item label="用户名" prop="mqttUsername" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="deviceForm.mqttUsername"
                placeholder="MQTT连接用户名"
                maxlength="50"
                class="form-input"
              />
              <span class="form-hint-inline">设备连接时使用的用户名（仅用于在设备详情页显示）</span>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="mqttPassword" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="deviceForm.mqttPassword"
                type="password"
                show-password
                placeholder="MQTT连接密码"
                maxlength="50"
                class="form-input"
              />
              <span class="form-hint-inline">设备连接时使用的密码（仅用于在设备详情页显示）</span>
            </div>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button
            type="primary"
            @click="handleSaveDevice"
            :loading="saving"
            class="action-button action-button-primary"
          >
            <el-icon class="button-icon"><Check /></el-icon>
            <span>保存配置</span>
          </el-button>
          <el-button
            @click="handleResetDevice"
            class="action-button action-button-secondary"
          >
            <el-icon class="button-icon"><RefreshLeft /></el-icon>
            <span>重置为默认</span>
          </el-button>
        </div>
      </el-form>

      <div class="config-note">
        <div class="note-header">
          <el-icon :size="16" class="note-icon"><InfoFilled /></el-icon>
          <strong class="note-title">配置说明</strong>
        </div>
        <ul class="note-list">
          <li>此配置为设备连接MQTT服务器时使用的连接信息，仅用于在设备详情页显示</li>
          <li>配置信息保存到数据库，用户根据此配置在设备端设置MQTT连接参数</li>
          <li>部署到公网时，请将MQTT地址修改为公网IP或域名，确保防火墙已开放MQTT端口（默认1883）</li>
        </ul>
      </div>
    </el-card>

    <!-- 平台连接MQTT配置 -->
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <div class="card-icon-wrapper card-icon-success">
              <el-icon :size="20" class="card-icon">
                <Monitor />
              </el-icon>
            </div>
            <div>
              <h3 class="card-title">平台连接MQTT配置</h3>
              <p class="card-subtitle">平台订阅设备数据连接参数</p>
            </div>
          </div>
          <el-tag type="success" size="small" class="status-tag">
            支持动态重新连接
          </el-tag>
        </div>
      </template>

      <el-form
        ref="platformFormRef"
        :model="platformForm"
        :rules="platformRules"
        label-width="100px"
        label-position="left"
        class="config-form"
      >
        <div class="form-grid">
          <el-form-item label="MQTT地址" prop="mqttBroker" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="platformForm.mqttBroker"
                placeholder="例如: 192.168.1.100"
                maxlength="255"
                class="form-input"
              >
                <template #prepend>
                  <span class="input-prepend">tcp://</span>
                </template>
              </el-input>
              <span class="form-hint-inline">平台连接的MQTT服务器地址，用于订阅设备上报数据</span>
            </div>
          </el-form-item>

          <el-form-item label="MQTT端口" prop="mqttPort" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input-number
                v-model="platformForm.mqttPort"
                :min="1"
                :max="65535"
                placeholder="例如: 1883"
                controls-position="right"
                class="form-input-number"
              />
              <span class="form-hint-inline">MQTT服务器端口号，用于订阅设备上报数据</span>
            </div>
          </el-form-item>

          <el-form-item label="Client ID" prop="mqttClientId" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="platformForm.mqttClientId"
                placeholder="例如: iot-platform-server"
                maxlength="100"
                class="form-input"
              />
              <span class="form-hint-inline">平台连接MQTT时使用的客户端ID</span>
            </div>
          </el-form-item>

          <el-form-item label="用户名" prop="mqttUsername" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="platformForm.mqttUsername"
                placeholder="MQTT连接用户名"
                maxlength="50"
                class="form-input"
              />
              <span class="form-hint-inline">平台连接时使用的用户名，用于订阅设备上报数据</span>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="mqttPassword" class="form-item form-item-inline">
            <div class="form-item-content">
              <el-input
                v-model="platformForm.mqttPassword"
                type="password"
                show-password
                placeholder="MQTT连接密码"
                maxlength="50"
                class="form-input"
              />
              <span class="form-hint-inline">平台连接时使用的密码，用于订阅设备上报数据</span>
            </div>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button
            type="primary"
            @click="handleSavePlatform"
            :loading="saving"
            class="action-button action-button-primary"
          >
            <el-icon class="button-icon"><Check /></el-icon>
            <span>保存配置</span>
          </el-button>
          <el-button
            @click="handleResetPlatform"
            class="action-button action-button-secondary"
          >
            <el-icon class="button-icon"><RefreshLeft /></el-icon>
            <span>重置为默认</span>
          </el-button>
        </div>
      </el-form>

      <div class="config-note config-note-success">
        <div class="note-header">
          <el-icon :size="16" class="note-icon"><InfoFilled /></el-icon>
          <strong class="note-title">配置说明</strong>
        </div>
        <ul class="note-list">
          <li>此配置为平台连接MQTT服务器时使用的连接信息，持久化保存到数据库</li>
          <li>保存配置后，MQTT连接会自动使用新配置重新连接，无需重启后端</li>
          <li>部署到公网时，请将MQTT地址修改为公网IP或域名，确保防火墙已开放MQTT端口（默认1883）</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Lock, Check, RefreshLeft, Connection, Monitor, InfoFilled } from '@element-plus/icons-vue'
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

onMounted(() => {
  loadSystemConfig()
})
</script>

<style scoped>
.settings-container {
  background: #f5f5f7;
  min-height: calc(100vh - 60px);
  padding: 20px 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text',
               'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  line-height: 1.6;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.header-icon {
  color: #ffffff;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.3px;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #86868b;
  font-weight: 400;
}

.header-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff3e0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #e65100;
}

.badge-icon {
  color: #e65100;
}

.badge-text {
  color: #e65100;
}

/* 配置卡片 */
.config-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e5e5e7;
  overflow: hidden;
}

.config-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  flex-shrink: 0;
}

.card-icon-info {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.card-icon-success {
  background: linear-gradient(135deg, rgba(52, 199, 89, 0.1) 0%, rgba(48, 209, 88, 0.1) 100%);
}

.card-icon {
  color: #667eea;
}

.card-icon-success .card-icon {
  color: #34c759;
}

.card-title {
  margin: 0 0 2px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.2px;
}

.card-subtitle {
  margin: 0;
  font-size: 12px;
  color: #86868b;
  font-weight: 400;
}

.status-tag {
  font-size: 12px;
  border-radius: 6px;
  padding: 4px 10px;
  font-weight: 500;
  cursor: default;
}

/* 表单样式 */
.config-form {
  padding: 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px 24px;
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 16px;
}

.form-grid .form-item {
  margin-bottom: 0;
}

/* 表单项样式 - 标签和输入框同一行 */
:deep(.el-form-item) {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  padding: 0 12px 0 0;
  line-height: 40px;
  text-align: right;
  flex-shrink: 0;
  width: 100px;
}

:deep(.el-form-item__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  line-height: 1.5;
}

.form-item-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.form-hint-inline {
  font-size: 12px;
  color: #86868b;
  line-height: 1.4;
  font-weight: 400;
  margin-top: 0;
}

.form-input,
.form-input-number {
  width: 100%;
}


.input-prepend {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  padding-top: 8px;
  margin-top: 8px;
}

.action-button {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.action-button-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
}

.action-button-primary:hover {
  background: linear-gradient(135deg, #5568d3 0%, #6a3d8f 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.35);
}

.action-button-secondary {
  background: #ffffff;
  border: 1px solid #d2d2d7;
  color: #1d1d1f;
}

.action-button-secondary:hover {
  background: #fafafa;
  border-color: #86868b;
  transform: translateY(-1px);
}

.button-icon {
  font-size: 16px;
}

/* 配置说明 */
.config-note {
  margin-top: 20px;
  padding: 16px 18px;
  background: #f9fafb;
  border: 1px solid #e5e5e7;
  border-radius: 10px;
}

.config-note-success {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.note-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.note-icon {
  color: #667eea;
}

.config-note-success .note-icon {
  color: #34c759;
}

.note-title {
  font-size: 13px;
  font-weight: 600;
  color: #1d1d1f;
}

.note-list {
  margin: 0;
  padding-left: 20px;
  list-style-type: decimal;
}

.note-list li {
  font-size: 12px;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 6px;
}

.note-list li:last-child {
  margin-bottom: 0;
}

/* Element Plus 样式覆盖 */
:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e5e7;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.04) inset;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  background: #ffffff;
  min-height: 40px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #86868b;
  box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.2) inset;
}

:deep(.el-input.is-focus .el-input__wrapper),
:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset,
              0 0 0 4px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.5;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #d2d2d7;
}

:deep(.el-input-number .el-input__wrapper:hover) {
  border-color: #86868b;
}

:deep(.el-input-number.is-focus .el-input__wrapper) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) inset,
              0 0 0 4px rgba(102, 126, 234, 0.1);
}

:deep(.el-button) {
  border-radius: 8px;
}

:deep(.el-tag) {
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: none;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
