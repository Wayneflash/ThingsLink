# Git 提交规范指南

## ⚠️ 重要：确保代码同步

### 问题原因
昨天在其他电脑上提交时，可能提交了编译文件（`target/`、`node_modules/.vite/`），但没有提交源代码修改，导致代码不一致。

### 解决方案

#### 1. 提交前检查（必须执行）
```bash
# 检查是否有编译文件被修改
git status | grep -E "target/|node_modules/"

# 如果有编译文件，不要提交它们
# 只提交源代码文件（.java, .vue, .js, .ts 等）
```

#### 2. 正确的提交流程
```bash
# 1. 查看修改的文件
git status

# 2. 只添加源代码文件（不要添加编译文件）
git add frontend/src/
git add backend/src/
git add .gitignore
git add .cursorrules
git add init.sql
# ... 其他源代码文件

# 3. 提交
git commit -m "描述你的修改"

# 4. 推送到 GitHub
git push origin main
```

#### 3. 从另一台电脑拉取代码
```bash
# 1. 拉取最新代码
git pull origin main

# 2. 检查是否有编译文件冲突
git status

# 3. 如果有编译文件冲突，删除它们（编译文件会自动重新生成）
git clean -fd
```

### 被忽略的文件（不应提交）
- ✅ `backend/target/` - Maven 编译输出
- ✅ `frontend/node_modules/` - npm 依赖
- ✅ `frontend/node_modules/.vite/` - Vite 缓存
- ✅ `*.class` - Java 编译文件
- ✅ `*.jar` - Java 打包文件
- ✅ `*.log` - 日志文件

### 应该提交的文件
- ✅ `backend/src/` - Java 源代码
- ✅ `frontend/src/` - Vue 源代码
- ✅ `init.sql` - 数据库初始化脚本
- ✅ `.gitignore` - Git 忽略配置
- ✅ `.cursorrules` - 开发规范
- ✅ `README.md` - 项目说明

### 快速检查脚本
创建 `check-before-commit.bat`：
```batch
@echo off
echo 检查是否有编译文件被修改...
git status | findstr /C:"target/" /C:"node_modules/"
if %errorlevel% == 0 (
    echo [警告] 发现编译文件被修改，请检查！
    pause
) else (
    echo [通过] 没有编译文件被修改
    pause
)
```

### 如果已经提交了编译文件
```bash
# 1. 从 Git 中移除编译文件（保留本地文件）
git rm -r --cached backend/target/
git rm -r --cached frontend/node_modules/.vite/

# 2. 更新 .gitignore（确保已包含这些目录）
# 3. 提交删除操作
git add .gitignore
git commit -m "移除编译文件，更新 .gitignore"
git push origin main
```

---

**记住：只提交源代码，不提交编译文件！**
