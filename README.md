# IoT Platform 轻量级物联网平台

> 基于 Spring Boot + Vue 3 的轻量级物联网管理平台，支持设备接入、数据采集、告警监控、权限管理等功能。

## 项目简介

IoT Platform 是一个功能完善的物联网设备管理平台，采用前后端分离架构，支持 MQTT 协议设备接入，提供设备管理、产品管理、告警配置、用户权限等核心功能。

### 主要特性

- 🚀 **设备管理** - 设备注册、状态监控、在线/离线检测
- 📦 **产品管理** - 产品定义、属性配置、命令下发
- 🌳 **分组管理** - 树形设备分组，支持多级分组
- 👥 **权限管理** - 基于角色的访问控制 (RBAC)，支持数据权限过滤
- 📊 **数据采集** - MQTT 协议数据接收与存储
- 🔔 **告警系统** - 告警规则配置、告警统计、消息通知
- 📹 **视频管理** - GB28181视频设备管理、实时播放、录像回放
- 📈 **数据概览** - 设备统计、图表展示
- 🎨 **现代化界面** - 基于 Element Plus + Tailwind CSS

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 11+ | 开发语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 6.2 | 缓存数据库 |
| MQTT | Eclipse Paho 1.2.5 | 消息队列协议 |
| Hutool | 5.8.25 | Java 工具类库 |
| FastJSON | 2.0.43 | JSON 处理 |
| OkHttp | 4.10.0 | HTTP 客户端（WVP接口调用） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Vue Router | 4.2.4 | 路由管理 |
| Element Plus | 2.3.14 | UI 组件库 |
| Axios | 1.5.0 | HTTP 客户端 |
| ECharts | 5.4.3 | 数据可视化 |
| HLS.js | 1.4.12 | HLS视频播放库 |
| Tailwind CSS | 4.1.18 | CSS 框架 |
| Vite | 4.4.9 | 构建工具 |

### 基础设施

| 组件 | 版本 | 说明 |
|------|------|------|
| EMQX | 5.3 | MQTT Broker |
| WVP | - | GB28181视频平台（外部服务） |
| Nginx | - | 反向代理 |
| Docker | - | 容器化部署 |

## 项目结构

```
IOT/
├── backend/                    # 后端项目
│   ├── src/main/java/com/iot/platform/
│   │   ├── IotPlatformApplication.java
│   │   ├── common/             # 公共类
│   │   ├── config/             # 配置类
│   │   ├── controller/         # 控制器
│   │   ├── dto/                # 数据传输对象
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── mqtt/               # MQTT 相关
│   │   ├── service/            # 业务逻辑层
│   │   ├── task/               # 定时任务
│   │   └── util/               # 工具类
│   ├── src/main/resources/
│   │   └── application.yml     # 应用配置
│   └── pom.xml
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── components/         # 公共组件
│   │   │   ├── GroupSelector.vue   # 分组选择器
│   │   │   ├── GroupTree.vue       # 分组树
│   │   │   └── NotificationPanel.vue
│   │   ├── layouts/            # 布局组件
│   │   │   └── MainLayout.vue  # 主布局
│   │   ├── router/             # 路由配置
│   │   ├── views/              # 页面组件
│   │   │   ├── Overview.vue        # 数据概览
│   │   │   ├── DeviceManagement.vue # 设备管理
│   │   │   ├── DeviceDetail.vue    # 设备详情
│   │   │   ├── DeviceGroups.vue    # 设备分组
│   │   │   ├── ProductList.vue     # 产品列表
│   │   │   ├── ProductDetail.vue   # 产品详情
│   │   │   ├── AlarmLog.vue        # 报警统计
│   │   │   ├── AlarmThresholdConfig.vue # 报警配置
│   │   │   ├── VideoManagement.vue      # 视频管理
│   │   │   ├── VideoDetail.vue           # 视频详情
│   │   │   ├── UserManagement.vue  # 用户管理
│   │   │   ├── RoleManagement.vue  # 角色管理
│   │   │   └── ...
│   │   │   ├── VideoPlayer.vue           # 视频播放组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
│
├── scripts/                    # 运维脚本
│   ├── build-frontend.bat      # 前端编译
│   ├── build-backend.bat       # 后端编译
│   ├── deploy-frontend.bat     # 前端部署
│   ├── deploy-backend.bat      # 后端部署
│   ├── pull-code.bat           # 拉取代码
│   ├── push-code.bat           # 推送代码
│   ├── sync-db.bat             # 数据库同步
│   ├── start-dev.bat           # 启动开发环境
│   └── deploy-config.template.txt  # 部署配置模板
│
├── sql/                        # 数据库脚本
│   └── migrations/             # 迁移脚本
│       ├── 001_create_migration_history.sql
│       ├── 002_add_alarm_log_fields.sql
│       ├── 003_create_device_data_table.sql
│       ├── 003_create_video_device_table.sql
│       ├── 004_create_device_log_table.sql
│       ├── 004_init_wvp_config.sql
│       └── 005_update_video_device_fields.sql
│
├── docker-compose.yml          # Docker Compose 编排
├── init.sql                    # 数据库初始化脚本
├── .cursorrules                # 项目开发规范
└── README.md
```

