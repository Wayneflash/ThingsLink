# 在 VSCode/Cursor 中开发 UniApp - 最佳方案

## 🎯 为什么选择 UniApp（考虑小程序扩展）

### 支持平台对比

| 平台 | Capacitor | UniApp |
|------|-----------|--------|
| Android | ✅ | ✅ |
| iOS | ✅ | ✅ |
| H5 | ✅ | ✅ |
| 微信小程序 | ❌ | ✅ |
| 支付宝小程序 | ❌ | ✅ |
| 百度小程序 | ❌ | ✅ |
| 字节跳动小程序 | ❌ | ✅ |
| QQ小程序 | ❌ | ✅ |
| 快手小程序 | ❌ | ✅ |

**结论：如果要支持小程序，UniApp 是唯一选择！**

---

## ✅ 好消息：UniApp 也可以在 VSCode/Cursor 中开发！

虽然 HBuilderX 更方便，但 **完全可以在 VSCode/Cursor 中开发 UniApp**，只需在打包时使用云打包即可。

---

## 📦 方案A：VSCode/Cursor 开发 + 云打包（推荐）

### 优点
- ✅ 在熟悉的编辑器中开发
- ✅ 完整的代码提示和调试支持
- ✅ 打包时使用云打包（无需本地环境）
- ✅ 一套代码支持所有平台

### 步骤

#### 1. 安装 VSCode 扩展（可选但推荐）

在 VSCode/Cursor 扩展市场搜索并安装：

1. **uni-app-schemas** - UniApp 语法提示
2. **uni-helper.uni-app-snippets** - UniApp 代码片段
3. **uni-helper.vscode-uni-app-schemas** - 类型提示

**安装命令：**
```bash
# 或者直接在扩展市场搜索：
# - uni-app
# - uni-helper
```

#### 2. 创建 UniApp 项目（使用 CLI，无需 HBuilderX）

```bash
# 安装 Vue CLI（如果没有）
npm install -g @vue/cli

# 创建 UniApp 项目（Vue3版本）
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 或使用 HBuilderX CLI（如果安装）
# npm install -g @dcloudio/uni-cli
# uni create -t default-vue3 iot-mobile-app
```

#### 3. 在 VSCode/Cursor 中开发

```bash
# 打开项目
cd iot-mobile-app
code . # 或直接在编辑器中打开
```

**项目结构：**
```
iot-mobile-app/
├── pages/              # 页面目录
├── components/         # 组件目录
├── static/            # 静态资源
├── api/               # API 接口
├── utils/             # 工具函数
├── App.vue            # 应用入口
├── main.js            # 入口文件
├── manifest.json      # 应用配置
└── pages.json         # 页面路由配置
```

#### 4. 开发调试

```bash
# H5 调试（浏览器）
npm run dev:h5

# 微信小程序（需要微信开发者工具）
npm run dev:mp-weixin

# 在 VSCode 中直接修改代码，保存即可热更新
```

#### 5. 打包 APK（使用云打包，无需 HBuilderX）

**方式1：使用 DCloud 在线打包（推荐）**
1. 访问：https://dev.dcloud.net.cn/
2. 登录 DCloud 账号
3. 创建应用 → 上传代码 → 云打包 → 选择 Android
4. 等待打包完成，下载 APK

**方式2：临时使用 HBuilderX 只打包（最小安装）**
- 只用于打包时打开一次
- 或让团队成员协助打包

---

## 📦 方案B：完全使用 CLI + GitHub Actions（最优雅）

### 优点
- ✅ 完全命令行，自动化程度高
- ✅ 可以使用 CI/CD 自动打包
- ✅ 团队协作方便

### 设置步骤

#### 1. 安装全局工具

```bash
# 安装 Vue CLI
npm install -g @vue/cli

# 或安装 uni-cli
npm install -g @dcloudio/uni-cli
```

#### 2. 配置 GitHub Actions 自动打包（可选）

创建 `.github/workflows/build-android.yml`：

```yaml
name: Build Android APK

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm install
      
      - name: Build H5
        run: npm run build:h5
      
      - name: Upload to DCloud
        # 使用 DCloud CLI 云打包
        run: |
          # 这里需要配置 DCloud API 密钥
          # 或使用其他云打包服务
```

---

## 🎨 推荐的开发工作流

### 日常开发（在 VSCode/Cursor 中）

```bash
# 1. 开发 H5 版本（浏览器调试，最快）
npm run dev:h5

# 2. 开发微信小程序（需要微信开发者工具）
npm run dev:mp-weixin

# 3. 所有代码在 VSCode/Cursor 中编写
# 4. 使用 Git 管理代码
```

### 打包发布（偶尔操作）

```bash
# 方案A：云打包（最简单）
# 访问 https://dev.dcloud.net.cn/ 上传代码打包

# 方案B：本地打包（需要 HBuilderX）
# 仅在需要时临时使用
```

---

## 🔧 VSCode/Cursor 插件推荐

### 必需插件

1. **uni-app**
   - 扩展ID: `DCloud.uni-app-schemas`
   - 提供语法提示和自动补全

2. **uni-helper.uni-app-snippets**
   - 提供代码片段

3. **Vue Language Features (Volar)**
   - 你已经有了

### 可选插件

4. **uni-app-schemas**
   - 更好的类型提示

5. **ESLint / Prettier**
   - 代码格式化

---

## 📝 代码迁移要点

### 1. API 请求封装

创建 `utils/request.js`：

```javascript
// uni-app 中使用 uni.request
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: 'https://your-api.com/api' + options.url,
      method: options.method || 'POST',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.data.code !== 200) {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          })
          return reject(new Error(res.data.message))
        }
        resolve(res.data.data)
      },
      fail: reject
    })
  })
}

export default request
```

### 2. 路由导航

```javascript
// 替代 router.push
uni.navigateTo({
  url: '/pages/device/detail?id=123'
})

// 替代 router.replace
uni.redirectTo({
  url: '/pages/login/login'
})
```

### 3. 组件替换

参考之前的组件对照表（在 UniApp快速迁移方案.md 中）

---

## 🚀 快速开始命令

```bash
# 1. 安装 Vue CLI
npm install -g @vue/cli

# 2. 创建项目
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 3. 进入项目
cd iot-mobile-app

# 4. 安装依赖
npm install

# 5. 安装 VSCode 扩展（在扩展市场搜索 "uni-app"）

# 6. 开发调试
npm run dev:h5  # H5 版本，浏览器打开

# 7. 打包（使用云打包或临时使用 HBuilderX）
```

---

## 🎯 最佳实践建议

### 开发阶段
- ✅ 在 VSCode/Cursor 中开发
- ✅ 使用 H5 版本快速调试
- ✅ 使用 Git 管理代码

### 测试阶段
- ✅ 真机调试（连接手机，HBuilderX 运行到手机）
- ✅ 小程序预览（微信开发者工具）

### 发布阶段
- ✅ Android/iOS：使用 DCloud 云打包
- ✅ 小程序：使用 HBuilderX 或 CLI 打包

---

## 💡 结论

**如果未来要做小程序，UniApp 是唯一正确选择！**

**推荐方案：**
1. **开发**：在 VSCode/Cursor 中开发（安装 uni-app 扩展）
2. **调试**：使用 H5 版本快速调试，真机/小程序真机调试
3. **打包**：使用 DCloud 云打包（无需本地 Android 环境）

**这样既能享受 VSCode 的开发体验，又能支持所有平台！**
