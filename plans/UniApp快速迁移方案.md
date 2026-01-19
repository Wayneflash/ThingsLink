# UniApp 快速迁移方案 - 最快构建安卓APP

## 🎯 目标
基于现有 Vue 3 代码，最快速度构建安卓 APP（预计 1-2 天可完成基础版本）

---

## 📦 第一步：安装必要的工具和依赖

### 1. 安装 HBuilderX（推荐，最简单）

**Windows 下载地址：**
```
https://www.dcloud.io/hbuilderx.html
```

**选择版本：** HBuilderX 正式版（最新版本）

**安装步骤：**
1. 下载后解压到任意目录（如 `D:\HBuilderX`）
2. 直接运行 `HBuilderX.exe`
3. 首次启动会提示安装插件，选择"是"

### 2. 或使用 CLI 方式（可选，适合命令行用户）

```bash
# 安装 Vue CLI
npm install -g @vue/cli

# 创建 UniApp 项目（Vue3版本）
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 进入项目目录
cd iot-mobile-app

# 安装依赖
npm install
```

---

## 🚀 第二步：创建 UniApp 项目结构

### 方式A：使用 HBuilderX（推荐）

1. **创建项目**
   - 打开 HBuilderX
   - 文件 → 新建 → 项目
   - 选择 `uni-app` → `默认模板` → **选择 Vue3 版本**
   - 项目名称：`iot-mobile-app`
   - 保存位置：建议放在项目根目录 `D:\AICoding\IOT\`

2. **项目目录结构**
   ```
   iot-mobile-app/
   ├── pages/              # 页面目录
   ├── components/         # 组件目录
   ├── static/            # 静态资源
   ├── api/               # API 接口（从现有项目复制）
   ├── utils/             # 工具函数
   ├── store/             # 状态管理（如需要）
   ├── App.vue            # 应用入口
   ├── main.js            # 入口文件
   ├── manifest.json      # 应用配置
   └── pages.json         # 页面路由配置
   ```

### 方式B：使用 CLI（命令行）

项目创建后会自动生成标准 UniApp 结构

---

## 📋 第三步：迁移现有代码

### 1. 复制 API 接口文件

**需要复制的文件：**
```
frontend/src/api/*  →  uni-app/api/
```

**修改点：**
- 将 `axios` 替换为 `uni.request`
- 将 `localStorage` 替换为 `uni.setStorageSync` / `uni.getStorageSync`
- 将 `router` 替换为 `uni.navigateTo`

**创建 `uni-app/utils/request.js`：**
```javascript
// 基于现有 request.js 修改
const baseURL = 'http://your-api-domain.com/api' // 替换为实际后端地址

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: baseURL + options.url,
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
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request
```

### 2. 创建页面（基于 MobilePrototypes.vue）

**需要创建的页面：**
```
pages/
├── login/
│   └── login.vue         # 登录页
├── overview/
│   └── overview.vue      # 概览页
├── device/
│   ├── list.vue          # 设备列表
│   └── detail.vue        # 设备详情
├── alarm/
│   └── alarm.vue         # 报警页
└── profile/
    └── profile.vue       # 我的
```

**参考现有原型：** 直接参考 `frontend/src/views/MobilePrototypes.vue` 的布局和样式

### 3. 配置路由（pages.json）

```json
{
  "pages": [
    {
      "path": "pages/login/login",
      "style": {
        "navigationBarTitleText": "登录"
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
  "tabBar": {
    "color": "#86868b",
    "selectedColor": "#667eea",
    "borderStyle": "black",
    "backgroundColor": "#ffffff",
    "list": [
      {
        "pagePath": "pages/overview/overview",
        "iconPath": "static/tabbar/home.png",
        "selectedIconPath": "static/tabbar/home-active.png",
        "text": "概览"
      },
      {
        "pagePath": "pages/device/list",
        "iconPath": "static/tabbar/device.png",
        "selectedIconPath": "static/tabbar/device-active.png",
        "text": "设备"
      },
      {
        "pagePath": "pages/alarm/alarm",
        "iconPath": "static/tabbar/alarm.png",
        "selectedIconPath": "static/tabbar/alarm-active.png",
        "text": "报警"
      },
      {
        "pagePath": "pages/profile/profile",
        "iconPath": "static/tabbar/profile.png",
        "selectedIconPath": "static/tabbar/profile-active.png",
        "text": "我的"
      }
    ]
  }
}
```

---

## 📦 第四步：安装必要的 UniApp 插件和依赖

### 1. 在 HBuilderX 中安装插件

**必须安装的插件：**
1. `uni-ui` - UniApp UI 组件库
   - 工具 → 插件安装 → 搜索 `uni-ui` → 安装

2. `uView UI`（可选，但推荐）
   - 更丰富的 UI 组件库
   - 工具 → 插件安装 → 搜索 `uView UI` → 安装

### 2. 使用 npm 安装依赖（如使用 CLI）

```bash
# 进入项目目录
cd iot-mobile-app

# 安装 uni-ui
npm install @dcloudio/uni-ui

# 安装 echarts（图表支持）
npm install echarts

# 如果需要状态管理
npm install pinia
```

---

## ⚙️ 第五步：配置 manifest.json

**关键配置：**

```json
{
  "name": "IoT平台",
  "appid": "__UNI__XXXXXX",
  "description": "物联网设备管理平台",
  "versionName": "1.0.0",
  "versionCode": "100",
  "transformPx": false,
  "app-plus": {
    "usingComponents": true,
    "nvueStyleCompiler": "uni-app",
    "compilerVersion": 3,
    "splashscreen": {
      "alwaysShowBeforeRender": true,
      "waiting": true,
      "autoclose": true,
      "delay": 0
    },
    "modules": {},
    "distribute": {
      "android": {
        "permissions": [
          "<uses-permission android:name=\"android.permission.INTERNET\"/>",
          "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\"/>"
        ],
        "abiFilters": ["armeabi-v7a", "arm64-v8a"]
      },
      "ios": {},
      "sdkConfigs": {}
    }
  },
  "h5": {
    "router": {
      "mode": "hash"
    }
  }
}
```

---

## 🏗️ 第六步：打包 APK

### 方式A：云打包（推荐，最简单）

1. **在 HBuilderX 中：**
   - 发行 → 原生App-云打包
   - 选择 Android
   - 填写应用名称、包名、版本号
   - 选择证书（首次需要生成）
   - 点击打包，等待完成
   - 下载 APK

2. **优点：**
   - 无需配置 Android 开发环境
   - 无需安装 Android SDK
   - 自动处理签名和优化

### 方式B：本地打包（需要 Android 环境）

需要安装：
- Android Studio
- Android SDK
- JDK

**步骤：**
1. 发行 → 原生App-本地打包
2. 按照提示配置环境
3. 生成 APK

---

## 📝 快速迁移检查清单

- [ ] 安装 HBuilderX 或创建 CLI 项目
- [ ] 复制 `frontend/src/api/*` 到 `uni-app/api/`
- [ ] 修改 `request.js` 使用 `uni.request`
- [ ] 创建基础页面结构（6-7个页面）
- [ ] 配置 `pages.json` 路由
- [ ] 配置 `manifest.json` 应用信息
- [ ] 替换 Element Plus 组件为 uni-ui
- [ ] 测试 API 调用是否正常
- [ ] 云打包生成 APK
- [ ] 真机测试

---

## 🎨 UI 组件替换对照表

| Element Plus | UniApp |
|-------------|--------|
| `<el-button>` | `<button>` 或 `<uni-button>` |
| `<el-input>` | `<input>` 或 `<uni-easyinput>` |
| `<el-select>` | `<picker>` 或 `<uni-data-picker>` |
| `<el-table>` | `<uni-list>` + 自定义 |
| `<el-card>` | `<view>` + 样式 |
| `<el-tag>` | `<uni-tag>` |
| `<el-icon>` | `<uni-icons>` |

---

## 🚨 常见问题

### 1. API 请求跨域问题
- 在 `manifest.json` 的 `h5` 中配置代理
- 或使用云打包，APP 中不会有跨域限制

### 2. 图标和图片
- 放到 `static/` 目录
- 使用绝对路径 `/static/xxx.png`

### 3. 调试方法
- HBuilderX：运行 → 运行到手机或模拟器
- 真机调试：连接手机，开启 USB 调试

---

## ⏱️ 预计时间

- **环境搭建：** 30分钟
- **代码迁移：** 4-6小时
- **页面适配：** 4-6小时
- **测试和打包：** 2-3小时
- **总计：** 1-2天

---

## 📞 下一步

完成基础迁移后，可以：
1. 优化 UI 细节
2. 添加原生功能（推送、扫码等）
3. 性能优化
4. 发布到应用商店
