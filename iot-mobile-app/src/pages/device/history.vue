<template>
  <view class="device-history-page">
    <view class="mobile-header-bar">
      <view class="back-icon" @click="goBack">
        <Icon name="ArrowLeft" :size="24" />
      </view>
      <text class="header-title">历史数据</text>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <view class="device-info-bar">
          <text class="device-name-bar">{{ deviceName }}</text>
        </view>

        <!-- 属性选择和时间范围 -->
        <view class="filter-card">
          <picker 
            mode="selector" 
            :range="attrOptions" 
            range-key="label" 
            @change="handleAttrChange"
            class="filter-picker"
          >
            <view class="filter-item">
              <Icon name="DataLine" :size="20" color="#667eea" />
              <text class="filter-label">{{ selectedAttr.label || '选择属性' }}</text>
              <Icon name="ArrowRight" :size="18" color="#86868b" />
            </view>
          </picker>
          
          <view class="filter-date-row">
            <picker 
              mode="date" 
              :value="startDate"
              @change="handleStartDateChange"
              class="filter-date-picker"
            >
              <view class="filter-item">
                <Icon name="Clock" :size="18" color="#86868b" />
                <text class="filter-label">{{ startDate || '开始日期' }}</text>
              </view>
            </picker>
            <text class="date-separator">至</text>
            <picker 
              mode="date" 
              :value="endDate"
              @change="handleEndDateChange"
              class="filter-date-picker"
            >
              <view class="filter-item">
                <Icon name="Clock" :size="18" color="#86868b" />
                <text class="filter-label">{{ endDate || '结束日期' }}</text>
              </view>
            </picker>
          </view>
          
          <button class="filter-query-btn" @click="handleQuery" :disabled="loading || !selectedAttr.value">
            <Icon name="Search" :size="20" color="#ffffff" />
            <text>{{ loading ? '查询中...' : '查询' }}</text>
          </button>
        </view>

        <!-- 数据列表 -->
        <view class="data-list-card">
          <view 
            class="data-item-card" 
            v-for="(item, index) in paginatedData" 
            :key="index"
          >
            <text class="data-time-text">{{ formatTime(item.reportTime) }}</text>
            <view class="data-value-wrapper">
              <text class="data-value-number">{{ formatValue(item.value) }}</text>
              <text v-if="selectedAttr.unit" class="data-unit-text">{{ selectedAttr.unit }}</text>
            </view>
          </view>
          
          <view v-if="historyData.length === 0 && !loading && selectedAttr.value" class="empty-state">
            <text class="empty-text">暂无数据</text>
          </view>
          <view v-if="!selectedAttr.value && !loading" class="empty-state">
            <text class="empty-text">请先选择属性</text>
          </view>
          <view v-if="loading" class="empty-state">
            <text class="empty-text">加载中...</text>
          </view>
        </view>

        <!-- 分页控件 -->
        <view v-if="historyData.length > 0" class="pagination-wrapper">
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
  </view>
</template>

<script>
import { getHistoryData } from '@/api/data'
import { getDeviceDetail } from '@/api/device'
import { getProductAttributes } from '@/api/product'
import Icon from '@/components/Icon.vue'

