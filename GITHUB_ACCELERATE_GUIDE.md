# GitHub 加速指南

本指南提供多种方法来加速GitHub访问和下载，特别是在中国大陆地区。

## 方法一：使用 GitHub 镜像站点（推荐）

### 1. Gitee（码云）镜像

Gitee提供了GitHub仓库的镜像，速度非常快。

**使用方法：**

```bash
# 方法1：直接从Gitee克隆（如果仓库已同步）
git clone https://gitee.com/YOUR_USERNAME/YOUR_REPO.git

# 方法2：导入到Gitee
# 访问 https://gitee.com/
# 点击右上角 "+" -> "从GitHub/GitLab导入仓库"
# 粘贴GitHub仓库地址，点击导入
```

### 2. FastGit 镜像

```bash
# 使用FastGit克隆
git clone https://hub.fastgit.xyz/YOUR_USERNAME/YOUR_REPO.git
```

### 3. GitClone 镜像

```bash
# 使用GitClone克隆
git clone https://gitclone.com/github.com/YOUR_USERNAME/YOUR_REPO.git
```

### 4. GitHub 镜像加速列表

| 镜像站点 | 地址 | 说明 |
|---------|------|------|
| Gitee | https://gitee.com | 国内最快的镜像 |
| FastGit | https://hub.fastgit.xyz | 支持GitHub和GitLab |
| GitClone | https://gitclone.com | 支持GitHub、GitLab、Gitee |
| cnpmjs | https://github.com.cnpmjs.org | GitHub文件下载加速 |

## 方法二：配置 Git 使用镜像

### 1. 临时使用镜像

```bash
# 克隆时直接使用镜像地址
git clone https://gitee.com/YOUR_USERNAME/YOUR_REPO.git
```

### 2. 永久配置镜像（修改Git配置）

```bash
# 方法1：使用Git的URL重写功能
git config --global url."https://github.com/".insteadof "https://gitee.com/"

# 这样之后，所有github.com的请求都会自动重定向到gitee.com
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
# 实际会从 gitee.com 克隆
```

### 3. 针对特定仓库配置镜像

```bash
# 只对特定仓库使用镜像
git config --global url."https://github.com/Wayneflash/".insteadof "https://gitee.com/Wayneflash/"

# 然后正常克隆
git clone https://github.com/Wayneflash/ThingsLink.git
# 实际会从 gitee.com/Wayneflash/ThingsLink.git 克隆
```

## 方法三：使用代理/VPN

如果您有可用的代理或VPN：

### 1. 配置 Git 使用 HTTP 代理

```bash
# 设置HTTP代理
git config --global http.proxy http://127.0.0.1:7890
git config --global https.proxy https://127.0.0.1:7890

# 取消代理
git config --global --unset http.proxy
git config --global --unset https.proxy
```

### 2. 配置 Git 使用 SOCKS 代理

```bash
# 设置SOCKS代理
git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080
```

## 方法四：修改 hosts 文件（加速域名解析）

### 1. 编辑 hosts 文件

**Windows:**
```
C:\Windows\System32\drivers\etc\hosts
```

**Linux/macOS:**
```bash
sudo nano /etc/hosts
```

### 2. 添加以下内容

```
# GitHub 加速
140.82.112.18 github.com
140.82.112.19 github.global.ssl.fastly.net
199.232.96.133 assets-cdn.github.com
199.232.96.133 raw.githubusercontent.com
199.232.96.133 gist.githubusercontent.com
199.232.96.133 cloud.githubusercontent.com
199.232.96.133 camo.githubusercontent.com
199.232.96.133 avatars.githubusercontent.com
199.232.96.133 avatars0.githubusercontent.com
199.232.96.133 avatars1.githubusercontent.com
199.232.96.133 avatars2.githubusercontent.com
199.232.96.133 avatars3.githubusercontent.com
199.232.96.133 avatars4.githubusercontent.com
199.232.96.133 avatars5.githubusercontent.com
199.232.96.133 avatars6.githubusercontent.com
199.232.96.133 avatars7.githubusercontent.com
199.232.96.133 avatars8.githubusercontent.com
199.232.96.133 avatars9.githubusercontent.com
```

### 3. 刷新DNS缓存

**Windows:**
```cmd
ipconfig /flushdns
```

**Linux:**
```bash
sudo systemd-resolve --flush-caches
# 或
sudo /etc/init.d/nscd restart
```

