# 设备告警阈值配置功能实现说明

## 📋 功能概述

本次实现了完整的**设备告警阈值配置**功能，支持单设备配置和批量配置，遵循"最新设置优先"原则，告警配置直接存储在设备实体中。

## 🎯 核心特性

### 1. 数据模型设计
- **去规则化**：告警配置直接属于设备，无独立规则实体
- **JSON存储**：使用`alarm_config`字段(TEXT类型)存储完整配置
- **覆盖策略**：每次配置都覆盖原有配置，最新设置为准

### 2. 功能特点
- ✅ 单设备配置：点击设备配置按钮，编辑该设备的告警阈值
- ✅ 批量配置：先选产品→选分组→选设备→统一配置
- ✅ 多条件支持：一个设备可配置多个监控条件
- ✅ 动态物模型：根据产品物模型动态加载监控指标
- ✅ 告警级别：严重、警告、提示三种级别
- ✅ 通知人员：支持多人通知
- ✅ 告警堆叠：开启后恢复前不重复告警
- ✅ 启用开关：支持快速启用/禁用告警

## 📁 文件清单

### 后端文件 (7个)

1. **Entity层**
   - `Device.java` - 新增字段：`alarmConfig`, `alarmEnabled`

2. **DTO层** (新增)
   - `AlarmConfigDTO.java` - 告警配置数据传输对象
   - `DeviceAlarmConfigRequest.java` - 配置请求对象

3. **Service层**
   - `DeviceService.java` - 新增方法：
     - `configureAlarm()` - 配置告警（单个/批量）
     - `getAlarmConfig()` - 获取配置
     - `toggleAlarmEnabled()` - 切换启用状态

4. **Controller层** (新增)
   - `DeviceAlarmController.java` - 告警配置API接口

5. **数据库迁移**
   - `add_device_alarm_config.sql` - 数据库变更脚本

### 前端文件 (2个)

1. **Views层** (新增)
   - `AlarmThresholdConfig.vue` - 告警配置页面组件

2. **Router配置**
   - `router/index.js` - 新增路由：`/alarm-threshold`

## 🚀 部署步骤

### 1. 数据库迁移

执行SQL脚本：
```bash
mysql -u root -p iot_platform < backend/src/main/resources/db/migration/add_device_alarm_config.sql
```

或手动执行：
```sql
ALTER TABLE tb_device 
ADD COLUMN alarm_config TEXT COMMENT '告警配置(JSON格式)' AFTER offline_timeout,
ADD COLUMN alarm_enabled TINYINT(1) DEFAULT 0 COMMENT '告警是否启用' AFTER alarm_config;

CREATE INDEX idx_alarm_enabled ON tb_device(alarm_enabled);
```

### 2. 后端启动

确保后端依赖已安装（需要Jackson用于JSON序列化）：
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

启动后端服务：
```bash
cd backend
mvn spring-boot:run
```

### 3. 前端启动

```bash
cd frontend
npm install  # 如果需要
npm run dev
```

### 4. 访问页面

浏览器访问：`http://localhost:5173/alarm-threshold`

## 🔌 API接口说明

### 1. 配置告警阈值（单个/批量）

**POST** `/api/device-alarm/configure`

请求体：
```json
{
  "deviceId": 1,           // 单个配置时使用
  "deviceIds": [1, 2, 3],  // 批量配置时使用
  "alarmConfig": {
    "level": "warning",    // critical/warning/info
    "conditions": [
      {
        "metric": "temperature",
        "operator": ">",
        "threshold": 30
      }
    ],
    "notifyUsers": [1, 2],
    "stackMode": true
  },
  "enabled": true
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 3,
    "message": "成功配置 3 台设备"
  }
}
```

### 2. 获取设备告警配置

**GET** `/api/device-alarm/config/{deviceId}`

响应：
```json
{
  "code": 200,
  "data": {
    "level": "warning",
    "conditions": [...],
    "notifyUsers": [1, 2],
    "stackMode": true
  }
}
```

### 3. 切换告警启用状态

**POST** `/api/device-alarm/toggle/{deviceId}?enabled=true`

响应：
```json
{
  "code": 200,
  "message": "切换成功"
}
```

## 📊 数据结构

### alarm_config字段JSON格式

```json
{
  "level": "warning",           // 告警级别：critical/warning/info
  "conditions": [               // 监控条件数组
    {
      "metric": "temperature",  // 监控指标（物模型标识符）
      "operator": ">",          // 运算符：> < =
      "threshold": 30.5         // 阈值
    }
  ],
  "notifyUsers": [1, 2, 3],    // 通知人员ID列表
  "stackMode": true             // 告警堆叠：true=开启，false=关闭
}
```

## 🎨 界面展示

### 主列表页面
- 显示所有设备及其告警配置状态
- 支持筛选：产品、分组、配置状态、在线状态
- 显示：设备编码、告警条件、级别、通知人员、启用开关

### 单设备配置弹窗
- 展示设备基本信息
- 配置告警级别、监控条件（支持多个）、通知人员、堆叠模式
- 监控指标根据产品物模型动态加载

### 批量配置弹窗
- ⚠️ 顶部警告提示：批量配置将覆盖原有配置
- 先选产品（物模型不同）
- 选择分组（可选）
- 多选框选择设备（显示当前配置状态）
- 统一配置所有选中设备
- 预览将要配置的设备列表

## 🔧 技术要点

### 后端
- **事务管理**：使用`@Transactional`确保批量操作原子性
- **JSON序列化**：使用Jackson进行DTO与JSON字符串互转
- **异常处理**：统一异常处理和错误提示

### 前端
- **Vue 3 Composition API**：使用`<script setup>`语法
- **Element Plus**：UI组件库
- **响应式数据**：`ref`和`reactive`管理状态
- **数据解析**：设备列表加载时自动解析`alarmConfig`JSON

## ⚠️ 注意事项

1. **覆盖策略**：批量配置会完全覆盖已选设备的原有配置
2. **物模型依赖**：监控指标基于产品物模型，需先配置物模型
3. **权限控制**：建议为告警配置功能添加权限验证
4. **数据校验**：前后端都应做好数据校验
5. **性能优化**：批量配置大量设备时考虑分批处理

## 🔮 后续扩展

1. **告警触发引擎**：根据配置实时检测设备数据，触发告警
2. **告警日志**：记录告警历史
3. **通知渠道**：邮件、短信、WebSocket推送
4. **告警统计**：告警次数、趋势分析
5. **配置模板**：保存常用配置为模板，快速应用

## 📝 总结

本次实现了完整的设备告警阈值配置功能，遵循"去规则化"的设计理念，配置直接绑定设备，支持单个和批量操作，界面友好，交互流畅。代码结构清晰，易于维护和扩展。
