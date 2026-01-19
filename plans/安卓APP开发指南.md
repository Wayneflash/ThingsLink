# 安卓 APP 开发指南 - 基于 UniApp

## 🎯 目标
基于现有 Vue 3 项目，快速开发并打包安卓 APP

---

## 📋 第一步：环境准备（10分钟）

### 1. 安装 Vue CLI（如果还没有）

```bash
# 检查是否已安装
vue --version

# 如果没有，安装 Vue CLI
npm install -g @vue/cli
```

### 2. 安装 VSCode/Cursor 扩展（可选但推荐）

在扩展市场搜索并安装：
- **uni-app** - 语法提示和自动补全
- **uni-helper.uni-app-snippets** - 代码片段

或使用命令行：
```bash
# 在 VSCode/Cursor 中按 Ctrl+Shift+P
# 输入：Extensions: Install Extensions
# 搜索：uni-app
```

### 3. 注册 DCloud 账号（用于云打包）

访问：https://dev.dcloud.net.cn/
- 注册账号（免费）
- 后续用于云打包 APK

---

## 🚀 第二步：创建 UniApp 项目（5分钟）

在项目根目录 `D:\AICoding\IOT\` 执行：

```bash
# 创建 UniApp 项目（Vue3版本）
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 创建过程中的选择：
# - 选择 Vue 3
# - 其他选项保持默认
```

**项目创建成功后：**

```bash
# 进入项目目录
cd iot-mobile-app

# 查看项目结构
dir  # Windows
```

---

## 📁 第三步：项目结构说明

创建后的项目结构：

```
iot-mobile-app/
├── pages/              # 页面目录（在这里创建页面）
│   └── index/
│       └── index.vue   # 首页示例
├── static/            # 静态资源（图片、图标等）
├── components/        # 组件目录
├── App.vue            # 应用入口
├── main.js            # 入口文件
├── manifest.json      # 应用配置（APP名称、图标等）
├── pages.json         # 页面路由配置
└── uni.scss           # 全局样式
```

---

## 🔧 第四步：迁移现有代码

### 1. 创建目录结构

```bash
# 在 iot-mobile-app 目录下
mkdir api
mkdir utils
mkdir pages/login
mkdir pages/overview
mkdir pages/device
mkdir pages/alarm
mkdir pages/profile
```

### 2. 复制 API 文件

**从现有项目复制：**
```bash
# 复制所有 API 文件
copy frontend\src\api\*.* iot-mobile-app\api\
```

**需要修改：** 将 `axios` 改为 `uni.request`（见下一步）

### 3. 创建请求封装（重要！）

创建 `iot-mobile-app/utils/request.js`：

```javascript
// 替换现有的 axios 请求
const baseURL = 'http://your-api-domain.com/api' // 替换为实际后端地址

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: baseURL + (options.url || ''),
      method: options.method || 'POST',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        const data = res.data
        
        // 统一处理响应结构 {code, data, message}
        if (data.code !== 200) {
          if (data.code === 401) {
            // Token失效
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.showToast({
              title: data.message || '登录已过期',
              icon: 'none'
            })
            uni.reLaunch({
              url: '/pages/login/login'
            })
            return reject(new Error('登录已过期'))
          }
          uni.showToast({
            title: data.message || '请求失败',
            icon: 'none'
          })
          return reject(new Error(data.message || 'Error'))
        }
        
        resolve(data.data)
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误，请检查网络连接',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// 导出便捷方法
export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })

