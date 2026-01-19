# 手动创建 UniApp 项目步骤

## 🎯 快速步骤（5分钟）

### 方式1：使用命令行（推荐）

在项目根目录 `D:\AICoding\IOT\` 打开终端（PowerShell 或 CMD），执行：

```bash
# 1. 创建项目（手动交互）
vue create -p dcloudio/uni-preset-vue iot-mobile-app

# 执行时会提示：
# - 选择 Vue 3
# - 选择其他选项（使用默认即可）
# - 等待安装完成
```

**注意：** 如果提示选择 npm 镜像，选择 `Y` 使用国内镜像（更快）

---

### 方式2：使用 HBuilderX（最简单）

1. **下载 HBuilderX**
   - 访问：https://www.dcloud.io/hbuilderx.html
   - 下载标准版（解压即可用）

2. **创建项目**
   - 打开 HBuilderX
   - 文件 → 新建 → 项目
   - 选择 `uni-app` → `默认模板`
   - **重要：选择 Vue3 版本**
   - 项目名称：`iot-mobile-app`
   - 保存位置：`D:\AICoding\IOT\`

3. **项目创建完成**
   - 可以直接在 HBuilderX 中开发
   - 也可以导出到 VSCode/Cursor 中继续开发

---

### 方式3：使用 npm 直接创建（如果方式1失败）

```bash
# 在项目根目录
npm install -g @dcloudio/uvm
uvm install latest
uvm use latest

# 然后使用 uni-cli
npx @dcloudio/uni-cli create iot-mobile-app
```

---

## ✅ 创建成功后的验证

创建完成后，检查是否有以下文件：

```
iot-mobile-app/
├── pages/
├── App.vue
├── main.js
├── manifest.json
└── pages.json
```

如果有这些文件，说明创建成功！

---

## 🚀 下一步

创建成功后，继续执行：

```bash
cd iot-mobile-app
npm install  # 如果还没安装依赖

# 测试运行
npm run dev:h5
```

如果浏览器能打开并显示默认页面，说明环境配置正确！