## 核心功能模块

### 1. 数据概览 (`/overview`)
- 设备在线/离线统计
- 今日告警数量统计
- 设备状态分布图表
- 最近告警列表

### 2. 设备管理 (`/devices`)
- 设备列表（在线优先排序）
- 设备新增/编辑/删除
- 设备状态监控
- 设备详情查看
- 设备数据查询

### 3. 设备分组 (`/groups`)
- 树形分组结构
- 分组新增/编辑/删除
- 分组设备数量统计

### 4. 产品管理 (`/products`)
- 产品列表
- 产品属性配置
- 产品命令配置
- 产品启用/禁用

### 5. 报警统计 (`/alarms`)
- 告警日志列表
- 告警处理
- 告警恢复
- 告警统计分析

### 6. 报警配置 (`/alarm-threshold`)
- 设备告警规则配置
- 阈值设置
- 通知人员配置

### 7. 用户管理 (`/users`)
- 用户列表
- 用户新增/编辑/删除
- 用户分组分配

### 8. 视频管理 (`/video`)
- 视频设备列表（GB28181设备）
- 视频设备新增/编辑/删除
- 实时视频播放（HLS流）
- 设备状态查询
- 录像查询与回放
- 支持分组权限控制

### 9. 用户管理 (`/users`)
- 用户列表
- 用户新增/编辑/删除
- 用户分组分配

### 10. 角色管理 (`/roles`)
- 角色列表
- 角色权限配置
- 菜单权限分配

## 数据库设计

### 核心表结构

| 表名 | 说明 |
|------|------|
| `tb_user` | 用户表 |
| `tb_role` | 角色表 |
| `tb_menu` | 菜单表 |
| `tb_device_group` | 设备分组表 |
| `tb_product` | 产品表 |
| `tb_attribute` | 产品属性表 |
| `tb_command` | 产品命令表 |
| `tb_device` | 设备表 |
| `tb_device_data` | 设备数据表 |
| `tb_device_log` | 设备日志表 |
| `tb_video_device` | 视频设备表（GB28181） |
| `tb_command_log` | 命令下发记录表 |
| `tb_notification` | 消息通知表 |
| `tb_alarm_log` | 告警日志表 |
| `tb_migration_history` | 数据库迁移记录表 |
| `system_config` | 系统配置表 |

### 数据库迁移

项目使用迁移脚本管理数据库结构变更：

```bash
# 迁移脚本位置
sql/migrations/

# 同步数据库
双击 scripts/sync-db.bat
```

## 快速开始

### 环境要求

- JDK 11+
- Node.js 16+
- Maven 3.6+
- Docker & Docker Compose

### 1. 启动基础服务

```bash
# 启动 MySQL、Redis、EMQX
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 2. 编译项目

```bash
# 方式一：使用脚本（推荐）
双击 scripts/build-backend.bat   # 编译后端
双击 scripts/build-frontend.bat  # 编译前端