export default request
```

### 4. 修改 API 文件示例

以 `api/auth.js` 为例，修改方式：

**原来的代码（使用 axios）：**
```javascript
import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}
```

**修改后（使用 uni.request）：**
```javascript
import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'POST',
    data
  })
}
// 代码几乎不需要改动！只需要确保 request 使用的是 uni.request
```

---

## 📱 第五步：创建基础页面

### 1. 配置路由（pages.json）

```json
{
  "pages": [
    {
      "path": "pages/login/login",
      "style": {
        "navigationBarTitleText": "登录",
        "navigationStyle": "custom"
      }
    },
    {
      "path": "pages/overview/overview",
      "style": {
        "navigationBarTitleText": "概览"
      }
    },
    {
      "path": "pages/device/list",
      "style": {
        "navigationBarTitleText": "设备列表"
      }
    },
    {
      "path": "pages/device/detail",
      "style": {
        "navigationBarTitleText": "设备详情"
      }
    },
    {
      "path": "pages/alarm/alarm",
      "style": {
        "navigationBarTitleText": "报警"
      }
    },
    {
      "path": "pages/profile/profile",
      "style": {
        "navigationBarTitleText": "我的"
      }
    }
  ],
  "globalStyle": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "IoT平台",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f5f5f7"
  },
  "tabBar": {
    "color": "#86868b",
    "selectedColor": "#667eea",
    "borderStyle": "black",
    "backgroundColor": "#ffffff",
    "list": [
      {
        "pagePath": "pages/overview/overview",
        "text": "概览",
        "iconPath": "static/tabbar/home.png",
        "selectedIconPath": "static/tabbar/home-active.png"
      },
      {
        "pagePath": "pages/device/list",
        "text": "设备",
        "iconPath": "static/tabbar/device.png",
        "selectedIconPath": "static/tabbar/device-active.png"
      },
      {
        "pagePath": "pages/alarm/alarm",
        "text": "报警",
        "iconPath": "static/tabbar/alarm.png",
        "selectedIconPath": "static/tabbar/alarm-active.png"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的",
        "iconPath": "static/tabbar/profile.png",
        "selectedIconPath": "static/tabbar/profile-active.png"
      }
    ]
  }
}
```

### 2. 创建登录页（参考 MobilePrototypes.vue）

创建 `pages/login/login.vue`：

```vue
<template>
  <view class="login-page">
    <view class="login-content">
      <view class="logo-section">
        <view class="logo-icon">
          <text class="logo-text">🔌</text>
        </view>
        <text class="logo-title">ThingsLink</text>
        <text class="logo-subtitle">智慧物联网平台</text>
      </view>
      
      <view class="login-form">
        <view class="input-group">
          <input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            class="form-input"
          />
        </view>
        <view class="input-group">
          <input 
            v-model="loginForm.password" 
            type="password"
            placeholder="请输入密码" 
            class="form-input"
          />
        </view>
        
        <button class="login-button" @click="handleLogin">登录</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login } from '@/api/auth'

const loginForm = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await login(loginForm.value)
    uni.setStorageSync('token', res.token)
    uni.setStorageSync('userInfo', res.userInfo)
    
    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
    
    uni.switchTab({
      url: '/pages/overview/overview'
    })
  } catch (error) {
    uni.showToast({
      title: error.message || '登录失败',
      icon: 'none'
    })
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-content {
  width: 90%;
  max-width: 400px;
}

.logo-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.logo-icon {
  width: 120rpx;
  height: 120rpx;
  margin: 0 auto 24rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 60rpx;
}

.logo-title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8rpx;
}

