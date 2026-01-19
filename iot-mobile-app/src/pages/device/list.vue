<template>
  <view class="device-list-page">
    <view class="mobile-header-bar">
      <!-- 搜索框 -->
      <view class="search-row">
        <view class="search-input-wrapper">
          <view class="icon-svg" v-html="getSearchIcon()" style="width: 18px; height: 18px; color: #86868b;"></view>
          <input 
            v-model="searchKeyword" 
            placeholder="搜索设备名称/编码" 
            class="search-input"
            placeholder-style="color: #86868b"
            @input="handleSearch"
          />
        </view>
      </view>
      <!-- 筛选器行 -->
      <view class="filter-row">
        <picker 
          mode="selector" 
          :range="groupOptions" 
          range-key="label" 
          @change="handleGroupChange" 
          class="filter-picker"
        >
          <view class="picker-text">{{ selectedGroup.label || '分组' }}</view>
        </picker>
        <picker 
          mode="selector" 
          :range="productOptions" 
          range-key="label" 
          @change="handleProductChange" 
          class="filter-picker"
        >
          <view class="picker-text">{{ selectedProduct.label || '型号' }}</view>
        </picker>
        <picker 
          mode="selector" 
          :range="statusOptions" 
          range-key="label" 
          @change="handleStatusChange" 
          class="filter-picker"
        >
          <view class="picker-text">{{ selectedStatus.label || '状态' }}</view>
        </picker>
      </view>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="mobile-content-area">
        <view class="device-list">
          <view 
            class="device-card" 
            v-for="device in deviceList" 
            :key="device.id"
            @click="navigateToDetail(device)"
          >
            <view class="device-status-indicator" :class="device.status === 1 || device.status === 'online' ? 'online' : 'offline'"></view>
            <view class="device-info">
              <text class="device-name">{{ device.name || device.deviceName || '-' }}</text>
              <text class="device-meta">{{ (device.code || device.deviceCode || '-') }} · {{ device.productName || '未知产品' }} · {{ device.productType || '' }}</text>
              <text class="device-time">最后上线：{{ formatTime(device.lastOnlineTime) }}</text>
            </view>
            <view class="device-tag" :class="device.status === 1 || device.status === 'online' ? 'tag-success' : 'tag-warning'">
              <text>{{ device.status === 1 || device.status === 'online' ? '在线' : '离线' }}</text>
            </view>
          </view>
        </view>
        
        <view v-if="deviceList.length === 0 && !loading" class="empty-state">
          <text class="empty-text">暂无设备</text>
        </view>
      </view>
    </scroll-view>
    <BottomNav />
  </view>
</template>

<script>
import { getDeviceList } from '@/api/device'
import { getGroupList } from '@/api/group'
import BottomNav from '@/components/BottomNav.vue'

export default {
  components: {
    BottomNav
  },
  data() {
    return {
      deviceList: [],
      searchKeyword: '',
      selectedGroup: { label: '分组', value: '' },
      selectedProduct: { label: '型号', value: '' },
      selectedStatus: { label: '状态', value: '' },
      groupOptions: [{ label: '分组', value: '' }],
      productOptions: [
        { label: '型号', value: '' },
        { label: 'ELEC', value: 'ELEC' },
        { label: 'WATER', value: 'WATER' },
        { label: 'GAS', value: 'GAS' }
      ],
      statusOptions: [
        { label: '状态', value: '' },
        { label: '在线', value: 'online' },
        { label: '离线', value: 'offline' }
      ],
      loading: false
    }
  },
  onLoad() {
    this.loadGroups()
    this.loadDeviceList()
  },
  onShow() {
    this.loadDeviceList()
  },
  methods: {
    async loadGroups() {
      try {
        const res = await getGroupList()
        if (res && res.list) {
          this.groupOptions = [
            { label: '分组', value: '' },
            ...res.list.map(item => ({
              label: item.name || item.groupName || '未知分组',
              value: item.id
            }))
          ]
        }
      } catch (error) {
        console.error('加载分组列表失败:', error)
      }
    },
    handleGroupChange(e) {
      this.selectedGroup = this.groupOptions[e.detail.value]
      this.loadDeviceList()
    },
    async loadDeviceList() {
      this.loading = true
      try {
        const params = {
          page: 1,
          pageSize: 100
        }
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        if (this.selectedProduct.value) {
          params.productType = this.selectedProduct.value
        }
        if (this.selectedStatus.value) {
          params.status = this.selectedStatus.value
        }
        if (this.selectedGroup.value) {
          params.groupId = this.selectedGroup.value
        }
        
        const res = await getDeviceList(params)
        console.log('设备列表响应:', res)
        
        // 后端返回的数据结构：{ list: [], total: 5, ... }
        if (res) {
          // 优先使用 res.list，如果没有则尝试 res.records
          const deviceArray = res.list || res.records || []
          
          if (deviceArray.length > 0) {
            this.deviceList = deviceArray.map(item => ({
              ...item,
              name: item.name || item.deviceName,
              code: item.code || item.deviceCode,
              productName: item.productName || '未知产品',
              productType: item.productType || '',
              status: item.status === 1 ? 'online' : 'offline'
            }))
          } else {
            this.deviceList = []
          }
        } else {
          this.deviceList = []
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.loadDeviceList()
    },
    handleProductChange(e) {
      this.selectedProduct = this.productOptions[e.detail.value]
      this.loadDeviceList()
    },
    handleStatusChange(e) {
      this.selectedStatus = this.statusOptions[e.detail.value]
      this.loadDeviceList()
    },
    navigateToDetail(device) {
      uni.navigateTo({
        url: `/pages/device/detail?id=${device.id}&code=${device.code || device.deviceCode}&name=${encodeURIComponent(device.name || device.deviceName || '')}`
      })
    },
    getSearchIcon() {
      return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>'
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
  }
}
</script>

<style scoped>
.device-list-page {
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
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.search-row {
  width: 100%;
  margin-bottom: 16rpx;
}

.filter-row {
  display: flex;
  gap: 16rpx;
  width: 100%;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  background: #f5f5f7;
  border-radius: 16rpx;
  padding: 0 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.search-icon {
  flex-shrink: 0;
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

.search-input {
  flex: 1;
  height: 64rpx;
  font-size: 28rpx;
  color: #1d1d1f;
  border: none;
  background: transparent;
}

.filter-picker {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  font-size: 24rpx;
  color: #1d1d1f;
  border: 1rpx solid #e5e7eb;
  min-width: 120rpx;
}

.picker-text {
  color: #1d1d1f;
}

.content-scroll {
  height: calc(100vh - 120rpx);
}

.mobile-content-area {
  padding: 32rpx;
  padding-bottom: 120rpx;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.device-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.device-card:active {
  opacity: 0.8;
}

.device-status-indicator {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.device-status-indicator.online {
  background: #34c759;
  box-shadow: 0 0 16rpx rgba(52, 199, 89, 0.4);
}

.device-status-indicator.offline {
  background: #ff3b30;
}

.device-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.device-name {
  display: block;
  font-size: 32rpx;
  font-weight: 500;
  color: #1d1d1f;
}

.device-meta {
  display: block;
  font-size: 26rpx;
  color: #86868b;
}

.device-time {
  display: block;
  font-size: 24rpx;
  color: #86868b;
}

.device-tag {
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.tag-success {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.tag-warning {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
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