# 方式二：命令行
cd backend && mvn clean package -DskipTests
cd frontend && npm install && npm run build
```

### 3. 启动开发环境

```bash
# 方式一：使用脚本
双击 scripts/start-dev.bat

# 方式二：分别启动
# 后端
cd backend && mvn spring-boot:run

# 前端（新终端）
cd frontend && npm run dev
```

### 4. 访问系统

- 前端开发：http://localhost:5173
- 后端接口：http://localhost:8080
- EMQX 控制台：http://localhost:18083

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123456 | 超级管理员 |

## 常用脚本

| 脚本 | 说明 |
|------|------|
| `scripts/build-frontend.bat` | 编译前端 (`npm run build`) |
| `scripts/build-backend.bat` | 编译后端 (`mvn clean package`) |
| `scripts/deploy-frontend.bat` | 部署前端到服务器 |
| `scripts/deploy-backend.bat` | 部署后端到服务器 |
| `scripts/pull-code.bat` | 从远程拉取代码（会覆盖本地） |
| `scripts/push-code.bat` | 提交并推送代码到远程 |
| `scripts/sync-db.bat` | 同步数据库迁移脚本 |
| `scripts/start-dev.bat` | 启动本地开发环境 |

### 部署配置

首次部署需要配置服务器信息：

```bash
# 1. 复制配置模板
copy scripts/deploy-config.template.txt scripts/deploy-config.txt

# 2. 编辑配置文件
REMOTE_HOST=your-server-ip
REMOTE_PORT=22
REMOTE_USER=root
REMOTE_BACKEND_PATH=/opt/iot-platform/backend
REMOTE_FRONTEND_PATH=/opt/iot-platform/frontend
REMOTE_BACKEND_SERVICE=iot-platform
```

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/iot_platform
    username: root
    password: root123456
  
  redis:
    host: localhost
    port: 16379
    password: redis123456

mqtt:
  broker-url: tcp://localhost:1883
  client-id: iot-platform-backend
  username: admin
  password: admin123.

# WVP视频平台配置（存储在system_config表中）
# wvp.server.url: WVP服务器地址（HTTPS）
# wvp.server.username: WVP登录用户名
# wvp.server.password: WVP登录密码（MD5）
```

### MQTT 主题配置

| 主题 | 方向 | 说明 |
|------|------|------|
| `ssc/+/report` | 设备 → 平台 | 设备数据上报 |
| `ssc/{deviceCode}/command` | 平台 → 设备 | 命令下发 |

## API 接口

### 设备接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/devices` | POST | 创建设备 |
| `/devices/list` | POST | 分页查询设备列表 |
| `/devices/detail` | POST | 获取设备详情 |
| `/devices/update` | POST | 更新设备 |
| `/devices/delete` | POST | 删除设备 |
| `/devices/statistics` | POST | 获取设备统计数据 |

### 告警接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/alarm-logs/list` | POST | 查询告警列表 |
| `/alarm-logs/handle` | POST | 处理告警 |
| `/alarm-logs/statistics` | POST | 告警统计 |

### 视频接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/video/list` | POST | 查询视频设备列表 |
| `/video/detail` | POST | 获取视频设备详情（含设备状态） |
| `/video/add` | POST | 添加视频设备 |
| `/video/update` | POST | 更新视频设备 |
| `/video/delete` | POST | 删除视频设备 |
| `/video/play` | POST | 获取实时播放流地址 |
| `/video/record/query` | POST | 查询录像列表 |
| `/video/record/playback` | POST | 获取录像回放流地址 |

### 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/auth/login` | POST | 用户登录 |
| `/auth/logout` | POST | 用户登出 |
| `/users/list` | POST | 获取用户列表 |

## 视频功能说明

### GB28181视频管理

平台集成了GB28181视频设备管理功能，支持视频设备的统一管理和实时播放。

#### 功能特性

