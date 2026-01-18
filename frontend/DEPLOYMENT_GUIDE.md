# 前端部署指南

## 📋 部署前检查清单

### 1. 确保构建文件完整
```bash
# 在前端目录执行构建
cd frontend
npm install
npm run build

# 检查 dist 目录是否包含以下文件：
# - index.html
# - assets/ (目录，包含 .js 和 .css 文件)
# - icons/ (目录，包含 SVG 图标)
```

### 2. 上传到服务器的文件
只需要上传 `dist` 目录的内容（不是 dist 目录本身）到服务器的 web 根目录。

```
服务器目录结构应该是：
/usr/share/nginx/html/  (或你的 web 根目录)
├── index.html
├── assets/
│   ├── *.js
│   └── *.css
└── icons/
    └── industrial/
        └── *.svg
```

---

## 🔧 Nginx 配置

### 标准配置（根目录部署）

在 `/etc/nginx/conf.d/default.conf` 或 `/etc/nginx/sites-available/default` 中配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 改为你的域名或 IP
    
    # 前端静态资源根目录
    root /usr/share/nginx/html;  # 改为你的实际路径
    index index.html;
    
    # 前端静态资源 - 必须配置 try_files 支持 Vue Router 的 history 模式
    location / {
        try_files $uri $uri/ /index.html;
        
        # HTML 文件：不缓存，确保每次获取最新版本（支持自动版本更新）
        location ~* \.html$ {
            add_header Cache-Control "no-cache, no-store, must-revalidate";
            add_header Pragma "no-cache";
            add_header Expires "0";
        }
    }
    
    # JS/CSS 文件（带 hash）：长期缓存（1年），因为文件名包含 hash，内容变化时文件名会变
    location ~* \.(js|css)$ {
        add_header Cache-Control "public, max-age=31536000, immutable";
        expires 1y;
    }
    
    # 图片、字体等静态资源：中等缓存（1个月）
    location ~* \.(jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
        add_header Cache-Control "public, max-age=2592000";
        expires 1M;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8080/;  # 改为你的后端地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS 支持（如果后端没有配置）
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS';
        add_header Access-Control-Allow-Headers 'Authorization, Content-Type';
        
        # 处理 OPTIONS 请求
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }
    
    # 错误页面
    error_page 404 /index.html;
}
```

> **💡 缓存控制说明：**
> - **HTML 文件不缓存**：确保每次部署新版本时，浏览器都能获取到最新的 HTML（包含新的构建时间戳）
> - **JS/CSS 长期缓存**：因为这些文件文件名包含 hash，内容变化时文件名会变，可以安全地长期缓存
> - **自动版本更新**：前端会在检测到新版本时自动刷新页面，无需手动清空缓存

### 子目录部署配置（如果需要部署在子路径）

如果前端部署在 `http://your-domain.com/app/` 子路径下：

**1. 修改 `vite.config.js`：**
```javascript
export default defineConfig({
  base: '/app/',  // 添加 base 路径
  // ... 其他配置
})
```

**2. 重新构建：**
```bash
npm run build
```

**3. Nginx 配置：**
```nginx
location /app/ {
    alias /usr/share/nginx/html/app/;
    try_files $uri $uri/ /app/index.html;
}
```

---

## 🐛 常见问题排查

### 问题1：页面空白，浏览器控制台报错 404
**原因：** Vue Router 的 history 模式需要 nginx 配置 `try_files`

**解决：** 确保 nginx 配置中有：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### 问题2：API 请求失败（404 或 CORS 错误）
**原因：** nginx 没有配置 API 代理或代理地址不正确

**解决：** 
1. 检查 nginx 配置中 `/api/` 的 `proxy_pass` 地址
2. 确认后端服务正在运行（默认端口 8080）
3. 检查后端服务地址是否正确

### 问题3：静态资源（JS/CSS）加载失败 404
**原因：** 
- 文件路径不正确
- 权限问题

**解决：**
```bash
# 检查文件是否存在
ls -la /usr/share/nginx/html/assets/

# 检查文件权限
chmod -R 755 /usr/share/nginx/html/

# 检查 nginx 用户权限
chown -R nginx:nginx /usr/share/nginx/html/  # CentOS/RHEL
chown -R www-data:www-data /usr/share/nginx/html/  # Ubuntu/Debian
```

### 问题4：刷新页面后 404
**原因：** 路由使用 history 模式，nginx 需要配置 `try_files`

**解决：** 参考上面的 nginx 配置

### 问题5：页面显示但样式错乱
**原因：** CSS 文件路径不正确或未加载

**解决：**
1. 检查浏览器 Network 面板，看 CSS 文件是否加载成功
2. 检查 `index.html` 中的 CSS 路径是否正确
3. 确认所有文件都已上传到服务器

---

## ✅ 部署后验证步骤

1. **检查静态文件是否可访问：**
   ```bash
   curl http://your-domain.com/
   curl http://your-domain.com/assets/index-*.js  # 替换为实际文件名
   ```

2. **检查 API 代理是否正常：**
   ```bash
   curl http://your-domain.com/api/system/health
   ```

3. **浏览器测试：**
   - 打开 `http://your-domain.com`
   - 打开浏览器开发者工具（F12）
   - 查看 Console 是否有错误
   - 查看 Network 面板，确认资源加载情况

---

## 📝 快速部署脚本

```bash
#!/bin/bash
# deploy.sh

# 构建前端
echo "开始构建前端..."
cd frontend
npm install
npm run build

# 上传到服务器（需要配置 SSH）
echo "上传到服务器..."
scp -r dist/* user@server:/usr/share/nginx/html/

# 重启 nginx（如果需要）
echo "重启 nginx..."
ssh user@server "sudo systemctl reload nginx"

echo "部署完成！"
```

---

## 🔐 权限配置

```bash
# 设置正确的文件所有者
sudo chown -R nginx:nginx /usr/share/nginx/html/

# 设置正确的文件权限
sudo chmod -R 755 /usr/share/nginx/html/

# 确保 nginx 有读取权限
sudo chmod -R 644 /usr/share/nginx/html/assets/*
```

---

## 💡 调试技巧

1. **查看 nginx 错误日志：**
   ```bash
   tail -f /var/log/nginx/error.log
   ```

2. **查看 nginx 访问日志：**
   ```bash
   tail -f /var/log/nginx/access.log
   ```

3. **测试 nginx 配置：**
   ```bash
   sudo nginx -t
   ```

4. **重新加载 nginx：**
   ```bash
   sudo systemctl reload nginx
   # 或
   sudo nginx -s reload
   ```

---

## 📞 获取帮助

如果遇到问题，请提供以下信息：
1. 浏览器控制台的错误信息（F12 → Console）
2. Network 面板中的失败请求（F12 → Network）
3. Nginx 错误日志：`/var/log/nginx/error.log`
4. 服务器部署路径和 nginx 配置
