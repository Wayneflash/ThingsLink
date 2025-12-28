# 母版布局系统使用指南

## 🎯 核心特性

### Axure 母版效果
- ✅ **固定导航栏**：顶部 Header 固定，不随页面滚动
- ✅ **固定侧边栏**：左侧菜单固定，独立滚动
- ✅ **一处修改全站生效**：只需修改 `MainLayout.vue` 即可控制全站布局
- ✅ **嵌套路由**：中间 `router-view` 容器展示业务内容
- ✅ **响应式设计**：使用 Tailwind CSS 适配不同屏幕

---

## 📂 文件结构

```
src/
├── layouts/
│   └── MainLayout.vue          # 母版布局（核心文件）
├── router/
│   └── index.js                # 路由配置（嵌套路由）
└── views/
    ├── Overview.vue            # 设备概览
    ├── DeviceList.vue          # 设备管理
    ├── AlarmLog.vue            # 报警日志
    ├── ProductList.vue         # 产品列表
    ├── ProductAttributes.vue   # 产品属性
    ├── DeviceGroups.vue        # 设备分组
    └── DataQuery.vue           # 数据查询
```

---

## 🎨 MainLayout.vue 结构

### 整体布局
```
┌────────────────────────────────────────────┐
│  固定侧边栏 (w-64)                          │
│  ├─ Logo 区域                              │
│  ├─ el-menu 导航菜单                      │
│  └─ 用户信息                              │
├───────────┬────────────────────────────────┤
│           │  固定顶部 Header               │
│           │  ├─ 页面标题 + 面包屑          │
│  (固定)    │  └─ 通知 + 用户下拉菜单        │
│           ├────────────────────────────────┤
│           │                                │
│           │  router-view 内容区域          │
│           │  (可滚动)                      │
│           │                                │
└───────────┴────────────────────────────────┘
```

### 关键区域说明

#### 1. 侧边栏（Sidebar）
```vue
<aside class="fixed left-0 top-0 h-screen w-64 bg-gray-900">
  <!-- Logo -->
  <div class="h-16">...</div>
  
  <!-- 导航菜单 -->
  <el-menu router>
    <el-menu-item index="/overview">设备概览</el-menu-item>
    <el-menu-item index="/devices">设备管理</el-menu-item>
    <el-menu-item index="/alarms">报警日志</el-menu-item>
    <!-- 更多菜单项 -->
  </el-menu>
  
  <!-- 用户信息 -->
  <div class="user-section">...</div>
</aside>
```

#### 2. 顶部导航栏（Header）
```vue
<header class="fixed top-0 right-0 left-64 h-16">
  <!-- 左侧：标题 + 面包屑 -->
  <div>
    <h1>{{ pageTitle }}</h1>
    <el-breadcrumb>...</el-breadcrumb>
  </div>
  
  <!-- 右侧：通知 + 用户菜单 -->
  <div>
    <el-badge>...</el-badge>
    <el-dropdown>...</el-dropdown>
  </div>
</header>
```

#### 3. 内容区域（Content）
```vue
<main class="flex-1 mt-16 p-6 overflow-y-auto">
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</main>
```

---

## 🛣️ 路由配置

### 嵌套路由结构
```javascript
const routes = [
  {
    path: '/',
    component: MainLayout,  // 母版布局
    redirect: '/overview',
    children: [
      {
        path: 'overview',
        component: () => import('../views/Overview.vue')
      },
      {
        path: 'devices',
        component: () => import('../views/DeviceList.vue')
      },
      // 更多子路由...
    ]
  }
]
```

### 添加新页面（3步搞定）

**步骤1：在 router/index.js 添加路由**
```javascript
children: [
  // ... 现有路由
  {
    path: 'my-new-page',
    name: 'MyNewPage',
    component: () => import('../views/MyNewPage.vue'),
    meta: { title: '我的新页面' }
  }
]
```

**步骤2：在 MainLayout.vue 添加菜单项**
```vue
<el-menu-item index="/my-new-page">
  <el-icon><Star /></el-icon>
  <span>我的新页面</span>
</el-menu-item>
```

**步骤3：创建页面组件**
```vue
<!-- src/views/MyNewPage.vue -->
<template>
  <div>
    <el-card>
      <template #header>
        <span class="text-lg font-semibold">我的新页面</span>
      </template>
      <!-- 页面内容 -->
    </el-card>
  </div>
</template>
```

---

## 🎯 核心功能

### 1. 菜单高亮
```javascript
// 自动根据当前路由高亮菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/products/')) {
    return path  // 子菜单项
  }
  return path
})
```

### 2. 动态页面标题
```javascript
// 根据路由自动显示标题
const pageTitleMap = {
  '/overview': '设备概览',
  '/devices': '设备管理',
  '/alarms': '报警日志',
  // ...
}

const pageTitle = computed(() => {
  return pageTitleMap[route.path] || 'IOT Platform'
})
```

### 3. 面包屑导航
```vue
<el-breadcrumb separator="/">
  <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
  <el-breadcrumb-item v-if="breadcrumb">
    {{ breadcrumb }}
  </el-breadcrumb-item>
</el-breadcrumb>
```

