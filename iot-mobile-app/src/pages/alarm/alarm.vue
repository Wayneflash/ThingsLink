<template>
  <view class="alarm-page">
    <view class="mobile-header-bar">
      <text class="header-title">报警管理</text>
      <view class="header-icon refresh-btn" @click="handleRefresh">
        <Icon name="Refresh" :size="20" />
      </view>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <!-- 筛选栏 -->
        <view class="filter-card">
          <!-- 搜索框 -->
          <view class="search-wrapper">
            <Icon name="Search" :size="20" color="#86868b" />
            <input 
              class="search-input" 
              v-model="searchKeyword"
              placeholder="搜索设备编码/设备名称"
              @input="handleSearchInput"
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <text>×</text>
            </view>
          </view>

          <!-- 筛选器行 -->
          <view class="filter-row">
            <picker 
              mode="selector" 
              :range="levelOptions" 
              range-key="label" 
              @change="handleLevelChange"
              class="filter-picker"
            >
              <view class="filter-item">
                <Icon name="WarningFilled" :size="18" color="#667eea" />
                <text class="filter-label">{{ selectedLevel.label || '报警级别' }}</text>
                <Icon name="ArrowRight" :size="16" color="#86868b" />
              </view>
            </picker>
            <picker 
              mode="selector" 
              :range="statusOptions" 
              range-key="label" 
              @change="handleStatusChange"
              class="filter-picker"
            >
              <view class="filter-item">
                <Icon name="CircleCheck" :size="18" color="#667eea" />
                <text class="filter-label">{{ selectedStatus.label || '处理状态' }}</text>
                <Icon name="ArrowRight" :size="16" color="#86868b" />
              </view>
            </picker>
          </view>

          <!-- 时间范围 -->
          <view class="filter-date-row">
            <picker 
              mode="date" 
              :value="startDate"
              @change="handleStartDateChange"
              class="filter-date-picker"
            >
              <view class="filter-date-item">
                <Icon name="Clock" :size="18" color="#86868b" />
                <text class="filter-date-text">{{ startDate || '开始日期' }}</text>
              </view>
            </picker>
            <text class="date-separator">至</text>
            <picker 
              mode="date" 
              :value="endDate"
              @change="handleEndDateChange"
              class="filter-date-picker"
            >
              <view class="filter-date-item">
                <Icon name="Clock" :size="18" color="#86868b" />
                <text class="filter-date-text">{{ endDate || '结束日期' }}</text>
              </view>
            </picker>
          </view>

          <!-- 查询按钮 -->
          <button class="filter-query-btn" @click="loadAlarmList" :disabled="loading">
            <Icon name="Search" :size="20" color="#ffffff" />
            <text>{{ loading ? '查询中...' : '查询' }}</text>
          </button>
        </view>

        <!-- 报警列表 -->
        <view class="alarm-list-wrapper">
          <view 
            class="alarm-list-item" 
            v-for="(alarm, index) in paginatedData" 
            :key="alarm.id"
            @click="handleAlarmClick(alarm)"
          >
            <!-- 左侧图标和指示条 -->
            <view class="alarm-item-left">
              <view class="alarm-level-indicator" :class="alarm.alarmLevel"></view>
              <view class="alarm-icon-mini" :class="alarm.alarmLevel">
                <Icon :name="getAlarmIconName(alarm.alarmLevel)" :size="18" color="#ffffff" />
              </view>
            </view>
            
            <!-- 中间内容 -->
            <view class="alarm-item-center">
              <view class="alarm-item-header">
                <text class="alarm-device-title">{{ alarm.deviceName || alarm.deviceCode || '-' }}</text>
                <view class="alarm-badge-group">
                  <view class="alarm-level-badge-mini" :class="alarm.alarmLevel">
                    <text>{{ getLevelText(alarm.alarmLevel) }}</text>
                  </view>
                  <view class="alarm-status-badge-mini" :class="alarm.status === 1 ? 'status-processed' : 'status-pending'">
                    <text>{{ alarm.status === 1 ? '已处理' : '未处理' }}</text>
                  </view>
                </view>
              </view>
              <text class="alarm-content-text">{{ alarm.alarmMessage || alarm.message || '-' }}</text>
              <view class="alarm-item-meta">
                <view class="alarm-time-item">
                  <Icon name="Clock" :size="12" color="#86868b" />
                  <text class="alarm-time-label">{{ formatTime(alarm.triggerTime) }}</text>
                </view>
                <text class="alarm-code-label" v-if="alarm.deviceCode && alarm.deviceName">{{ alarm.deviceCode }}</text>
              </view>
            </view>
            
            <!-- 右侧箭头 -->
            <view class="alarm-item-right">
              <Icon name="ArrowRight" :size="18" color="#c7c7cc" />
            </view>
            
            <!-- 分割线 -->
            <view v-if="index < paginatedData.length - 1" class="alarm-divider"></view>
          </view>
          
          <view v-if="alarmList.length === 0 && !loading" class="empty-state">
            <text class="empty-text">暂无报警记录</text>
          </view>
          <view v-if="loading" class="empty-state">
            <text class="empty-text">加载中...</text>
          </view>
        </view>

        <!-- 处理报警对话框 -->
        <view v-if="handleDialogVisible" class="modal-overlay" @click.self="handleDialogVisible = false">
          <view class="modal-container" @click.stop>
            <!-- 标题栏 -->
            <view class="modal-header">
              <text class="modal-title">处理报警</text>
              <view class="modal-close-btn" @click="handleDialogVisible = false">
                <Icon name="CircleClose" :size="20" color="#86868b" />
              </view>
            </view>
            
            <!-- 内容区域 -->
            <scroll-view scroll-y class="modal-scroll-body">
              <!-- 处理描述 -->
              <view class="modal-form-item">
                <text class="modal-form-label">处理描述</text>
                <view class="textarea-wrapper">
                  <textarea 
                    class="modal-textarea" 
                    v-model="handleForm.description"
                    placeholder="请描述处理方式和结果"
                    maxlength="500"
                    placeholder-style="color: #86868b"
                  ></textarea>
                  <text class="char-counter">{{ handleForm.description.length }}/500</text>
                </view>
              </view>
              
              <!-- 上传图片 -->
              <view class="modal-form-item">
                <text class="modal-form-label">上传图片</text>
                <view class="upload-images-grid">
                  <!-- 已上传的图片 -->
                  <view 
                    class="uploaded-image-box" 
                    v-for="(img, index) in handleForm.images" 
                    :key="index"
                  >
                    <image :src="img" mode="aspectFill" class="uploaded-image" />
                    <view class="image-delete-btn" @click="removeImage(index)">
                      <Icon name="CircleClose" :size="18" color="#ffffff" />
                    </view>
                  </view>
                  <!-- 上传按钮 -->
                  <view 
                    v-if="handleForm.images.length < 5" 
                    class="upload-image-btn" 
                    @click="chooseImage"
                  >
                    <Icon name="Plus" :size="32" color="#86868b" />
                    <text class="upload-btn-text">添加图片</text>
                  </view>
                </view>
                <text class="upload-tip">支持jpg/png格式，最多5张</text>
              </view>
            </scroll-view>
            
            <!-- 底部按钮 -->
            <view class="modal-footer">
              <button class="footer-btn cancel-footer-btn" @click="handleDialogVisible = false">
                <text>取消</text>
              </button>
              <button 
                class="footer-btn confirm-footer-btn" 
                @click="handleSave" 
                :disabled="handleLoading"
              >
                <text>{{ handleLoading ? '处理中...' : '确认处理' }}</text>
              </button>
            </view>
          </view>
        </view>

        <!-- 查看处理详情对话框 -->
        <view v-if="detailDialogVisible" class="modal-overlay" @click.self="detailDialogVisible = false">
          <view class="modal-container" @click.stop>
            <view class="modal-header">
              <text class="modal-title">处理详情</text>
              <view class="modal-close-btn" @click="detailDialogVisible = false">
                <Icon name="CircleClose" :size="20" color="#86868b" />
              </view>
            </view>
            <scroll-view scroll-y class="modal-scroll-body">
              <view class="detail-item">
                <text class="detail-label">处理人：</text>
                <text class="detail-value">{{ detailData.handler || '-' }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-label">处理时间：</text>
                <text class="detail-value">{{ formatTime(detailData.handleTime) }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-label">处理描述：</text>
                <text class="detail-value">{{ detailData.handleDescription || '未填写' }}</text>
              </view>
              <view v-if="detailData.handleImages && detailData.handleImages.length > 0" class="detail-item">
                <text class="detail-label">处理图片：</text>
                <view class="detail-images">
                  <image 
                    v-for="(img, index) in detailData.handleImages" 
                    :key="index"
                    :src="img" 
                    mode="aspectFill" 
                    class="detail-image"
                    @click="previewImage(img, detailData.handleImages)"
                  />
                </view>
              </view>
            </scroll-view>
            <view class="modal-footer">
              <button class="footer-btn confirm-footer-btn" @click="detailDialogVisible = false" style="width: 100%;">
                <text>关闭</text>
              </button>
            </view>
          </view>
        </view>

        <!-- 分页控件 -->
        <view v-if="alarmList.length > 0" class="pagination-wrapper">
          <view class="pagination-info">
            <text>共 {{ pagination.total }} 条</text>
          </view>
          <view class="pagination-controls">
            <button 
              class="page-btn" 
              :disabled="pagination.currentPage === 1"
              @click="prevPage"
            >
              <text>上一页</text>
            </button>
            <text class="page-info">{{ pagination.currentPage }} / {{ totalPages }}</text>
            <button 
              class="page-btn" 
              :disabled="pagination.currentPage === totalPages"
              @click="nextPage"
            >
              <text>下一页</text>
            </button>
          </view>
        </view>
      </view>
    </scroll-view>
    <BottomNav />
  </view>
</template>

<script>
import { getAlarmLogList, getAlarmStatistics, handleAlarm } from '@/api/alarm'
import BottomNav from '@/components/BottomNav.vue'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    BottomNav,
    Icon
  },
  data() {
    return {
      alarmList: [],
      alarmStats: {
        total: 0,
        unhandled: 0,
        critical: 0,
        warning: 0,
        info: 0
      },
      selectedLevel: { label: '报警级别', value: '' },
      selectedStatus: { label: '处理状态', value: '' },
      searchKeyword: '',
      startDate: '',
      endDate: '',
      levelOptions: [
        { label: '报警级别', value: '' },
        { label: '全部', value: '' },
        { label: '严重', value: 'critical' },
        { label: '警告', value: 'warning' },
        { label: '提示', value: 'info' }
      ],
      statusOptions: [
        { label: '处理状态', value: '' },
        { label: '全部', value: '' },
        { label: '未处理', value: 0 },
        { label: '已处理', value: 1 }
      ],
      loading: false,
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      },
      // 处理对话框
      handleDialogVisible: false,
      handleLoading: false,
      currentAlarm: null,
      handleForm: {
        description: '',
        images: []
      },
      // 详情对话框
      detailDialogVisible: false,
      detailData: {
        handler: '',
        handleTime: '',
        handleDescription: '',
        handleImages: []
      }
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.alarmList.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.pagination.total / this.pagination.pageSize) || 1
    }
  },
  onLoad() {
    this.loadAlarmStats()
    this.loadAlarmList()
  },
  onShow() {
    this.loadAlarmStats()
    this.loadAlarmList()
  },
  methods: {
    async loadAlarmStats() {
      try {
        const res = await getAlarmStatistics()
        if (res) {
          this.alarmStats = {
            total: res.total || 0,
            unhandled: res.unhandled || 0,
            critical: res.critical || 0,
            warning: res.warning || 0,
            info: res.info || 0
          }
        }
      } catch (error) {
        console.error('加载报警统计失败:', error)
      }
    },
    getBellIcon() {
      return '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>'
    },
    getClockIcon() {
      return '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>'
    },
    getWarningIcon() {
      return '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>'
    },
    getAlertIcon() {
      return '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>'
    },
    getInfoIcon() {
      return '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>'
    },
    async loadAlarmList() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        }
        
        // 设备编码搜索
        if (this.searchKeyword) {
          params.deviceCode = this.searchKeyword
        }
        
        // 报警级别筛选
        if (this.selectedLevel.value) {
          params.alarmLevel = this.selectedLevel.value
        }
        
        // 处理状态筛选
        if (this.selectedStatus.value !== '') {
          params.status = this.selectedStatus.value
        }
        
        // 时间范围
        if (this.startDate) {
          params.startTime = `${this.startDate} 00:00:00`
        }
        if (this.endDate) {
          params.endTime = `${this.endDate} 23:59:59`
        }
        
        const res = await getAlarmLogList(params)
        if (res) {
          // Web端返回 res.list，确保兼容性
          this.alarmList = res.list || res.records || []
          this.pagination.total = res.total || 0
        } else {
          this.alarmList = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('加载报警列表失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载失败') 
          : String(error || '加载失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.loading = false
      }
    },
    handleLevelChange(e) {
      this.selectedLevel = this.levelOptions[e.detail.value]
      this.pagination.currentPage = 1
      this.loadAlarmList()
    },
    handleStatusChange(e) {
      this.selectedStatus = this.statusOptions[e.detail.value]
      this.pagination.currentPage = 1
      this.loadAlarmList()
    },
    handleSearchInput() {
      // 防抖搜索，可以延迟执行
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.pagination.currentPage = 1
        this.loadAlarmList()
      }, 500)
    },
    clearSearch() {
      this.searchKeyword = ''
      this.pagination.currentPage = 1
      this.loadAlarmList()
    },
    handleStartDateChange(e) {
      this.startDate = e.detail.value
    },
    handleEndDateChange(e) {
      this.endDate = e.detail.value
    },
    prevPage() {
      if (this.pagination.currentPage > 1) {
        this.pagination.currentPage--
        this.loadAlarmList()
      }
    },
    nextPage() {
      if (this.pagination.currentPage < this.totalPages) {
        this.pagination.currentPage++
        this.loadAlarmList()
      }
    },
    getAlarmIconName(level) {
      if (level === 'critical') return 'WarningFilled'
      if (level === 'warning') return 'Warning'
      return 'InfoFilled'
    },
    handleRefresh() {
      this.loadAlarmStats()
      this.pagination.currentPage = 1
      this.loadAlarmList()
      uni.showToast({
        title: '刷新成功',
        icon: 'success',
        duration: 1500
      })
    },
    handleAlarmClick(alarm) {
      // 如果已处理，显示处理详情；如果未处理，显示处理对话框
      if (alarm.status === 1) {
        this.showHandleDetail(alarm)
      } else {
        this.showHandleDialog(alarm)
      }
    },
    showHandleDialog(alarm) {
      this.currentAlarm = alarm
      this.handleForm.description = ''
      this.handleForm.images = []
      this.handleDialogVisible = true
    },
    showHandleDetail(alarm) {
      // 解析图片数据
      let images = []
      if (alarm.handleImages) {
        try {
          if (typeof alarm.handleImages === 'string') {
            images = JSON.parse(alarm.handleImages)
          } else if (Array.isArray(alarm.handleImages)) {
            images = alarm.handleImages
          }
        } catch (error) {
          console.error('解析图片失败:', error)
        }
      }
      
      this.detailData = {
        handler: alarm.handler || '-',
        handleTime: alarm.handleTime || '-',
        handleDescription: alarm.handleDescription || '未填写',
        handleImages: images
      }
      this.detailDialogVisible = true
    },
    async handleSave() {
      if (!this.handleForm.description || this.handleForm.description.trim() === '') {
        uni.showToast({
          title: '请输入处理描述',
          icon: 'none'
        })
        return
      }

      this.handleLoading = true
      try {
        await handleAlarm({
          alarmId: this.currentAlarm.id,
          handleDescription: this.handleForm.description,
          handleImages: this.handleForm.images
        })
        
        uni.showToast({
          title: '处理成功',
          icon: 'success'
        })
        
        this.handleDialogVisible = false
        // 重新加载列表和统计
        await this.loadAlarmList()
        await this.loadAlarmStats()
      } catch (error) {
        console.error('处理报警失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '处理失败') 
          : String(error || '处理失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.handleLoading = false
      }
    },
    chooseImage() {
      const remaining = 5 - this.handleForm.images.length
      if (remaining <= 0) {
        uni.showToast({
          title: '最多只能上传5张图片',
          icon: 'none'
        })
        return
      }
      
      uni.chooseImage({
        count: remaining,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          // 将图片转为Base64
          let processedCount = 0
          res.tempFilePaths.forEach((filePath) => {
            uni.getFileSystemManager().readFile({
              filePath: filePath,
              encoding: 'base64',
              success: (fileRes) => {
                // 判断图片类型
                let mimeType = 'image/jpeg'
                if (filePath.toLowerCase().endsWith('.png')) {
                  mimeType = 'image/png'
                }
                this.handleForm.images.push(`data:${mimeType};base64,${fileRes.data}`)
                processedCount++
                if (processedCount === res.tempFilePaths.length) {
                  uni.showToast({
                    title: '图片添加成功',
                    icon: 'success',
                    duration: 1500
                  })
                }
              },
              fail: (err) => {
                console.error('读取图片失败:', err)
                uni.showToast({
                  title: '读取图片失败',
                  icon: 'none'
                })
              }
            })
          })
        },
        fail: (err) => {
          console.error('选择图片失败:', err)
          if (err.errMsg && err.errMsg.includes('cancel')) {
            // 用户取消选择，不提示错误
            return
          }
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    removeImage(index) {
      this.handleForm.images.splice(index, 1)
    },
    previewImage(current, urls) {
      uni.previewImage({
        current: current,
        urls: urls
      })
    },
    getAlarmClass(level) {
      return `alarm-${level || 'info'}`
    },
    getLevelText(level) {
      const map = {
        critical: '严重',
        warning: '警告',
        info: '提示'
      }
      return map[level] || '提示'
    },
    formatTime(time) {
      if (!time) return '-'
      // 去掉T，格式化为 yyyy-MM-dd HH:mm:ss，确保在一行显示
      let timeStr = typeof time === 'string' ? time : String(time)
      if (timeStr.includes('T')) {
        timeStr = timeStr.replace('T', ' ')
      }
      // 只取前19个字符（yyyy-MM-dd HH:mm:ss），确保不换行
      return timeStr.substring(0, 19)
    }
  },
  beforeDestroy() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  }
}
</script>

<style scoped>
.alarm-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.mobile-header-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
}

.header-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  cursor: pointer;
}

