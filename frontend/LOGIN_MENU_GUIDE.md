# 登录菜单权限使用指南

## 🎯 核心改进

### 问题
之前需要额外的接口去获取用户菜单权限，增加了请求次数，降低了性能。

### 解决方案 ✅
**登录接口直接返回菜单权限**，一次请求完成所有初始化！

---

## 📡 后端更新

### 1. UserService.java
新增 `getUserMenus()` 方法，根据用户角色动态生成菜单树：

```java
// 超级管理员拥有所有菜单
if (isSuperAdmin) {
    menus.add(createMenu("overview", "设备概览", "/overview", "DataAnalysis", null, 1));
    menus.add(createMenu("devices", "设备管理", "/devices", "Monitor", null, 2));
    // ... 更多菜单
}

// 普通角色根据权限返回菜单
if (permissions.contains("device")) {
    menus.add(createMenu("devices", "设备管理", "/devices", "Monitor", null, 2));
}
```

### 2. 登录接口响应结构

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "a1b2c3d4...",
    "refreshToken": "refresh_a1b2c3d4...",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "超级管理员",
      "roleId": 1,
      "status": 1
    },
    "menus": [
      {
        "code": "overview",
        "name": "设备概览",
        "path": "/overview",
        "icon": "DataAnalysis",
        "sort": 1,
        "children": null
      },
      {
        "code": "products",
        "name": "产品管理",
        "path": "/products",
        "icon": "Box",
        "sort": 4,
        "children": [
          {
            "code": "products-list",
            "name": "产品列表",
            "path": "/products/list",
            "icon": null,
            "sort": 1,
            "children": null
          }
        ]
      }
    ]
  }
}
```

---

## 🎨 前端实现

### 1. 登录页面（Login.vue）

```vue
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const loginForm = ref({
  username: 'admin',
  password: 'admin123456'
})

const handleLogin = async () => {
  loading.value = true
  try {
    // 调用登录接口
    const data = await login(loginForm.value)
    
    // 存储 token
    localStorage.setItem('token', data.token)
    
    // 存储用户信息
    localStorage.setItem('userInfo', JSON.stringify(data.user))
    
    // 🔥 关键：存储菜单数据
    localStorage.setItem('menus', JSON.stringify(data.menus))
    
    ElMessage.success('登录成功')
    
    // 跳转到首页
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>
```

### 2. MainLayout.vue（动态菜单）

```vue
<template>
  <el-menu router>
    <!-- 动态生成菜单 -->
    <template v-for="menu in menus" :key="menu.code">
      <!-- 无子菜单 -->
      <el-menu-item v-if="!menu.children" :index="menu.path">
        <el-icon v-if="menu.icon">
          <component :is="menu.icon" />
        </el-icon>
        <span>{{ menu.name }}</span>
      </el-menu-item>

      <!-- 有子菜单 -->
      <el-sub-menu v-else :index="menu.path">
        <template #title>
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </template>
        <el-menu-item
          v-for="child in menu.children"
          :key="child.code"
          :index="child.path"
        >
          {{ child.name }}
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 菜单数据
const menus = ref([])

// 从 LocalStorage 加载菜单
onMounted(() => {
  const menusData = JSON.parse(localStorage.getItem('menus') || '[]')
  menus.value = menusData
})
</script>
```

---

## 🔄 完整流程

### 步骤1：用户登录
```javascript
// 调用登录接口
const data = await login({ username: 'admin', password: '123456' })

// 后端返回：
// {
//   token: "xxx",
//   user: {...},
//   menus: [...]  // 🔥 菜单权限
// }
```

### 步骤2：存储数据
```javascript
localStorage.setItem('token', data.token)
localStorage.setItem('userInfo', JSON.stringify(data.user))
localStorage.setItem('menus', JSON.stringify(data.menus))  // 🔥 存储菜单
```

### 步骤3：动态渲染侧边栏
```vue
<!-- MainLayout.vue 会自动读取 menus 并渲染 -->
<el-menu>
  <template v-for="menu in menus">
    <!-- 根据 menu.children 判断是否有子菜单 -->
  </template>
</el-menu>
```

### 步骤4：退出登录
```javascript
// 清除所有数据
localStorage.removeItem('token')
localStorage.removeItem('userInfo')
localStorage.removeItem('menus')  // 🔥 清除菜单
```

---

## 🎯 权限控制示例

### 超级管理员（super_admin）
```json
{
  "menus": [
    {"code": "overview", "name": "设备概览"},
    {"code": "devices", "name": "设备管理"},
    {"code": "alarms", "name": "报警日志"},
    {"code": "products", "name": "产品管理"},
    {"code": "groups", "name": "设备分组"},
    {"code": "data-query", "name": "数据查询"},
    {
      "code": "system",
      "name": "系统管理",
      "children": [
        {"code": "users", "name": "用户管理"},
        {"code": "roles", "name": "角色管理"}
      ]
    }
  ]
}
```

### 设备管理员
```json
{
  "menus": [
    {"code": "overview", "name": "设备概览"},
    {"code": "devices", "name": "设备管理"},
    {"code": "alarms", "name": "报警日志"}
  ]
}
```

### 只读用户
```json
{
  "menus": [
    {"code": "overview", "name": "设备概览"},
    {"code": "data-query", "name": "数据查询"}
  ]
}
```

---

## 📝 菜单数据结构

```typescript
interface Menu {
  code: string        // 菜单唯一标识
  name: string        // 菜单名称（显示文本）
  path: string        // 路由路径
  icon: string | null // Element Plus 图标名称
  sort: number        // 排序
  children: Menu[] | null  // 子菜单（null 表示无子菜单）
}
```

### 图标映射
| icon 值 | Element Plus 图标 |
|---------|-------------------|
| DataAnalysis | `<DataAnalysis />` |
| Monitor | `<Monitor />` |
| BellFilled | `<BellFilled />` |
| Box | `<Box />` |
| FolderOpened | `<FolderOpened />` |
| TrendCharts | `<TrendCharts />` |
| Setting | `<Setting />` |

---

## ✅ 优势总结

### 1. 性能提升
- ❌ 旧方案：登录 → 获取用户信息 → 获取菜单权限（3次请求）
- ✅ 新方案：登录（1次请求，返回所有数据）

### 2. 代码简洁
- 前端无需额外调用获取菜单接口
- 登录后直接可用，无需等待

### 3. 权限控制
- 基于角色的菜单权限（RBAC）
- 超级管理员自动拥有所有菜单
- 普通角色根据权限动态生成

### 4. 用户体验
- 登录更快，菜单立即显示
- 不同角色看到不同菜单
- 无权限的菜单直接不显示

---

## 🔒 安全说明

1. **菜单权限只是UI层面的控制**
   - 隐藏菜单 ≠ 禁止访问接口
   - 后端必须有接口级别的权限验证

2. **Token验证**
   - 所有接口调用都需要验证Token
   - Token失效自动跳转登录页

3. **前端路由守卫**
   - 未登录访问受保护页面 → 跳转登录
   - 已登录访问登录页 → 跳转首页

---

## 🎉 使用效果

用户登录后，侧边栏会根据其角色权限自动显示对应的菜单，无需额外配置，真正实现了"一次登录，全部就绪"！

**超级管理员看到：** 所有菜单  
**设备管理员看到：** 设备相关菜单  
**只读用户看到：** 查询相关菜单

完美实现基于角色的菜单权限控制！🚀
