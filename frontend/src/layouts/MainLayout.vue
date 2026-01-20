<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
      <!-- Logo区域 -->
      <div class="sidebar-header">
        <div class="logo">
          <el-icon :size="28" class="logo-icon">
            <Connection />
          </el-icon>
          <transition name="fade">
            <span v-show="!isCollapsed" class="logo-text">ThingsLink</span>
          </transition>
        </div>
        <el-button
          class="collapse-btn"
          text
          @click="toggleSidebar"
        >
          <el-icon :size="20">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :unique-opened="true"
        class="sidebar-menu"
        router
        @select="handleMenuSelect"
      >
        <template v-for="menu in menuList" :key="menu.code">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.code">
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="getIconComponent(menu.icon)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.code"
              :index="child.path"
            >
              <el-icon v-if="child.icon">
                <component :is="getIconComponent(child.icon)" />
              </el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 无子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon">
              <component :is="getIconComponent(menu.icon)" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>

      <!-- 侧边栏底部用户信息 -->
      <div class="sidebar-footer">
        <div class="user-info">
          <el-avatar :size="40" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <transition name="fade">
            <div v-show="!isCollapsed" class="user-details">
              <div class="user-name">{{ userInfo.realName || userInfo.username }}</div>
              <div class="user-status">在线</div>
            </div>
          </transition>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumb">{{ breadcrumb }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 下载APP -->
          <AppDownload />
          
          <!-- 通知图标 -->
          <NotificationPanel ref="notificationPanelRef" />

          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" class="dropdown-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="dropdown-name">{{ userInfo.realName || userInfo.username }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="change-password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item command="system-settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Connection,
  User,
  Bell,
  ArrowDown,
  Setting,
  SwitchButton,
  Fold,
  Expand,
  DataAnalysis,
  Monitor,
  Box,
  FolderOpened,
  TrendCharts,
  BellFilled,
  Warning,
  Tools,
  Lock,
  VideoCamera,
  Document
} from '@element-plus/icons-vue'
import NotificationPanel from '@/components/NotificationPanel.vue'
import AppDownload from '@/components/AppDownload.vue'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 用户信息
const userInfo = ref({})

// 通知面板引用
const notificationPanelRef = ref(null)

// 菜单列表
const menuList = ref([])

// 图标组件映射
const iconMap = {
  DataAnalysis,
  Monitor,
  Box,
  FolderOpened,
  TrendCharts,
  BellFilled,
  Warning,
  Setting,
  User,
  Tools,
  VideoCamera,
  Document
}

// 获取图标组件
const getIconComponent = (iconName) => {
  return iconMap[iconName] || Box
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 页面标题映射
const pageTitleMap = {
  '/overview': '设备概览',
  '/devices': '设备管理',
  '/alarms': '报警日志',
  '/products': '产品管理',
  '/products/list': '产品列表',
  '/products/attributes': '产品属性',
  '/groups': '设备分组',
  '/users': '用户管理',
  '/roles': '角色管理',
  '/system': '系统管理',
  '/system-config/change-password': '修改密码',
  '/profile': '个人资料'
}

// 动态页面标题
const pageTitle = computed(() => {
  return pageTitleMap[route.path] || 'IOT Platform'
})

// 面包屑
const breadcrumb = computed(() => {
  if (route.path === '/') return null
  return pageTitle.value
})

// 加载用户信息和菜单
const loadUserInfo = () => {
  // 加载用户信息
  const userInfoData = localStorage.getItem('userInfo')
  if (userInfoData) {
    userInfo.value = JSON.parse(userInfoData)
  }

  // 加载菜单数据（从后端API获取，不硬编码）
  const menusData = localStorage.getItem('menus')
  if (menusData) {
    try {
      const menus = JSON.parse(menusData)
      // 过滤掉"数据查询"和"系统配置"菜单项（兼容旧数据）
      menuList.value = menus.filter(menu => 
        menu.code !== 'data-query' && 
        menu.path !== '/data-query' &&
        menu.code !== 'system-config' &&
        menu.path !== '/system-config'
      )
    } catch (e) {
      console.error('解析菜单数据失败:', e)
      menuList.value = []
    }
  }
}

// 处理通知（已由NotificationPanel组件处理）

// 菜单选择处理（确保路由跳转正常工作）
const handleMenuSelect = (index) => {
  if (index && router.currentRoute.value.path !== index) {
    router.push(index).catch(err => {
      console.error('路由跳转失败:', err)
    })
  }
}

// 下拉菜单命令处理
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'change-password':
      router.push('/system-config/change-password')
      break
    case 'system-settings':
      // 检查是否为admin
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          if (userInfo.username === 'admin') {
            router.push('/system-settings')
          } else {
            ElMessage.error('无权限访问系统设置页面')
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
      break
    case 'logout':
      await handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？退出后需要重新登录。',
      '退出确认',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('menus')

    ElMessage.success('已退出登录')

    // 跳转到登录页
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}

// 初始化
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.logo-icon {
  color: #667eea;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

.collapse-btn {
  color: rgba(255, 255, 255, 0.7);
  padding: 8px;
}

.collapse-btn:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
  height: 40px;
  line-height: 40px;
  background: transparent !important;
  color: rgba(255, 255, 255, 0.7) !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  font-weight: 500;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-header {
  height: 64px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  cursor: pointer;
  transition: all 0.3s;
}

.header-icon:hover {
  transform: scale(1.1);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f7fa;
}

.dropdown-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.dropdown-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
}

.dropdown-arrow {
  color: #86868b;
  font-size: 14px;
}

.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
  }

  .sidebar.mobile-open {
    transform: translateX(0);
  }

  .sidebar-collapsed {
    width: 240px;
  }

  .main-content {
    margin-left: 0;
  }

  .top-header {
    padding: 0 16px;
  }

  .content-area {
    padding: 16px;
  }

  .dropdown-name {
    display: none;
  }
}

/* 滚动条美化 */
.content-area::-webkit-scrollbar {
  width: 8px;
}

.content-area::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.content-area::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content-area::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Element Plus Menu 自定义样式 */
:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
}

:deep(.el-sub-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.7);
}

/* 子菜单项样式 - 强制覆盖 */
.sidebar-menu :deep(.el-sub-menu .el-menu) {
  background: transparent !important;
  border: none !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.7) !important;
  border: none !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  font-weight: 500;
}

:deep(.el-menu--popup) {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%) !important;
}

:deep(.el-menu--popup .el-menu-item) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-menu--popup .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white !important;
}
</style>