- **视频设备管理**：支持GB28181设备的添加、编辑、删除
- **实时视频播放**：基于HLS协议，支持HTTPS加密传输
- **设备状态查询**：实时查询设备在线状态、IP、端口等信息
- **录像查询与回放**：支持按时间范围查询录像，并播放历史录像
- **权限控制**：基于设备分组的权限控制，与现有权限体系一致

#### 技术架构

```
IoT平台 ←→ WVP平台（GB28181视频平台）
  │              │
  ├─ 设备管理     ├─ 设备注册（GB28181协议）
  ├─ 状态查询 ────┼─ 设备状态查询接口
  ├─ 实时播放 ────┼─ 实时流接口（HLS）
  └─ 录像回放 ────┼─ 录像查询/回放接口
```

#### 配置说明

WVP服务器配置存储在`system_config`表中：

| 配置键 | 说明 | 示例值 |
|--------|------|--------|
| `wvp.server.url` | WVP服务器地址（HTTPS） | `https://lxs.fjqiaolong.com:18082` |
| `wvp.server.username` | WVP登录用户名 | `wangyq` |
| `wvp.server.password` | WVP登录密码（MD5） | `4913dfad39dca93a85ba3be000abc3a3` |

#### 使用流程

1. **添加视频设备**：
   - 进入"视频管理"页面
   - 点击"添加视频设备"
   - 填写设备编码、通道编码、名称等信息
   - 保存后设备出现在列表中

2. **查看设备详情**：
   - 点击设备列表中的"详情"按钮
   - 查看设备状态信息（在线/离线、IP、端口等）
   - 查看实时视频播放

3. **播放实时视频**：
   - 在设备详情页点击"播放"按钮
   - 系统自动获取HLS流地址并播放
   - 支持播放控制（播放/暂停、音量、全屏）

4. **查询录像**：
   - 在设备详情页切换到"历史录像"标签
   - 选择时间范围查询录像列表
   - 点击录像片段播放历史录像

#### 注意事项

- 视频设备需要先在WVP平台上通过GB28181协议注册
- IoT平台只管理设备映射关系，不直接管理GB28181设备
- 视频流使用HTTPS传输，确保安全性
- 播放需要浏览器支持HLS协议（现代浏览器均支持）

## 开发规范

项目遵循 `.cursorrules` 中定义的开发规范：

1. **修改优先级**：优先修改前端，后端修改需确认
2. **数据权限**：超级管理员查看所有，普通用户按分组过滤
3. **API 规范**：统一 POST 方法，JSON 格式
4. **分组组件**：统一使用 `GroupSelector` 和 `GroupTree`
5. **时间格式**：`yyyy-MM-dd HH:mm:ss`
6. **数据库迁移**：通过 `sql/migrations/` 管理，禁止直接修改表结构

## 常见问题

### 1. 设备离线检测

系统默认每 5 分钟检查一次设备在线状态，超时时间可在设备配置中设置。

### 2. 数据库字段缺失

如果出现字段不存在错误，运行数据库同步脚本：
```bash
双击 scripts/sync-db.bat
```

### 3. 编译失败

确保环境变量正确配置：
- `JAVA_HOME` 指向 JDK 11+
- Maven 在 PATH 中
- Node.js 16+ 已安装

### 4. 视频播放失败

**问题**：视频无法播放或显示黑屏

**解决方案**：
1. 检查WVP服务器配置是否正确（`system_config`表中的`wvp.server.*`配置）
2. 确保后端可以访问WVP服务器（网络连通性）
3. 检查设备是否在线（详情页查看设备状态）
4. 浏览器控制台查看错误信息（可能是CORS或HTTPS证书问题）
5. 确认HLS.js库已正确安装：`npm install hls.js`

### 5. Redis连接失败

**问题**：`Unable to connect to Redis`

**解决方案**：
1. 检查Redis容器是否运行：`docker ps | grep redis`
2. 确认端口映射正确（应为`6379:6379`）
3. 如果端口映射错误，重启Redis容器：`docker-compose restart redis`

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