.refresh-btn:active {
  opacity: 0.6;
}

.icon-svg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.icon-svg svg {
  width: 100%;
  height: 100%;
}

.content-scroll {
  height: calc(100vh - 88rpx - 100rpx);
  padding-bottom: 120rpx;
}

.mobile-content-area {
  padding: 32rpx;
  padding-bottom: 120rpx;
}


/* 筛选卡片 */
.filter-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin: 0 auto 32rpx;
  width: 92%;
  max-width: 92%;
  box-sizing: border-box;
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  background: #f5f5f7;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
}

.search-wrapper:active {
  border-color: #667eea;
  background: #e5e7eb;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  color: #1d1d1f;
  background: transparent;
  border: none;
  outline: none;
}

.search-input::placeholder {
  color: #86868b;
}

.clear-icon {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86868b;
  font-size: 36rpx;
  line-height: 1;
  cursor: pointer;
}

.filter-row {
  display: flex;
  gap: 16rpx;
}

.filter-picker {
  flex: 1;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  background: #f5f5f7;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
}

.filter-item:active {
  background: #e5e7eb;
  border-color: #667eea;
}

.filter-label {
  flex: 1;
  font-size: 30rpx;
  color: #1d1d1f;
  font-weight: 500;
  text-align: left;
}

.filter-date-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: nowrap;
}