export default {
  components: {
    Icon
  },
  data() {
    return {
      deviceCode: '',
      deviceName: '',
      productId: null,
      productAttributes: [],
      selectedAttr: { label: '选择属性', value: '', unit: '' },
      attrOptions: [{ label: '选择属性', value: '', unit: '' }],
      startDate: '',
      endDate: '',
      historyData: [],
      loading: false,
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      }
    }
  },
  computed: {
    paginatedData() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.historyData.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.pagination.total / this.pagination.pageSize) || 1
    }
  },
  onLoad(options) {
    if (options.code) {
      this.deviceCode = options.code
    }
    if (options.name) {
      this.deviceName = decodeURIComponent(options.name)
    }
    this.loadDeviceInfo()
  },
  methods: {
    async loadDeviceInfo() {
      try {
        const res = await getDeviceDetail({
          deviceCode: this.deviceCode
        })
        if (res) {
          this.productId = res.productId
          if (!this.deviceName && res.name) {
            this.deviceName = res.name || res.deviceName
          }
          // 加载产品属性
          await this.loadProductAttributes()
          // 设置默认时间范围
          this.setDefaultDateRange()
        }
      } catch (error) {
        console.error('加载设备信息失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '加载失败') 
          : String(error || '加载失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },
    async loadProductAttributes() {
      if (!this.productId) {
        console.warn('产品ID不存在')
        return
      }
      
      try {
        const attrData = await getProductAttributes(this.productId)
        if (attrData) {
          this.productAttributes = attrData || []
          // 构建属性选项
          this.attrOptions = [
            { label: '选择属性', value: '', unit: '' },
            ...this.productAttributes.map(attr => ({
              label: attr.attrName || attr.name,
              value: attr.addr || attr.identifier,
              unit: attr.unit || ''
            }))
          ]
        }
      } catch (error) {
        console.error('加载产品属性失败:', error)
      }
    },
    setDefaultDateRange() {
      // 设置默认时间范围：今天00:00到现在
      const now = new Date()
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      
      this.startDate = this.formatDate(today)
      this.endDate = this.formatDate(now)
    },
    async loadHistoryData() {
      if (!this.startDate || !this.endDate || !this.selectedAttr.value) {
        return
      }
      
      this.loading = true
      try {
        const params = {
          deviceCode: this.deviceCode,
          startTime: `${this.startDate} 00:00:00`,
          endTime: `${this.endDate} 23:59:59`,
          attrs: this.selectedAttr.value // 指定查询的属性
        }
        
        const data = await getHistoryData(params)
        
        if (data) {
          // 后端返回的是 [{addr: 'tem', addrv: '22.5', ctime: '2025-12-25 22:30:00'}, ...]
          // 按时间排序，取对应属性的值
          const filteredData = data
            .filter(item => item.addr === this.selectedAttr.value)
            .map(item => {
              // 确保时间格式为 yyyy-MM-dd HH:mm:ss，去掉T
              let time = ''
              if (item.ctime) {
                time = item.ctime.includes('T') ? item.ctime.replace('T', ' ') : item.ctime
                time = time.substring(0, 19) // 确保只取 yyyy-MM-dd HH:mm:ss
              }
              return {
                reportTime: time,
                value: item.addrv
              }
            })
            .sort((a, b) => {
              return new Date(b.reportTime) - new Date(a.reportTime) // 降序
            })
          
          this.historyData = filteredData
          this.pagination.total = filteredData.length
          this.pagination.currentPage = 1 // 重置到第一页
        } else {
          this.historyData = []
        }
      } catch (error) {
        console.error('加载历史数据失败:', error)
        const errorMsg = error && typeof error === 'object' 
          ? (error.message || error.errMsg || '查询失败') 
          : String(error || '查询失败')
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      } finally {
        this.loading = false
      }
    },
    handleAttrChange(e) {
      this.selectedAttr = this.attrOptions[e.detail.value]
      // 选择属性后自动查询
      if (this.selectedAttr.value) {
        this.loadHistoryData()
      } else {
        this.historyData = []
      }
    },
    handleStartDateChange(e) {
      this.startDate = e.detail.value
    },
    handleEndDateChange(e) {
      this.endDate = e.detail.value
    },
    handleQuery() {
      if (!this.startDate || !this.endDate) {
        uni.showToast({
          title: '请选择时间范围',
          icon: 'none',
          duration: 2000
        })
        return
      }
      if (!this.selectedAttr.value) {
        uni.showToast({
          title: '请先选择属性',
          icon: 'none',
          duration: 2000
        })
        return
      }
      this.loadHistoryData()
    },
    goBack() {
      uni.navigateBack()
    },
    formatDate(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    formatTime(time) {
      if (!time) return '-'
      // 确保时间格式为 yyyy-MM-dd HH:mm:ss，去掉T，不换行
      let timeStr = typeof time === 'string' ? time : String(time)
      if (timeStr.includes('T')) {
        timeStr = timeStr.replace('T', ' ')
      }
      // 只取前19个字符（yyyy-MM-dd HH:mm:ss）
      return timeStr.substring(0, 19)
    },
    formatValue(value) {
      if (value === null || value === undefined || value === '') return '-'
      if (typeof value === 'number') {
        return value.toFixed(2)
      }
      return String(value)
    },
    prevPage() {
      if (this.pagination.currentPage > 1) {
        this.pagination.currentPage--
      }
    },
    nextPage() {
      if (this.pagination.currentPage < this.totalPages) {
        this.pagination.currentPage++
      }
    }
  }
}
</script>

<style scoped>
.device-history-page {
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
  gap: 24rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.back-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1d1d1f;
  cursor: pointer;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
  flex: 1;
}

.content-scroll {
  height: calc(100vh - 88rpx);
}

.mobile-content-area {
  padding: 32rpx;
}

.device-info-bar {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.device-name-bar {
  font-size: 36rpx;
  font-weight: 600;
  color: #1d1d1f;
}

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
  max-width: 92%;
}

.filter-picker {
  width: 100%;
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
  gap: 16rpx;
}

.filter-date-picker {
  flex: 1;
}

.date-separator {
  font-size: 28rpx;
  color: #86868b;
  padding: 0 8rpx;
  flex-shrink: 0;
  font-weight: 500;
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

.data-list-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  margin: 0 auto;
  max-width: 92%;
}

.data-item-card {
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}

.data-item-card:last-child {
  border-bottom: none;
}

.data-item-card:active {
  background: #f9f9f9;
  margin: 0 -24rpx;
  padding-left: 24rpx;
  padding-right: 24rpx;
}

.data-time-text {
  font-size: 28rpx;
  color: #86868b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
  min-width: 0;
}

.data-value-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  flex-wrap: nowrap;
  flex-shrink: 0;
  text-align: right;
}

.data-value-number {
  font-size: 40rpx;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.data-unit-text {
  font-size: 26rpx;
  font-weight: 400;
  color: #86868b;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 128rpx 32rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #86868b;
}

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
  max-width: 92%;
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
</style>
