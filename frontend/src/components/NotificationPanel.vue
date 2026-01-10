<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="400"
    trigger="click"
    popper-class="notification-popover"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-icon">
        <el-button text @click="handleClick">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
      </el-badge>
    </template>

    <div class="notification-panel">
      <!-- 头部 -->
      <div class="notification-header">
        <span class="header-title">消息通知</span>
        <div class="header-actions">
          <el-button 
            text 
            size="small" 
            @click="handleMarkAllAsRead"
            :disabled="unreadCount === 0"
          >
            全部已读
          </el-button>
          <el-button text size="small" @click="handleViewAll">查看全部</el-button>
        </div>
      </div>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all">
          <template #label>
            <span>全部 <span class="tab-count" v-if="totalCount > 0">({{ totalCount }})</span></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="未读" name="unread">
          <template #label>
            <span>未读 <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="tab-badge" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已读" name="read">
          <template #label>
            <span>已读 <span class="tab-count" v-if="readCount > 0">({{ readCount }})</span></span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 通知列表 -->
      <div class="notification-list" v-loading="loading">
        <div v-if="notificationList.length === 0" class="empty-state">
          <el-empty description="暂无通知" :image-size="80" />
        </div>
        
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ 'unread': item.isRead === 0 }"
          @click="handleNotificationClick(item)"
        >
          <div class="notification-content">
            <div class="notification-title">
              <el-tag 
                :type="getLevelType(item.alarmLevel)" 
                size="small"
                effect="plain"
              >
                {{ getLevelText(item.alarmLevel) }}
              </el-tag>
              <span class="device-name">{{ item.deviceName }}</span>
              <span class="device-code" v-if="item.deviceCode">({{ item.deviceCode }})</span>
              <el-tag 
                v-if="activeTab === 'all'"
                :type="item.isRead === 1 ? 'success' : 'warning'"
                size="small"
                effect="plain"
                class="read-status-tag"
              >
                {{ item.isRead === 1 ? '已读' : '未读' }}
              </el-tag>
            </div>
            <div class="notification-message">
              {{ item.alarmMessage }}
              <span class="message-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="notification-action">
            <el-button
              v-if="item.isRead === 0"
              text
              size="small"
              @click.stop="handleMarkAsRead(item.id)"
            >
              标记已读
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="notification-footer" v-if="totalCount > 0">
        <div class="footer-info">
          <span class="total-info">共 {{ totalCount }} 条</span>
        </div>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalCount"
          :page-sizes="[10, 20, 50]"
          layout="prev, pager, next"
          small
          @current-change="loadNotifications"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'

const router = useRouter()

// 弹窗显示状态
const visible = ref(false)

// 加载状态
const loading = ref(false)

// 当前标签页
const activeTab = ref('all')

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 通知列表
const notificationList = ref([])
const totalCount = ref(0)
const unreadCount = ref(0)
const readCount = ref(0)

// 点击通知图标
const handleClick = () => {
  if (!visible.value) {
    loadNotifications()
  }
}

// 加载通知列表
const loadNotifications = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 根据标签页筛选
    if (activeTab.value === 'unread') {
      params.isRead = 0
    } else if (activeTab.value === 'read') {
      params.isRead = 1
    }
    
    const result = await getNotificationList(params)
    if (result) {
      notificationList.value = result.list || []
      
      // 根据标签页更新计数
      if (activeTab.value === 'all') {
        totalCount.value = result.total || 0
        // 已读数量 = 总数 - 未读数（需要重新加载未读数来更新）
        loadUnreadCount()
      } else if (activeTab.value === 'unread') {
        unreadCount.value = result.total || 0
      } else if (activeTab.value === 'read') {
        readCount.value = result.total || 0
      }
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const result = await getUnreadCount()
    if (result !== undefined && result !== null) {
      unreadCount.value = result || 0
      // 如果当前在"全部"标签页，计算已读数量 = 总数 - 未读数
      if (activeTab.value === 'all' && totalCount.value > 0) {
        readCount.value = Math.max(0, totalCount.value - unreadCount.value)
      }
    } else {
      unreadCount.value = 0
      readCount.value = 0
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
    // API调用失败时不影响页面显示，静默处理，设为0
    unreadCount.value = 0
    readCount.value = 0
  }
}

// 标签页切换
const handleTabChange = () => {
  currentPage.value = 1
  loadNotifications()
}

// 每页大小改变
const handleSizeChange = () => {
  currentPage.value = 1
  loadNotifications()
}

// 标记为已读
const handleMarkAsRead = async (notificationId) => {
  try {
    await markAsRead({ notificationId })
    ElMessage.success('已标记为已读')
    
    // 重新加载
    loadNotifications()
    loadUnreadCount()
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 标记所有为已读
const handleMarkAllAsRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    
    // 重新加载
    loadNotifications()
    loadUnreadCount()
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记全部已读失败')
  }
}

// 点击通知项
const handleNotificationClick = async (item) => {
  // 如果未读，先标记为已读
  if (item.isRead === 0) {
    await handleMarkAsRead(item.id)
  }
  
  // 关闭弹窗
  visible.value = false
  
  // 跳转到报警日志页面，传递alarmId用于精准定位
  router.push({
    path: '/alarms',
    query: {
      alarmId: item.alarmId
    }
  })
}

// 查看全部
const handleViewAll = () => {
  visible.value = false
  router.push('/alarms')
}

// 获取报警级别类型
const getLevelType = (level) => {
  const map = {
    'critical': 'danger',
    'warning': 'warning',
    'info': 'info'
  }
  return map[level] || 'info'
}

// 获取报警级别文本
const getLevelText = (level) => {
  const map = {
    'critical': '严重',
    'warning': '警告',
    'info': '提示'
  }
  return map[level] || level
}

// 格式化时间 - 显示具体的触发时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  try {
    const date = new Date(timeStr)
    
    // 格式化为：YYYY-MM-DD HH:mm:ss
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return timeStr
  }
}

// 监听弹窗显示状态
watch(visible, (newVal) => {
  if (newVal) {
    loadNotifications()
  }
})

// 定时刷新未读数量（每30秒）
let refreshTimer = null
onMounted(() => {
  loadUnreadCount()
  
  refreshTimer = setInterval(() => {
    loadUnreadCount()
  }, 30000) // 30秒刷新一次
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 暴露方法供父组件调用
defineExpose({
  loadUnreadCount,
  refresh: loadUnreadCount
})
</script>

<style scoped>
.notification-panel {
  max-height: 600px;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  padding: 8px 0;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f7;
}

.notification-item:hover {
  background-color: #f5f5f7;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.notification-item.unread:hover {
  background-color: #e0f2fe;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.device-name {
  font-weight: 500;
  color: #1d1d1f;
  font-size: 14px;
}

.device-code {
  color: #86868b;
  font-size: 12px;
  font-weight: normal;
}

.read-status-tag {
  margin-left: auto;
}

.notification-message {
  color: #6e6e73;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.notification-time {
  color: #86868b;
  font-size: 12px;
}

.notification-action {
  margin-left: 12px;
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-info {
  display: flex;
  align-items: center;
}

.total-info {
  color: #6e6e73;
  font-size: 13px;
}

.message-time {
  color: #86868b;
  font-size: 12px;
  margin-left: 8px;
}

.tab-count {
  color: #6e6e73;
  font-size: 12px;
  font-weight: normal;
}

.tab-badge {
  margin-left: 4px;
}

:deep(.el-tabs__header) {
  margin: 0 16px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-tabs__content) {
  padding: 0;
}
</style>

<style>
.notification-popover {
  padding: 0 !important;
}
</style>
