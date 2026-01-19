# Capacitor 快速构建安卓 APP - 在现有编辑器中完成

## 🎯 优势
- ✅ **无需换编辑器**：直接在 VSCode/Cursor 中开发
- ✅ **最小改动**：基于现有 Vue 3 + Vite 项目
- ✅ **无需下载额外工具**：只需安装 npm 包
- ✅ **直接编译 APK**：可以使用云构建或本地编译

---

## 📦 第一步：安装 Capacitor

在 `frontend` 目录下执行：

```bash
cd frontend

# 安装 Capacitor 核心和 CLI
npm install @capacitor/core @capacitor/cli

# 安装 Android 平台支持
npm install @capacitor/android

# 初始化 Capacitor
npx cap init
```

**初始化时会询问：**
- App name: `IoT平台` 或 `IoT Platform`
- App ID: `com.yourcompany.iot`（反向域名格式）
- Web dir: `dist`（Vite 构建输出目录）

---

## 🚀 第二步：配置项目

### 1. 修改 `vite.config.js`

在 `vite.config.js` 中添加：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  // ... 现有配置 ...
  base: './', // 重要：确保资源路径正确
  build: {
    outDir: 'dist',
    // ... 现有配置 ...
  }
})
```

### 2. 创建 `capacitor.config.ts`（自动生成，可能需要调整）

```typescript
import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.yourcompany.iot',
  appName: 'IoT平台',
  webDir: 'dist',
  server: {
    androidScheme: 'https' // 或 'http'
  }
};

export default config;
```

---

## 📱 第三步：添加 Android 平台

```bash
# 添加 Android 平台
npx cap add android

# 同步构建产物到 Android 项目
npx cap sync
```

**这会：**
- 在项目根目录创建 `android/` 文件夹
- 将 `frontend/dist` 中的文件同步到 Android 项目

---

## 🏗️ 第四步：编译和打包

### 方式A：使用 Android Studio（推荐，可完全在编辑器外操作）

1. **安装 Android Studio**
   - 下载：https://developer.android.com/studio
   - 安装时选择 Android SDK

2. **打开项目**
   ```bash
   # 在 frontend 目录下
   npx cap open android
   ```
   - 这会自动打开 Android Studio

3. **在 Android Studio 中编译**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 等待编译完成
   - APK 位置：`android/app/build/outputs/apk/debug/app-debug.apk`

### 方式B：使用命令行编译（需要 Android SDK）

```bash
cd android
./gradlew assembleDebug
# APK 在 android/app/build/outputs/apk/debug/app-debug.apk
```

### 方式C：使用云构建服务（最简单，无需 Android Studio）

**选项1：Capacitor Cloud Build（需付费）**
- 但可以本地配置后提交构建

**选项2：GitHub Actions / GitLab CI（免费）**
- 配置自动构建流程

---

## 📝 第五步：适配现有代码

### 1. 修改 API 请求（如需要）

由于 Capacitor 运行在原生环境中，可能需要调整 API 地址：

```javascript
// utils/request.js
const baseURL = import.meta.env.DEV 
  ? '/api'  // 开发环境
  : 'https://your-api-domain.com/api'  // 生产环境
```

或使用环境变量：
```javascript
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
```

### 2. 处理本地存储

Capacitor 可以直接使用 `localStorage`，但推荐使用 `@capacitor/preferences`：

```bash
npm install @capacitor/preferences
```

```javascript
import { Preferences } from '@capacitor/preferences'

// 替代 localStorage.setItem
await Preferences.set({ key: 'token', value: 'xxx' })

// 替代 localStorage.getItem
const { value } = await Preferences.get({ key: 'token' })
```

### 3. 处理路由

Vue Router 可以直接使用，但需要确保使用 `hash` 模式（Capacitor 默认支持）：

```javascript
// router/index.js
const router = createRouter({
  history: createWebHashHistory(), // 使用 hash 模式
  routes
})
```

---

## 🔧 开发工作流

```bash
# 1. 开发前端（在 VSCode/Cursor 中）
cd frontend
npm run dev

# 2. 构建前端
npm run build

# 3. 同步到 Android 项目
npx cap sync

# 4. 打开 Android Studio 编译
npx cap open android
# 或使用命令行编译
cd android && ./gradlew assembleDebug
```

---

## 📦 完整安装命令（一键执行）

在 `frontend` 目录下执行：

```bash
# 安装 Capacitor
npm install @capacitor/core @capacitor/cli @capacitor/android @capacitor/preferences

# 初始化（按提示输入）
npx cap init

# 添加 Android 平台
npx cap add android

# 构建前端
npm run build

# 同步到 Android
npx cap sync
```

---

## 🎨 VSCode/Cursor 扩展推荐

虽然不是必需的，但可以安装这些扩展提高效率：

1. **Android Studio Integration**（可选）
   - 但主要还是在 Android Studio 中编译

2. **Vue Language Features (Volar)**
   - 你已经有了

3. **ESLint / Prettier**
   - 代码规范

---

## ⚡ 最快的方案总结

1. **安装 Capacitor**（5分钟）
   ```bash
   cd frontend
   npm install @capacitor/core @capacitor/cli @capacitor/android
   npx cap init
   npx cap add android
   ```

2. **构建并同步**（2分钟）
   ```bash
   npm run build
   npx cap sync
   ```

3. **编译 APK**
   - 安装 Android Studio（一次性，约30分钟）
   - 或使用云构建服务

4. **后续开发**：只需重复步骤2和3

---

## 🆚 Capacitor vs UniApp 对比

| 特性 | Capacitor | UniApp |
|------|-----------|--------|
| 编辑器支持 | ✅ VSCode/Cursor | ❌ 需要 HBuilderX |
| 代码改动 | ⭐⭐⭐ 最小 | ⭐⭐ 需要适配 |
| 编译方式 | Android Studio 或云构建 | HBuilderX 云打包 |
| 性能 | ⭐⭐⭐⭐ 原生性能 | ⭐⭐⭐ 良好 |
| 生态 | ⭐⭐⭐⭐ 丰富 | ⭐⭐⭐⭐ 丰富 |

---

## 🚨 常见问题

### Q: 必须安装 Android Studio 吗？
A: 不一定。可以使用：
- **云构建服务**（如 GitHub Actions）
- **在线构建平台**（如 EAS Build）
- 或者只在需要打包时临时安装

### Q: 可以完全在 VSCode 中编译吗？
A: 如果配置了命令行 Android SDK，可以使用 `gradlew` 命令编译，但首次仍需要 Android SDK。

### Q: 性能如何？
A: Capacitor 性能接近原生，比纯 WebView 好很多。

---

## 🎯 推荐方案

**最快路径：**
1. 在 VSCode/Cursor 中安装 Capacitor（5分钟）
2. 配置项目（10分钟）
3. 使用 **Capacitor Appflow 云构建**（免费试用）或 GitHub Actions 自动构建
4. 完全不需要 Android Studio！