.filter-date-picker {
  flex: 1;
  min-width: 0;
}

.filter-date-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 16rpx;
  background: #f5f5f7;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
}

.filter-date-item:active {
  background: #e5e7eb;
  border-color: #667eea;
}

.filter-date-text {
  flex: 1;
  font-size: 26rpx;
  color: #1d1d1f;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.date-separator {
  font-size: 26rpx;
  color: #86868b;
  padding: 0 4rpx;
  flex-shrink: 0;
  font-weight: 500;
  white-space: nowrap;
}

.filter-query-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.filter-query-btn text {
  color: #ffffff;
}

.filter-query-btn:active {
  opacity: 0.9;
  transform: translateY(2rpx);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.25);
}

.filter-query-btn[disabled] {
  opacity: 0.5;
  transform: none;
}

/* 报警列表 - 简洁样式 */
.alarm-list-wrapper {
  background: #ffffff;
  border-radius: 24rpx;
  margin: 0 auto;
  width: 92%;
  max-width: 92%;
  box-sizing: border-box;
  overflow: hidden;
}

.alarm-list-item {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: 32rpx;
  gap: 24rpx;
  cursor: pointer;
  transition: background-color 0.2s ease;
  background: #ffffff;
}

.alarm-list-item:active {
  background: #f5f5f7;
}

