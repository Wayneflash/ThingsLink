# APK 打包指南

## 🚀 快速打包（推荐方式）

### 方式一：DCloud 云打包（最简单，推荐）

**优点**：
- ✅ 无需配置 Android SDK
- ✅ 无需本地编译环境
- ✅ 在线打包，自动签名
- ✅ 支持自定义证书

**步骤**：

1. **注册 DCloud 账号**
   - 访问：https://dev.dcloud.net.cn/
   - 注册并登录账号

2. **创建应用**
   - 登录后，点击"创建应用"
   - 填写应用信息：
     - 应用名称：IoT平台
     - 应用标识：com.iot.platform（自定义）
     - 选择"5+ App"类型

3. **准备项目代码**
   ```bash
   # 进入项目目录
   cd iot-mobile-app
   
   # 确保依赖已安装
   npm install
   ```

4. **上传代码**
   - 在 DCloud 控制台，找到刚创建的应用
   - 点击"代码管理" → "上传代码"
   - 将 `iot-mobile-app` 整个目录打包成 ZIP（注意：不要包含 `node_modules`）
   - 上传 ZIP 文件

5. **配置打包参数**
   - 进入"发行" → "原生App-云打包"
   - 选择平台：**Android**
   - 配置参数：
     - **应用图标**：上传 192x192 的 PNG 图标
     - **启动图**：上传启动页图片
     - **应用包名**：`com.iot.platform`（必须与创建应用时的标识一致）
     - **应用版本名**：`1.0.0`
     - **应用版本号**：`100`
     - **打包模式**：选择"传统打包"或"uni-app原生渲染"
     - **证书**：首次打包可以使用 DCloud 默认证书（或上传自己的签名证书）

6. **开始打包**
   - 点击"打包"
   - 等待打包完成（通常 10-30 分钟）
   - 打包完成后，下载 APK 文件

---

## 🔧 方式二：本地打包编译 APK（推荐给有经验的开发者）

### 前提条件

需要安装以下工具：
1. **Java JDK 8+**
2. **Android Studio**（用于下载 Android SDK）
3. **Android SDK**（API Level 28+）
4. **Gradle**

### 方法 A：使用 HBuilderX 本地打包

#### 1. 安装 HBuilderX
- 下载地址：https://www.dcloud.io/hbuilderx.html
- 下载 **App 开发版**（完整版）

#### 2. 安装 Android Studio
- 下载地址：https://developer.android.com/studio
- 安装后，打开 Android Studio
- **SDK Manager** → 安装 Android SDK（API Level 28 或更高）
- 记录 SDK 路径（通常在 `C:\Users\用户名\AppData\Local\Android\Sdk`）

#### 3. 配置 HBuilderX
- 打开 HBuilderX
- 文件 → 打开目录 → 选择 `iot-mobile-app` 目录
- HBuilderX → 工具 → 选项 → SDK配置
- 配置 Android SDK 路径：
  - **Android SDK 路径**：填写你的 Android SDK 路径（如：`C:\Users\你的用户名\AppData\Local\Android\Sdk`）
  - **Android NDK 路径**：可选，如果不使用原生插件可以不填

#### 4. 配置 manifest.json
- 在 HBuilderX 中打开 `src/manifest.json`
- 可视化配置界面：
  - 应用名称：IoT平台
  - AppID：`__UNI__IOT_PLATFORM`
  - 版本号：1.0.0

#### 5. 本地打包
- 发行 → 原生App-本地打包 → 生成本地打包App资源
- 等待生成完成
- 会生成一个 `unpackage` 目录

#### 6. 使用 Android Studio 打包
- 使用 HBuilderX 生成的资源（需要 Android 原生工程模板）

---

### 方法 B：使用 CLI 构建（高级，需要更多配置）

#### 1. 安装 Android SDK
```bash
# 设置环境变量（Windows）
ANDROID_HOME=C:\Users\你的用户名\AppData\Local\Android\Sdk
PATH=%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools
```

#### 2. 构建 App 资源
```bash
cd iot-mobile-app
npm run build:app-plus
```

#### 3. 使用 Android Studio 编译 APK
- 这会生成 `unpackage/dist/build/app-plus` 目录
- 需要将其导入到 Android Studio 项目中进行编译

**注意**：CLI 构建后还需要 Android 原生工程才能生成 APK，比较复杂。

---

### 方法 C：使用 Android Studio + UniApp 原生插件（推荐）

这是最直接的方法，但需要一些设置：

1. **安装 Android Studio**
2. **下载 UniApp 原生插件模板**
   - 访问：https://nativesupport.dcloud.net.cn/
   - 下载 Android 原生插件模板

3. **导入项目**
   - 在 Android Studio 中打开原生插件项目
   - 将你的 UniApp 代码集成进去

**这种方法比较繁琐，不推荐新手使用。**

---

### 本地打包优缺点对比

**优点**：
- ✅ 打包速度快（无需等待云服务）
- ✅ 可离线打包
- ✅ 可以调试原生代码
- ✅ 不需要上传代码到云端

