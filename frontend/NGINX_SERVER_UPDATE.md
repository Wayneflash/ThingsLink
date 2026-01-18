# 服务器 Nginx 配置更新指南

## 📋 检查当前 Nginx 配置

登录到服务器后，执行以下命令检查当前配置：

```bash
# 1. 检查 Nginx 配置文件位置
ls -la /etc/nginx/conf.d/
ls -la /etc/nginx/sites-available/

# 2. 查看当前配置文件（可能是其中一个）
cat /etc/nginx/conf.d/default.conf
# 或
cat /etc/nginx/sites-available/default

# 3. 检查 Nginx 服务状态
systemctl status nginx

# 4. 检查前端文件目录
ls -la /opt/iot-platform/frontend/dist/
# 或检查 web 根目录
ls -la /usr/share/nginx/html/
```

## 🔧 更新 Nginx 配置

### 步骤 1：备份当前配置

```bash
# 备份配置文件
sudo cp /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.backup.$(date +%Y%m%d)
# 或
sudo cp /etc/nginx/sites-available/default /etc/nginx/sites-available/default.backup.$(date +%Y%m%d)
```

### 步骤 2：更新配置文件

根据你的实际配置位置，选择其中一个：

#### 方案 A：使用 /etc/nginx/conf.d/default.conf

```bash
sudo vim /etc/nginx/conf.d/default.conf
```

#### 方案 B：使用 /etc/nginx/sites-available/default

```bash
sudo vim /etc/nginx/sites-available/default
```

### 步骤 3：应用新的配置

参考项目中的 `frontend/nginx.conf` 或 `frontend/DEPLOYMENT_GUIDE.md` 中的配置，确保包含以下缓存控制设置：

```nginx
server {
    listen 80;
    server_name localhost;  # 或你的域名/IP
    
    # 前端静态资源根目录（确认你的实际路径）
    root /usr/share/nginx/html;  # 或 /opt/iot-platform/frontend/dist
    index index.html;
    
    # 前端静态资源
    location / {
        try_files $uri $uri/ /index.html;
        
        # HTML 文件：不缓存，确保每次获取最新版本
        location ~* \.html$ {
            add_header Cache-Control "no-cache, no-store, must-revalidate";
            add_header Pragma "no-cache";
            add_header Expires "0";
        }
    }
    
    # JS/CSS 文件（带 hash）：长期缓存（1年）
    location ~* \.(js|css)$ {
        root /usr/share/nginx/html;  # 或你的实际路径
        add_header Cache-Control "public, max-age=31536000, immutable";
        expires 1y;
    }
    
    # 图片、字体等静态资源：中等缓存（1个月）
    location ~* \.(jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /usr/share/nginx/html;  # 或你的实际路径
        add_header Cache-Control "public, max-age=2592000";
        expires 1M;
    }
    
    # 后端 API 代理（确认你的后端地址）
    location /api/ {
        proxy_pass http://localhost:8080/;  # 确认后端地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 错误页面
    error_page 404 /index.html;
}
```

**注意：**
- `root` 路径需要根据你的实际部署路径调整
- `proxy_pass` 需要确认你的后端服务地址（可能是 `localhost:8080` 或其他）

### 步骤 4：测试配置

```bash
# 测试 Nginx 配置是否正确
sudo nginx -t
```

如果显示 `syntax is ok` 和 `test is successful`，说明配置正确。

### 步骤 5：重新加载 Nginx

```bash
# 重新加载配置（不中断服务）
sudo systemctl reload nginx

# 或重启 Nginx（会短暂中断服务）
sudo systemctl restart nginx
```

## ✅ 验证更新

### 1. 检查 Nginx 状态

```bash
sudo systemctl status nginx
```

### 2. 检查缓存头（可选）

```bash
# 检查 HTML 文件的缓存头
curl -I http://localhost/

# 检查 JS 文件的缓存头
curl -I http://localhost/assets/index-*.js
```

应该看到：
- HTML 文件：`Cache-Control: no-cache, no-store, must-revalidate`
- JS/CSS 文件：`Cache-Control: public, max-age=31536000, immutable`

## 🔍 快速检查命令

如果你只想快速查看当前配置，可以执行：

```bash
# 查看配置文件内容
cat /etc/nginx/conf.d/default.conf | grep -A 5 -B 5 "Cache-Control\|location"
```

## 📞 遇到问题

如果更新后出现问题：

```bash
# 恢复备份
sudo cp /etc/nginx/conf.d/default.conf.backup.* /etc/nginx/conf.d/default.conf
sudo nginx -t
sudo systemctl reload nginx
```

---

**提示：** 更新配置后，前端会在检测到新版本时自动刷新页面，无需手动清空浏览器缓存。