.alarm-item-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.alarm-level-indicator {
  width: 4rpx;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
}

.alarm-level-indicator.critical {
  background: #ff3b30;
}

.alarm-level-indicator.warning {
  background: #ff9500;
}

.alarm-level-indicator.info {
  background: #34c759;
}

.alarm-icon-mini {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alarm-icon-mini.critical {
  background: linear-gradient(135deg, #ff3b30 0%, #ff6b6b 100%);
}

.alarm-icon-mini.warning {
  background: linear-gradient(135deg, #ff9500 0%, #ffb74d 100%);
}

.alarm-icon-mini.info {
  background: linear-gradient(135deg, #34c759 0%, #51cf66 100%);
}

.alarm-item-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  min-width: 0;
}

.alarm-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  flex-wrap: wrap;
}

.alarm-device-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.4;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-badge-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.alarm-level-badge-mini {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
  white-space: nowrap;
}

.alarm-level-badge-mini.critical {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.alarm-level-badge-mini.warning {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.alarm-level-badge-mini.info {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.alarm-status-badge-mini {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: 600;
  white-space: nowrap;
}

.alarm-status-badge-mini.status-processed {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.alarm-status-badge-mini.status-pending {
  background: rgba(255, 59, 48, 0.1);
  color: #ff3b30;
}

.alarm-content-text {
  font-size: 28rpx;
  color: #6e6e73;
  line-height: 1.6;
  word-break: break-word;
  margin-top: 4rpx;
}

.alarm-item-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 4rpx;
}

.alarm-time-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex: 1;
  min-width: 0;
}

.alarm-time-label {
  font-size: 24rpx;
  color: #86868b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.alarm-code-label {
  font-size: 24rpx;
  color: #86868b;
  white-space: nowrap;
  flex-shrink: 0;
}

.alarm-item-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding-top: 4rpx;
}

.alarm-divider {
  position: absolute;
  left: 104rpx;
  right: 32rpx;
  bottom: 0;
  height: 1rpx;
  background: #e5e7eb;
}

/* 分页 */
.pagination-wrapper {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 32rpx;
  margin-top: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 32rpx auto 0;
  width: 92%;
  max-width: 92%;
  box-sizing: border-box;
}

.pagination-info {
  font-size: 28rpx;
  color: #86868b;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.page-btn {
  padding: 12rpx 24rpx;
  background: #f5f5f7;
  border: 1rpx solid #e5e7eb;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #1d1d1f;
  transition: all 0.3s ease;
}

.page-btn:active:not([disabled]) {
  background: #e5e7eb;
  transform: scale(0.95);
}

.page-btn[disabled] {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 28rpx;
  color: #1d1d1f;
  font-weight: 500;
  min-width: 120rpx;
  text-align: center;
}

/* 模态框样式 - 优化移动端显示 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
  padding: 0;
  box-sizing: border-box;
}

.modal-container {
  width: 100%;
  max-height: 90vh;
  background: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.16);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #e5e7eb;
  flex-shrink: 0;
  background: #ffffff;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.4;
  letter-spacing: -0.5px;
}

.modal-close-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.modal-close-btn:active {
  background: #f5f5f7;
  transform: scale(0.95);
}

.modal-scroll-body {
  flex: 1;
  padding: 32rpx;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.modal-form-item {
  margin-bottom: 32rpx;
}

.modal-form-item:last-child {
  margin-bottom: 0;
}

.modal-form-label {
  display: block;
  font-size: 28rpx;
  color: #1d1d1f;
  margin-bottom: 16rpx;
  font-weight: 600;
  line-height: 1.4;
}

.textarea-wrapper {
  position: relative;
  width: 92%;
  margin: 0 auto;
}

.modal-textarea {
  width: 100%;
  min-height: 240rpx;
  max-height: 400rpx;
  padding: 20rpx;
  background: #f5f5f7;
  border: 2rpx solid #e5e7eb;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1d1d1f;
  box-sizing: border-box;
  line-height: 1.5;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  transition: all 0.3s ease;
  word-wrap: break-word;
  word-break: break-all;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.modal-textarea:focus {
  border-color: #667eea;
  background: #ffffff;
  outline: none;
  box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
}

.char-counter {
  position: absolute;
  bottom: 12rpx;
  right: 16rpx;
  font-size: 22rpx;
  color: #86868b;
  background: rgba(255, 255, 255, 0.95);
  padding: 4rpx 10rpx;
  border-radius: 6rpx;
  pointer-events: none;
  font-weight: 500;
}

.upload-images-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.uploaded-image-box {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 16rpx;
  overflow: hidden;
  background: #f5f5f7;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-delete-btn {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 52rpx;
  height: 52rpx;
  background: rgba(0, 0, 0, 0.65);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(4px);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.image-delete-btn:active {
  background: rgba(0, 0, 0, 0.85);
  transform: scale(0.9);
}

.upload-image-btn {
  width: 100%;
  aspect-ratio: 1;
  border: 2rpx dashed #d1d5db;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9f9f9;
}

.upload-image-btn:active {
  border-color: #667eea;
  background: #f0f4ff;
  transform: scale(0.98);
}

.upload-btn-text {
  font-size: 26rpx;
  color: #86868b;
  font-weight: 500;
}

.upload-tip {
  display: block;
  font-size: 24rpx;
  color: #86868b;
  line-height: 1.5;
  margin-top: 8rpx;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #e5e7eb;
  flex-shrink: 0;
  background: #ffffff;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.footer-btn {
  flex: 1;
  height: 96rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  cursor: pointer;
}

.cancel-footer-btn {
  background: #ffffff;
  color: #1d1d1f;
  border: 2rpx solid #e5e7eb;
}

.cancel-footer-btn:active {
  background: #f5f5f7;
  border-color: #d1d5db;
  transform: scale(0.98);
}

.confirm-footer-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.confirm-footer-btn:active:not([disabled]) {
  opacity: 0.9;
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.25);
}

.confirm-footer-btn[disabled] {
  opacity: 0.5;
  transform: none;
  box-shadow: none;
  cursor: not-allowed;
}

/* 详情样式 - 优化移动端显示 */
.detail-item {
  margin-bottom: 36rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f5f5f7;
}

.detail-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.detail-label {
  display: block;
  font-size: 26rpx;
  color: #86868b;
  margin-bottom: 10rpx;
  font-weight: 500;
  line-height: 1.4;
}

.detail-value {
  display: block;
  font-size: 28rpx;
  color: #1d1d1f;
  line-height: 1.5;
  word-break: break-word;
  word-wrap: break-word;
  font-weight: 400;
  white-space: pre-wrap;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;
}

.detail-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  cursor: pointer;
  object-fit: cover;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease;
}

.detail-image:active {
  transform: scale(0.98);
}

.empty-state {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86868b;
}
</style>