**缺点**：
- ❌ 需要配置复杂的 Android 开发环境
- ❌ 需要安装 JDK、Android SDK、Gradle 等
- ❌ 环境配置容易出错
- ❌ 占用本地磁盘空间较大（Android SDK 可能占用数 GB）

---

## 💡 推荐方案

**如果你是新手或想快速打包**：使用 **DCloud 云打包**（方式一）
- 最简单，无需配置环境
- 只需上传代码，等待打包完成

**如果你有 Android 开发经验**：可以使用 **HBuilderX 本地打包**（方式二方法A）
- 需要安装 HBuilderX 和 Android SDK
- 打包速度更快，可离线打包

**如果你需要频繁打包或调试原生代码**：使用 **本地打包** + Android Studio
- 需要完整的 Android 开发环境

---

### 方式三：使用 Android Studio 直接编译（高级）

**优点**：
- ✅ 可离线打包
- ✅ 打包速度快
- ✅ 可调试

**缺点**：
- ❌ 需要配置 Android 开发环境
- ❌ 需要 JDK、Android SDK、Gradle

**步骤**：

1. **下载 HBuilderX**
   - 访问：https://www.dcloud.io/hbuilderx.html
   - 下载 HBuilderX（建议下载 App 开发版）

2. **打开项目**
   - 打开 HBuilderX
   - 文件 → 打开目录 → 选择 `iot-mobile-app` 目录

3. **配置 manifest.json**
   - 双击 `src/manifest.json` 打开可视化配置
   - 检查应用信息：
     - 应用名称：IoT平台
     - AppID：`__UNI__IOT_PLATFORM`
     - 版本号：`1.0.0`

4. **发行打包**
   - 点击"发行" → "原生App-云打包"
   - 或者："发行" → "原生App-本地打包"（需要配置 Android 环境）

---

## ⚙️ 打包前检查清单

### 1. 代码检查
- [ ] 确认 `src/manifest.json` 中的应用信息正确
- [ ] 确认所有页面路由已配置在 `src/pages.json` 中
- [ ] 确认 API 地址配置正确（`src/utils/request.js`）

### 2. 配置检查
- [ ] 应用名称：IoT平台
- [ ] 应用包名：com.iot.platform
- [ ] 版本号：1.0.0
- [ ] 图标和启动图已准备

### 3. 功能测试
- [ ] 在 H5 环境下测试所有功能正常
- [ ] 测试登录功能
- [ ] 测试系统设置功能
- [ ] 测试主要业务页面

---

## 📦 打包后的配置

### 生产环境 API 地址

打包前，确保 `src/utils/request.js` 中的生产环境配置正确：

```javascript
// 生产环境：从本地存储读取服务器配置
// 默认地址：http://117.72.222.8:8080
```

用户首次使用时，需要在"系统设置"中配置服务器地址。

---

## 🔐 应用签名（可选）

### 使用自定义签名证书

1. **生成签名证书**
   ```bash
   # 使用 keytool 生成签名证书
   keytool -genkey -v -keystore iot-platform.keystore -alias iot-platform -keyalg RSA -keysize 2048 -validity 36500
   ```

2. **在云打包时上传证书**
   - 上传 `.keystore` 文件
   - 填写密钥库密码和密钥别名密码

---

## 📱 安装测试

### 安装到手机

1. **方法一：直接安装**
   - 将 APK 文件传输到 Android 手机
   - 在手机上打开 APK 文件
   - 允许"安装未知来源应用"
   - 点击安装

2. **方法二：ADB 安装**
   ```bash
   adb install iot-platform.apk
   ```

### 测试要点

- [ ] 应用能正常启动
- [ ] 登录功能正常
- [ ] 系统设置能正确保存服务器地址
- [ ] 所有页面能正常访问
- [ ] API 请求能正常发送

---

## 🐛 常见问题

### Q1: 打包失败，提示代码错误？
**A**: 
- 检查代码是否有语法错误
- 确保所有依赖已安装
- 检查 `pages.json` 配置是否正确

### Q2: 安装后应用闪退？
**A**: 
- 检查控制台日志
- 确认 API 地址配置正确
- 检查是否有未捕获的错误

### Q3: 网络请求失败？
**A**: 
- 确保手机网络正常
- 检查服务器地址和端口是否正确
- 确认服务器允许跨域访问（如果使用 H5+）

### Q4: 如何更新应用？
**A**: 
- 修改 `src/manifest.json` 中的版本号
- 重新打包
- 用户安装新版本时会覆盖旧版本（需要包名一致）

---

## 📞 技术支持

- **DCloud 官方文档**：https://uniapp.dcloud.net.cn/
- **DCloud 社区**：https://ask.dcloud.net.cn/
- **HBuilderX 下载**：https://www.dcloud.io/hbuilderx.html

---

## 🎯 快速开始

最简单的打包方式：

```bash
# 1. 进入项目目录
cd iot-mobile-app

# 2. 安装依赖（如果还没安装）
npm install

# 3. 测试 H5 版本
npm run dev:h5

# 4. 如果测试正常，前往 DCloud 云打包
# 访问：https://dev.dcloud.net.cn/
# 按照上面的步骤进行云打包
```

打包完成后，下载 APK 文件即可安装到手机上使用！