**macOS:**
```bash
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

## 方法五：使用 GitHub 加速工具

### 1. GitHub 加速器

访问以下网站获取加速地址：
- https://ghproxy.com/
- https://github.com.cnpmjs.org/
- https://mirror.ghproxy.com/

### 2. 使用加速下载工具

**使用 wget 加速：**
```bash
# 下载文件时使用加速镜像
wget https://ghproxy.com/https://github.com/YOUR_USERNAME/YOUR_REPO/archive/main.zip
```

**使用 curl 加速：**
```bash
# 下载文件时使用加速镜像
curl -L https://ghproxy.com/https://github.com/YOUR_USERNAME/YOUR_REPO/archive/main.zip -o main.zip
```

## 方法六：更新部署脚本支持镜像

修改 [`deploy.sh`](deploy.sh:1) 脚本，添加镜像选择：

```bash
# 在脚本开头添加镜像选择
echo "请选择下载源："
echo "1) GitHub (官方源)"
echo "2) Gitee (国内镜像，推荐)"
echo "3) FastGit (加速镜像)"
read -p "请输入选项 [1-3]: " mirror_choice

case $mirror_choice in
    1)
        REPO_URL="https://github.com/Wayneflash/ThingsLink.git"
        ;;
    2)
        REPO_URL="https://gitee.com/Wayneflash/ThingsLink.git"
        ;;
    3)
        REPO_URL="https://hub.fastgit.xyz/Wayneflash/ThingsLink.git"
        ;;
    *)
        echo "无效选项，使用默认GitHub源"
        REPO_URL="https://github.com/Wayneflash/ThingsLink.git"
        ;;
esac

# 使用选择的镜像克隆
git clone $REPO_URL
```

## 推荐方案

### 对于服务器部署

**方案1：使用Gitee镜像（最简单）**

```bash
# 1. 在Gitee上导入GitHub仓库
# 访问 https://gitee.com/
# 点击 "+" -> "从GitHub/GitLab导入仓库"
# 粘贴：https://github.com/Wayneflash/ThingsLink.git

# 2. 从Gitee克隆
git clone https://gitee.com/Wayneflash/ThingsLink.git
cd ThingsLink
chmod +x deploy.sh
./deploy.sh
```

**方案2：使用FastGit镜像**

```bash
# 直接从FastGit克隆
git clone https://hub.fastgit.xyz/Wayneflash/ThingsLink.git
cd ThingsLink
chmod +x deploy.sh
./deploy.sh
```

### 对于本地开发

**方案1：配置全局镜像**

```bash
# 配置所有GitHub请求使用Gitee镜像
git config --global url."https://github.com/".insteadof "https://gitee.com/"

# 之后所有git clone github.com都会自动使用gitee.com
```

**方案2：使用 hosts 文件加速**

编辑 `/etc/hosts`（Linux/macOS）或 `C:\Windows\System32\drivers\etc\hosts`（Windows），添加上面的hosts配置。

## 验证加速效果

### 测试下载速度

```bash
# 测试GitHub速度
time git clone https://github.com/Wayneflash/ThingsLink.git test-github

# 测试Gitee速度
time git clone https://gitee.com/Wayneflash/ThingsLink.git test-gitee

# 测试FastGit速度
time git clone https://hub.fastgit.xyz/Wayneflash/ThingsLink.git test-fastgit

# 对比时间，选择最快的
```

## 常见问题

### 问题1：镜像同步延迟

**现象**：Gitee镜像可能有几分钟的同步延迟

**解决**：
- 如果需要最新代码，使用GitHub官方源
- 如果不着急，使用Gitee镜像（速度快）

### 问题2：镜像不支持某些功能

**现象**：某些镜像可能不支持GitHub Actions、Pages等功能

**解决**：
- 对于需要GitHub Actions的项目，使用官方源
- 对于普通代码下载，使用镜像

### 问题3：hosts配置不生效

**解决**：
- 清除浏览器和DNS缓存
- 重启网络服务
- 检查hosts文件格式是否正确

## 最佳实践

1. **服务器部署**：优先使用Gitee或FastGit镜像
2. **本地开发**：配置全局Git镜像或修改hosts文件
3. **需要最新代码**：使用GitHub官方源+代理
4. **团队协作**：在README中说明可用的镜像地址

## 相关资源

- Gitee: https://gitee.com/
- FastGit: https://hub.fastgit.xyz/
- GitClone: https://gitclone.com/
- GitHub加速: https://ghproxy.com/
- hosts文件生成: https://github.com/ineo6/hosts/raw/master/hosts

选择适合您的方法，享受快速的GitHub访问！🚀