.logo-subtitle {
  display: block;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-form {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
}

.input-group {
  margin-bottom: 32rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f5f5f7;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.login-button {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  margin-top: 24rpx;
}
</style>
```

### 3. 其他页面参考

**参考现有原型：**
- `frontend/src/views/MobilePrototypes.vue` 中已有完整的移动端页面布局
- 可以直接参考其中的结构和样式
- 需要将 Element Plus 组件替换为 uni-app 组件

---

## 🎨 第六步：组件替换对照

创建页面时，需要替换组件：

| Element Plus | UniApp 替换 |
|-------------|------------|
| `<el-button>` | `<button>` |
| `<el-input>` | `<input>` |
| `<el-select>` | `<picker>` |
| `<el-table>` | `<view>` + 自定义列表 |
| `<el-card>` | `<view class="card">` |
| `<el-tag>` | `<text class="tag">` |
| `<el-icon>` | `<text>` 或图标字体 |

---

## 🔍 第七步：开发调试

### H5 调试（最快，浏览器）

```bash
cd iot-mobile-app
npm run dev:h5
```

- 自动打开浏览器
- 支持热更新
- 适合快速开发调试

### 真机调试（推荐）

**方式1：使用 HBuilderX（临时安装，仅用于调试）**
1. 下载 HBuilderX（最小安装）
2. 打开项目：文件 → 打开目录 → 选择 `iot-mobile-app`
3. 运行 → 运行到手机或模拟器 → 选择 Android

**方式2：USB 调试**
1. 手机开启 USB 调试
2. 连接电脑
3. 运行：`npm run dev:app-android`（需要配置 Android 环境）

**推荐：先用 H5 版本开发，基本完成后真机测试**

---

## 📦 第八步：配置 APP 信息

### 修改 manifest.json

```json
{
  "name": "IoT平台",
  "appid": "__UNI__XXXXXX",
  "description": "物联网设备管理平台",
  "versionName": "1.0.0",
  "versionCode": "100",
  "app-plus": {
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.INTERNET\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\"/>"
        ],
        "abiFilters": ["armeabi-v7a", "arm64-v8a"]
      }
    }
  }
}
```

---

## 🏗️ 第九步：打包 APK

### 方式A：DCloud 云打包（推荐，最简单）

1. **访问 DCloud 控制台**
   - https://dev.dcloud.net.cn/

2. **创建应用**
   - 我的项目 → 创建 → 选择"uni-app" → 填写应用名称

3. **上传代码**
   - 将 `iot-mobile-app` 目录压缩为 zip
   - 上传到 DCloud 控制台

4. **云打包**
   - 发行 → 原生App-云打包
   - 选择 Android
   - 填写应用信息（名称、包名、版本号）
   - 选择证书（首次需要生成）
   - 点击打包

5. **下载 APK**
   - 等待打包完成（通常 5-10 分钟）
   - 下载 APK 文件

### 方式B：本地打包（需要 Android Studio）

1. 安装 Android Studio
2. 配置 Android SDK
3. 在 HBuilderX 中：发行 → 原生App-本地打包

**推荐使用云打包，无需本地环境！**

---

## ✅ 开发检查清单

- [ ] 安装 Vue CLI
- [ ] 创建 UniApp 项目
- [ ] 安装 VSCode 扩展（可选）
- [ ] 创建 API 目录并复制文件
- [ ] 创建 `utils/request.js`（使用 uni.request）
- [ ] 修改 API 文件引用
- [ ] 配置 `pages.json` 路由
- [ ] 创建登录页
- [ ] 创建概览页
- [ ] 创建设备列表页
- [ ] 创建其他页面
- [ ] H5 调试测试
- [ ] 真机调试测试
- [ ] 配置 `manifest.json`
- [ ] 云打包生成 APK
- [ ] 安装测试 APK

---

## 🚀 快速命令汇总

```bash
# 1. 创建项目
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 2. 进入项目
cd iot-mobile-app

# 3. 安装依赖（如果还没有）
npm install

# 4. H5 开发调试
npm run dev:h5

# 5. 构建 H5（用于云打包）
npm run build:h5

# 6. 打包 APK（使用云打包，见上方步骤）
```

---

## 📚 参考资源

1. **UniApp 官方文档**：https://uniapp.dcloud.net.cn/
2. **现有移动端原型**：`frontend/src/views/MobilePrototypes.vue`
3. **API 文档**：`docs/API.md`

---

## 🎯 下一步

完成基础框架后，可以：
1. 逐个页面完善功能
2. 优化 UI 细节
3. 添加原生功能（推送、扫码等）
4. 性能优化
5. 发布到应用商店
