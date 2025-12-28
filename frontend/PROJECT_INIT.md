# IOT 物联网平台前端项目

## 项目初始化完成 ✅

### 技术栈
- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 新一代前端构建工具
- **Element Plus** - Vue 3 UI组件库
- **Tailwind CSS** - 原子化CSS框架
- **Axios** - HTTP客户端

### 项目结构
```
frontend/
├── src/
│   ├── api/                # API接口定义
│   │   ├── device.js      # 设备管理API ✅
│   │   └── index.js       # 其他API
│   ├── layouts/           # 布局组件
│   ├── router/            # 路由配置
│   │   └── index.js       # 路由定义（含登录守卫）
│   ├── styles/            # 样式文件
│   │   ├── common.css     # 通用样式 + Tailwind CSS ✅
│   │   └── element-theme.css
│   ├── utils/             # 工具函数
│   │   └── request.js     # Axios拦截器配置 ✅
│   ├── views/             # 页面组件
│   ├── App.vue
│   └── main.js            # 入口文件
├── tailwind.config.js     # Tailwind配置 ✅
├── postcss.config.js      # PostCSS配置 ✅
├── vite.config.js         # Vite配置
└── package.json
```

### 已完成的核心功能

#### 1. Axios请求拦截器 (`src/utils/request.js`) ✅

**请求拦截器功能：**
- 自动从 LocalStorage 获取 token
- 自动将 token 放入 Header 的 Authorization 字段

**响应拦截器功能：**
- 统一处理响应结构 `{code, data, message}`
- code !== 200 时，自动使用 ElMessage 报错
- code === 401 时，自动跳转登录页并清除 token
- 处理各种HTTP状态码错误（401、403、404、500等）
- 网络错误提示

#### 2. 设备管理API (`src/api/device.js`) ✅

包含以下接口（所有接口使用POST方法）：

| 函数名 | 接口路径 | 说明 |
|--------|---------|------|
| `getDeviceList` | POST /devices/list | 分页查询设备列表 |
| `createDevice` | POST /devices | 创建设备 |
| `getDeviceDetail` | POST /devices/detail | 获取设备详情 |
| `updateDevice` | POST /devices/update | 更新设备信息 |
| `deleteDevice` | POST /devices/delete | 删除设备 |
| `updateDeviceStatus` | POST /devices/{deviceCode}/status | 更新设备在线状态 |
| `getDeviceLatestData` | POST /devices/latest-data | 获取设备最新数据 |

**使用示例：**
```javascript
import { getDeviceList, createDevice, deleteDevice } from '@/api/device'

// 获取设备列表
const deviceList = await getDeviceList({
  page: 1,
  pageSize: 20,
  keyword: '温湿度',
  status: 'online'
})

// 创建设备
const newDevice = await createDevice({
  name: '温湿度传感器001',
  code: 'TH001-001',
  productId: 1,
  groupId: 1,
  remark: '办公室'
})

// 删除设备
await deleteDevice('TH001-001')
```

#### 3. Tailwind CSS 集成 ✅
- 已配置 `tailwind.config.js` 和 `postcss.config.js`
- Tailwind指令已添加到 `src/styles/common.css`
- 可以直接使用Tailwind的原子类

### API响应结构说明

所有接口统一响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  }
}
```

- `code === 200` 表示成功，其他值表示失败
- `code === 401` 会自动跳转登录页
- 响应拦截器已配置为直接返回 `data` 部分，无需手动解析

### 启动项目

```bash
# 进入项目目录
cd frontend

# 安装依赖（如未安装）
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 环境配置

**开发环境代理配置** (vite.config.js)：
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端服务地址
        changeOrigin: true
      }
    }
  }
}
```

### Token管理

**登录时存储token：**
```javascript
localStorage.setItem('token', 'your_token_here')
```

**登出时清除token：**
```javascript
localStorage.removeItem('token')
router.push('/login')
```

### 注意事项

1. ⚠️ **所有接口必须使用 POST 方法**，这是API文档的统一要求
2. ⚠️ request拦截器会自动添加token，无需在每个API调用中手动添加
3. ⚠️ 响应拦截器已配置为直接返回 `data` 部分，使用时无需 `response.data.data`
4. ⚠️ 错误处理已统一，无需在每个API调用中重复处理错误提示
5. ✅ 路由守卫已配置，未登录用户会自动跳转到登录页

### 下一步开发建议

1. 完善其他模块的API（用户管理、产品管理、数据管理等）
2. 开发具体的页面组件（设备列表、设备详情等）
3. 实现登录页面和token管理
4. 添加更多的业务逻辑和状态管理（如需要可使用Pinia）
5. 优化UI样式和用户体验

---

## 快速测试

你可以在任何组件中这样使用设备API：

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { getDeviceList } from '@/api/device'

const devices = ref([])

onMounted(async () => {
  try {
    const data = await getDeviceList({
      page: 1,
      pageSize: 20
    })
    devices.value = data.list
  } catch (error) {
    // 错误已被拦截器处理，这里可以做额外处理
    console.error('加载设备列表失败:', error)
  }
})
</script>
```