### 4. 用户下拉菜单
```vue
<el-dropdown @command="handleCommand">
  <el-avatar>...</el-avatar>
  <template #dropdown>
    <el-dropdown-menu>
      <el-dropdown-item command="profile">个人资料</el-dropdown-item>
      <el-dropdown-item command="settings">系统设置</el-dropdown-item>
      <el-dropdown-item command="logout">退出登录</el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
```

### 5. 页面切换动画
```vue
<router-view v-slot="{ Component }">
  <transition name="fade" mode="out-in">
    <component :is="Component" />
  </transition>
</router-view>
```

---

## 🎨 样式定制

### 修改侧边栏颜色
```vue
<!-- MainLayout.vue -->
<aside class="bg-gray-900">  <!-- 改为其他颜色 -->
```

### 修改菜单激活样式
```css
:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #3b82f6, #2563eb) !important;
  /* 修改为你想要的颜色 */
}
```

### 修改 Header 高度
```vue
<!-- 1. 修改高度 -->
<header class="h-16">  <!-- 改为 h-20 等 -->

<!-- 2. 同步修改内容区域的 margin-top -->
<main class="mt-16">  <!-- 改为 mt-20 等 -->
```

### 修改侧边栏宽度
```vue
<!-- 1. 侧边栏 -->
<aside class="w-64">  <!-- 改为 w-72 等 -->

<!-- 2. 主内容区 -->
<div class="ml-64">  <!-- 改为 ml-72 等 -->

<!-- 3. Header -->
<header class="left-64">  <!-- 改为 left-72 等 -->
```

---

## 📱 响应式设计

### 移动端适配
```css
@media (max-width: 768px) {
  /* 隐藏侧边栏 */
  aside {
    transform: translateX(-100%);
  }
  
  /* 移除主内容区的左边距 */
  .ml-64 {
    margin-left: 0 !important;
  }
  
  /* Header 占满宽度 */
  header {
    left: 0 !important;
  }
}
```

### Tailwind 响应式类
```vue
<!-- 移动端1列，平板2列，桌面4列 -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
  <!-- 内容 -->
</div>
```

---

## 🔧 常见修改场景

### 场景1：修改 Logo
```vue
<!-- MainLayout.vue Line 8-11 -->
<div class="flex items-center gap-3">
  <span class="text-3xl">🔗</span>  <!-- 改为你的 Logo -->
  <span class="text-xl font-bold text-white">IOT Platform</span>
</div>
```

### 场景2：添加子菜单
```vue
<el-sub-menu index="/products">
  <template #title>
    <el-icon><Box /></el-icon>
    <span>产品管理</span>
  </template>
  <el-menu-item index="/products/list">产品列表</el-menu-item>
  <el-menu-item index="/products/attributes">产品属性</el-menu-item>
  <!-- 添加更多子菜单 -->
  <el-menu-item index="/products/commands">产品命令</el-menu-item>
</el-sub-menu>
```

### 场景3：修改用户头像
```vue
<!-- 方式1：使用图片 -->
<el-avatar :src="userAvatar" />

<!-- 方式2：使用文字 -->
<el-avatar>{{ username.charAt(0) }}</el-avatar>

<!-- 方式3：使用图标 -->
<el-avatar>
  <el-icon><User /></el-icon>
</el-avatar>
```

### 场景4：添加通知功能
```javascript
// 获取通知数量
const notificationCount = ref(0)

// 加载通知
const loadNotifications = async () => {
  const data = await getNotifications()
  notificationCount.value = data.unreadCount
}

// 点击通知图标
const handleNotificationClick = () => {
  router.push('/notifications')
}
```

---

## 🚀 高级技巧

### 1. 菜单权限控制
```javascript
// 根据用户权限动态显示菜单
const userPermissions = ref(['device', 'alarm'])

const hasPermission = (permission) => {
  return userPermissions.value.includes(permission)
}
```

```vue
<el-menu-item v-if="hasPermission('device')" index="/devices">
  设备管理
</el-menu-item>
```

### 2. 菜单折叠功能
```javascript
const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}
```

```vue
<el-menu :collapse="isCollapse">
  <!-- 菜单项 -->
</el-menu>

<el-button @click="toggleCollapse">折叠</el-button>
```

### 3. 多标签页功能
```javascript
const tabs = ref([
  { name: '设备概览', path: '/overview' }
])

const addTab = (route) => {
  if (!tabs.value.find(t => t.path === route.path)) {
    tabs.value.push({
      name: route.meta.title,
      path: route.path
    })
  }
}
```

---

## ✅ 检查清单

- [ ] MainLayout.vue 布局是否正常显示
- [ ] 侧边栏菜单是否高亮正确
- [ ] 顶部标题是否动态更新
- [ ] 页面切换动画是否流畅
- [ ] 用户下拉菜单是否正常工作
- [ ] 退出登录功能是否正常
- [ ] 路由跳转是否正确
- [ ] 响应式设计是否适配移动端

---

## 📝 总结

母版布局系统已完成，核心优势：

1. ✅ **一处修改，全站生效** - 修改 MainLayout.vue 即可
2. ✅ **固定导航** - 侧边栏和顶栏固定不动
3. ✅ **嵌套路由** - router-view 展示业务内容
4. ✅ **Element Plus 菜单** - 功能完整的导航组件
5. ✅ **Tailwind CSS** - 响应式设计
6. ✅ **页面切换动画** - 流畅的用户体验

现在你可以专注于开发业务页面，布局系统会自动处理！🎉
