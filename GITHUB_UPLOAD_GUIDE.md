# GitHub 上传指南

本指南帮助您将IoT平台代码上传到GitHub，以便在服务器上部署。

## 前置准备

### 1. 安装 Git

**Windows:**
```bash
# 下载并安装 Git: https://git-scm.com/download/win
```

**macOS:**
```bash
# 使用 Homebrew 安装
brew install git
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install git
```

### 2. 创建 GitHub 账号

如果您还没有GitHub账号，请访问 https://github.com 注册。

## 上传步骤

### 第一步：在 GitHub 上创建仓库

1. 登录 GitHub: https://github.com
2. 点击右上角的 `+` 按钮，选择 `New repository`
3. 填写仓库信息：
   - **Repository name**: 输入仓库名称（如：`iot-platform`）
   - **Description**: 可选，填写仓库描述
   - **Public/Private**: 选择公开或私有
4. 点击 `Create repository` 按钮

### 第二步：在本地初始化 Git 仓库

在项目根目录（`e:/IOT`）执行以下命令：

```bash
# 1. 进入项目目录
cd /e/IOT

# 2. 初始化 Git 仓库
git init

# 3. 添加所有文件到暂存区
git add .

# 4. 提交更改
git commit -m "Initial commit: IoT Platform"
```

### 第三步：连接到 GitHub 仓库

```bash
# 添加远程仓库（替换 YOUR_USERNAME 和 REPO_NAME）
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# 示例：如果您的用户名是 myuser，仓库名是 iot-platform
# git remote add origin https://github.com/myuser/iot-platform.git
```

### 第四步：推送到 GitHub

```bash
# 推送到 main 分支
git push -u origin main
```

如果遇到错误，可能需要设置分支名称：

```bash
# 设置默认分支为 main
git branch -M main

# 再次推送
git push -u origin main
```

## 认证方式

### 方式一：使用 Personal Access Token（推荐）

1. 在 GitHub 上创建 Token：
   - 访问：https://github.com/settings/tokens
   - 点击 `Generate new token (classic)`
   - 勾选 `repo` 权限
   - 点击 `Generate token`
   - 复制生成的 token（只显示一次！）

2. 使用 Token 推送：
   ```bash
   # 推送时会提示输入密码
   # 用户名：您的 GitHub 用户名
   # 密码：粘贴刚才生成的 Token
   git push -u origin main
   ```

### 方式二：使用 SSH 密钥

1. 生成 SSH 密钥：
   ```bash
   ssh-keygen -t ed25519 -C "your_email@example.com"
   ```

2. 添加 SSH 密钥到 GitHub：
   - 复制公钥：`cat ~/.ssh/id_ed25519.pub`
   - 访问：https://github.com/settings/ssh/new
   - 粘贴公钥并保存

3. 使用 SSH 地址：
   ```bash
   # 修改远程仓库地址为 SSH
   git remote set-url origin git@github.com:YOUR_USERNAME/REPO_NAME.git
   
   # 推送
   git push -u origin main
   ```

## 常见问题

### 问题1：推送时提示认证失败

**解决**：使用 Personal Access Token 而不是密码

### 问题2：提示 "remote: Repository not found"

**解决**：检查仓库名称和用户名是否正确，确保仓库已创建

### 问题3：提示 "fatal: refusing to merge unrelated histories"

**解决**：
```bash
git pull origin main --allow-unrelated-histories
git push -u origin main
```

## 验证上传成功

推送完成后，访问您的GitHub仓库地址，应该能看到所有文件。

示例：https://github.com/YOUR_USERNAME/REPO_NAME

## 在服务器上克隆

上传成功后，在服务器上执行：

```bash
# 克隆仓库
git clone https://github.com/YOUR_USERNAME/REPO_NAME.git

# 进入项目目录
cd REPO_NAME

# 执行部署
chmod +x deploy.sh
./deploy.sh
```

## .gitignore 配置

如果还没有 `.gitignore` 文件，建议创建一个以排除不必要的文件：

```bash
# 创建 .gitignore 文件
cat > .gitignore << 'EOF'
# 依赖目录
node_modules/
target/

# 日志文件
*.log
logs/

# 数据目录
mysql-data/
emqx-data/
emqx-log/

# IDE 配置
.idea/
.vscode/
*.iml

# 系统文件
.DS_Store
Thumbs.db

# 备份文件
*.bak
*.backup
EOF

# 添加到 Git
git add .gitignore
git commit -m "Add .gitignore"
git push
```

## 下一步

代码上传成功后，您就可以：
1. 在任何服务器上克隆代码
2. 使用一键部署脚本快速部署
3. 方便团队协作开发

祝您上传顺利！🚀
