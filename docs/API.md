# IoT物联网设备管理平台 API 文档

> **版本**: v1.2.0  
> **基础URL**: `http://localhost:8080`  
> **更新时间**: 2026-01-17

---

## 📋 目录

- [1. 通用说明](#1-通用说明)
- [2. 认证授权](#2-认证授权)
- [3. 用户管理](#3-用户管理)
- [4. 角色管理](#4-角色管理)
- [5. 设备分组](#5-设备分组)
- [6. 产品管理](#6-产品管理)
- [7. 设备管理](#7-设备管理)
- [8. 设备数据](#8-设备数据)
- [9. 设备日志](#9-设备日志)
- [10. 命令下发](#10-命令下发)
- [11. 报警日志](#11-报警日志)
- [12. 设备报警配置](#12-设备报警配置)
- [13. 消息通知](#13-消息通知)
- [14. 统计查询](#14-统计查询)
- [15. 系统配置](#15-系统配置)
- [16. 视频管理](#16-视频管理)
- [17. 可视化公共接口 API](#17-可视化公共接口-api)

---

## 1. 通用说明

### 1.1 请求规范

- **请求方法**: 统一使用 `POST`（除特殊说明外）
- **请求头**: 
  ```http
  Content-Type: application/json
  Authorization: Bearer {token}  # 需要认证的接口
  ```
- **请求体**: JSON格式

### 1.2 响应规范

所有接口统一返回格式：

```json
{
  "code": 200,           // 状态码：200=成功，其他=失败
  "message": "操作成功",  // 提示信息
  "data": {}             // 响应数据
}
```

**状态码说明**:
- `200`: 成功
- `400`: 参数错误
- `401`: 未授权
- `403`: 无权限
- `500`: 服务器错误

### 1.3 数据权限

- **超级管理员**: 可查看所有数据（不过滤）
- **普通用户**: 只能查看本分组及下级分组数据
- **过滤字段**: 用户表、设备表、告警日志表都使用 `group_id` 过滤

### 1.4 分页参数

```json
{
  "page": 1,        // 页码，从1开始
  "pageSize": 20    // 每页数量
}
```

### 1.5 时间格式

统一使用格式：`yyyy-MM-dd HH:mm:ss`  
示例：`2026-01-13 08:42:49`

---

## 2. 认证授权

### 2.1 用户登录

**接口**: `POST /auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_xxx",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "roleId": 1,
      "roleName": "超级管理员",
      "menus": []
    }
  }
}
```

---

### 2.2 用户登出

**接口**: `POST /auth/logout`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

---

### 2.3 刷新Token

**接口**: `POST /auth/refresh`

**请求参数**:
```json
{
  "refreshToken": "refresh_token_xxx"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "new_token_xxx",
    "expiresIn": 7200
  }
}
```

---

### 2.4 获取当前用户信息

**接口**: `POST /auth/current`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "groupId": 0,
    "roleId": 1,
    "status": 1,
    "createTime": "2026-01-01 00:00:00"
  }
}
```

---

### 2.5 修改密码

**接口**: `POST /auth/change-password`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "newPassword": "654321"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null
}
```

**说明**:
- 不需要输入旧密码，直接设置新密码即可
- 新密码长度至少6位

---

## 3. 用户管理

### 3.1 用户列表

**接口**: `POST /users/list`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "groupId": 1,        // 可选：分组ID筛选
  "keyword": "admin",  // 可选：关键词搜索（用户名或姓名）
  "status": 1          // 可选：状态筛选（0=禁用，1=启用）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "list": [
      {
        "id": 1,
        "username": "admin",
        "realName": "管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "groupId": 0,
        "groupName": "默认分组",
        "roleId": 1,
        "roleName": "超级管理员",
        "status": 1,
        "isSuper": true,
        "createTime": "2026-01-01 00:00:00"
      }
    ]
  }
}
```

---

### 3.2 创建用户

**接口**: `POST /users/create`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "username": "user001",      // 必填
  "password": "123456",        // 必填
  "realname": "用户001",       // 必填
  "phone": "13800138000",     // 可选
  "email": "user001@example.com",  // 可选
  "groupId": 1,               // 必填：分组ID
  "roleId": 2,                // 必填：角色ID
  "status": 1                 // 可选：默认1（启用）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "username": "user001",
    "realname": "用户001",
    "phone": "13800138000",
    "email": "user001@example.com",
    "groupId": 1,
    "roleId": 2,
    "status": 1,
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 3.3 更新用户

**接口**: `POST /users/update`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "id": 2,                    // 必填
  "realname": "用户001更新",   // 可选
  "phone": "13800138001",     // 可选
  "email": "user001_new@example.com",  // 可选
  "groupId": 2,               // 可选
  "roleId": 3,                // 可选
  "status": 1                 // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 2,
    "username": "user001",
    "realname": "用户001更新",
    "phone": "13800138001",
    "email": "user001_new@example.com",
    "groupId": 2,
    "roleId": 3,
    "status": 1,
    "updateTime": "2026-01-13 09:00:00"
  }
}
```

---

### 3.4 删除用户

**接口**: `POST /users/delete`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "id": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 3.5 启用/禁用用户

**接口**: `POST /users/status`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "id": 2,
  "status": 0  // 1=启用，0=禁用
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "状态已更新",
  "data": null
}
```

---

### 3.6 重置密码

**接口**: `POST /users/password`

**请求参数**:
```json
{
  "id": 2,
  "newPassword": "newpass123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

### 3.7 获取个人资料

**接口**: `GET /users/profile`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "groupId": 0,
    "groupName": "默认分组",
    "roleId": 1,
    "roleName": "超级管理员",
    "status": 1,
    "isSuper": true,
    "createTime": "2026-01-01 00:00:00",
    "updateTime": "2026-01-13 08:00:00"
  }
}
```

---

### 3.8 更新个人资料

**接口**: `POST /users/profile`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "realName": "管理员更新",  // 必填
  "phone": "13800138001",   // 可选
  "email": "admin_new@example.com"  // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "个人资料更新成功",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员更新",
    "phone": "13800138001",
    "email": "admin_new@example.com",
    "updateTime": "2026-01-13 09:00:00"
  }
}
```

---

## 4. 角色管理

### 4.1 角色列表

**接口**: `POST /roles/list`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "超级管理员",
        "code": "super_admin",
        "description": "拥有所有权限",
        "status": 1,
        "userCount": 1,
        "isSuperAdmin": true,
        "createTime": "2026-01-01 00:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1
  }
}
```

---

### 4.2 角色详情

**接口**: `POST /roles/detail`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "roleInfo": {
      "id": 1,
      "name": "超级管理员",
      "code": "super_admin",
      "description": "拥有所有权限",
      "isSuperAdmin": true,
      "userCount": 1,
      "createTime": "2026-01-01 00:00:00"
    },
    "permissions": [
      {
        "code": "overview",
        "name": "数据概览",
        "icon": "📊",
        "sort": 1,
        "granted": true,
        "children": null
      }
    ]
  }
}
```

---

### 4.3 创建角色

**接口**: `POST /roles/create`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "name": "普通管理员",
  "code": "ROLE_ADMIN",  // 可选，不传则自动生成
  "description": "普通管理员角色",
  "permissions": [
    {
      "code": "devices",
      "granted": true,
      "actions": ["create", "update", "delete"]
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "普通管理员",
    "code": "ROLE_ADMIN",
    "description": "普通管理员角色",
    "status": 1,
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 4.4 更新角色

**接口**: `POST /roles/update`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "id": 2,
  "name": "普通管理员更新",
  "description": "更新后的描述",
  "menuIds": "devices,products",  // 方式1：直接传menuIds字符串
  "permissions": [                // 方式2：传permissions数组
    {
      "code": "devices",
      "granted": true,
      "actions": ["create", "update"]
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null
}
```

---

### 4.5 删除角色

**接口**: `POST /roles/delete`

**权限**: 仅超级管理员

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "id": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 5. 设备分组

### 5.1 创建分组

**接口**: `POST /device-groups/create`

**请求参数**:
```json
{
  "name": "办公区域",      // 必填，也支持"groupName"
  "parentId": 0,          // 可选，默认0（顶级分组）
  "description": "办公区域分组",
  "sort": 1,
  "groupType": "default"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "groupName": "办公区域",
    "parentId": 0,
    "description": "办公区域分组",
    "sort": 1,
    "groupType": "default",
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 5.2 分组树形列表

**接口**: `POST /device-groups/tree`

**请求头**: `Authorization: Bearer {token}` （可选，用于权限过滤）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tree": [
      {
        "id": 1,
        "name": "办公区域",
        "icon": null,
        "parentId": 0,
        "path": "办公区域",
        "deviceCount": 10,
        "level": 1,
        "sort": 1,
        "description": "办公区域分组",
        "children": [
          {
            "id": 2,
            "name": "一楼",
            "parentId": 1,
            "path": "办公区域/一楼",
            "deviceCount": 5,
            "level": 2,
            "children": []
          }
        ]
      }
    ]
  }
}
```

---

### 5.3 分组平铺列表

**接口**: `POST /device-groups/list`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "办公区域",
        "parentId": 0,
        "path": "办公区域",
        "level": 1
      },
      {
        "id": 2,
        "name": "一楼",
        "parentId": 1,
        "path": "办公区域/一楼",
        "level": 2
      }
    ]
  }
}
```

---

### 5.4 更新分组

**接口**: `POST /device-groups/update`

**请求参数**:
```json
{
  "id": 1,
  "groupName": "办公区域更新",
  "parentId": 0,
  "description": "更新后的描述",
  "sort": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "groupName": "办公区域更新",
    "parentId": 0,
    "description": "更新后的描述",
    "sort": 2
  }
}
```

---

### 5.5 删除分组

**接口**: `POST /device-groups/delete`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 6. 产品管理

### 6.1 创建产品

**接口**: `POST /products/create`

**请求参数**:
```json
{
  "name": "温湿度传感器",        // 必填，也支持"productName"
  "model": "TH-SENSOR-001",     // 必填，也支持"productModel"或"code"
  "protocol": "MQTT",
  "description": "温湿度传感器产品",
  "status": 1                   // 可选，默认1（启用）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "productName": "温湿度传感器",
    "productModel": "TH-SENSOR-001",
    "protocol": "MQTT",
    "description": "温湿度传感器产品",
    "status": 1,
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 6.2 产品列表

**接口**: `POST /products/list`

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "keyword": "传感器",    // 可选：关键词搜索
  "category": "sensor"    // 可选：分类筛选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "page": 1,
    "pageSize": 20,
    "totalPages": 1,
    "list": [
      {
        "id": 1,
        "productName": "温湿度传感器",
        "productModel": "TH-SENSOR-001",
        "protocol": "MQTT",
        "description": "温湿度传感器产品",
        "status": 1,
        "attrCount": 2,
        "cmdCount": 3,
        "createTime": "2026-01-13 08:42:49",
        "updateTime": "2026-01-13 08:42:49"
      }
    ]
  }
}
```

---

### 6.3 产品详情

**接口**: `POST /products/detail`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "productName": "温湿度传感器",
    "productModel": "TH-SENSOR-001",
    "protocol": "MQTT",
    "description": "温湿度传感器产品",
    "status": 1,
    "attrs": [
      {
        "id": 1,
        "productId": 1,
        "attrName": "温度",
        "attrCode": "temperature",
        "dataType": "float",
        "unit": "℃"
      }
    ],
    "commands": [
      {
        "id": 1,
        "productId": 1,
        "commandName": "设置温度阈值",
        "commandCode": "set_temp_threshold",
        "addr": "0x01",
        "commandValue": "25"
      }
    ],
    "createTime": "2026-01-13 08:42:49",
    "updateTime": "2026-01-13 08:42:49"
  }
}
```

---

### 6.4 更新产品

**接口**: `POST /products/update`

**请求参数**:
```json
{
  "id": 1,
  "productName": "温湿度传感器更新",
  "productModel": "TH-SENSOR-001",
  "protocol": "MQTT",
  "description": "更新后的描述",
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "productName": "温湿度传感器更新",
    "productModel": "TH-SENSOR-001",
    "protocol": "MQTT",
    "description": "更新后的描述",
    "status": 1
  }
}
```

---

### 6.5 删除产品

**接口**: `POST /products/delete`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**注意**: 如果产品下有设备，将返回错误提示。

---

### 6.6 添加产品属性

**接口**: `POST /products/attribute/create`

**请求参数**:
```json
{
  "productId": 1,
  "attrName": "温度",
  "attrCode": "temperature",
  "dataType": "float",
  "unit": "℃",
  "addr": "0x01"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "productId": 1,
    "attrName": "温度",
    "attrCode": "temperature",
    "dataType": "float",
    "unit": "℃",
    "addr": "0x01"
  }
}
```

---

### 6.7 获取产品属性列表

**接口**: `GET /products/{productId}/attributes`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "attrName": "温度",
      "attrCode": "temperature",
      "dataType": "float",
      "unit": "℃",
      "addr": "0x01"
    }
  ]
}
```

---

### 6.8 删除产品属性

**接口**: `POST /products/attribute/delete`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.9 添加产品命令

**接口**: `POST /products/command/create`

**请求参数**:
```json
{
  "productId": 1,
  "commandName": "设置温度阈值",
  "commandCode": "set_temp_threshold",
  "addr": "0x01",
  "commandValue": "25"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "productId": 1,
    "commandName": "设置温度阈值",
    "commandCode": "set_temp_threshold",
    "addr": "0x01",
    "commandValue": "25"
  }
}
```

---

### 6.10 获取产品命令列表

**接口**: `GET /products/{productId}/commands`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 1,
      "commandName": "设置温度阈值",
      "commandCode": "set_temp_threshold",
      "addr": "0x01",
      "commandValue": "25"
    }
  ]
}
```

---

### 6.11 删除产品命令

**接口**: `POST /products/command/delete`

**请求参数**:
```json
{
  "id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 7. 设备管理

### 7.1 创建设备

**接口**: `POST /devices`

**请求参数**:
```json
{
  "name": "设备001",              // 必填，也支持"deviceName"
  "code": "DEVICE-001",          // 必填，也支持"deviceCode"
  "productId": 1,                // 必填
  "groupId": 1,                  // 必填
  "remark": "备注信息"            // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "deviceName": "设备001",
    "deviceCode": "DEVICE-001",
    "productId": 1,
    "groupId": 1,
    "status": 0,
    "offlineTimeout": 300,
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 7.2 设备列表

**接口**: `POST /devices/list`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限过滤）

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "keyword": "设备001",    // 可选：搜索设备名称或编码
  "productId": 1,          // 可选：产品ID筛选
  "groupId": 1,            // 可选：分组ID筛选
  "status": "online"       // 可选：状态筛选（online/offline）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5,
    "list": [
      {
        "id": 1,
        "deviceName": "设备001",
        "deviceCode": "DEVICE-001",
        "productId": 1,
        "productName": "温湿度传感器",
        "productModel": "TH-SENSOR-001",
        "groupId": 1,
        "groupName": "办公区域",
        "groupPath": "办公区域/一楼",
        "status": 1,
        "lastOnlineTime": "2026-01-13 08:40:00",
        "alarmConfig": "{}",
        "alarmEnabled": true,
        "createTime": "2026-01-13 08:42:49",
        "updateTime": "2026-01-13 08:42:49"
      }
    ]
  }
}
```

---

### 7.3 设备详情

**接口**: `POST /devices/detail`

**请求参数**:
```json
{
  "deviceCode": "DEVICE-001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "deviceName": "设备001",
    "deviceCode": "DEVICE-001",
    "productId": 1,
    "productName": "温湿度传感器",
    "groupId": 1,
    "groupName": "办公区域",
    "status": 1,
    "lastOnlineTime": "2026-01-13 08:40:00",
    "createTime": "2026-01-13 08:42:49"
  }
}
```

---

### 7.4 获取设备（根据设备编码）

**接口**: `GET /devices/{deviceCode}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "deviceName": "设备001",
    "deviceCode": "DEVICE-001",
    "productId": 1,
    "groupId": 1,
    "status": 1
  }
}
```

---

### 7.5 更新设备

**接口**: `POST /devices/update`

**请求参数**:
```json
{
  "id": 1,
  "deviceName": "设备001更新",
  "deviceCode": "DEVICE-001",
  "productId": 1,
  "groupId": 2,
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "deviceName": "设备001更新",
    "deviceCode": "DEVICE-001",
    "productId": 1,
    "groupId": 2,
    "status": 1
  }
}
```

---

### 7.6 删除设备

**接口**: `POST /devices/delete`

**请求参数**:
```json
{
  "deviceCode": "DEVICE-001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 7.7 获取设备最新数据

**接口**: `POST /devices/latest-data`

**请求参数**:
```json
{
  "deviceCode": "DEVICE-001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": 1,
    "deviceCode": "DEVICE-001",
    "data": {
      "0x01": "25.5",
      "0x02": "60.0"
    },
    "reportTime": "2026-01-13 08:42:49"
  }
}
```

---

### 7.8 更新设备在线状态

**接口**: `POST /devices/{deviceCode}/status`

**请求参数**: `online=true` （Query参数）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 7.9 获取设备统计数据

**接口**: `POST /devices/statistics`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限过滤）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDevices": 100,
    "onlineDevices": 85,
    "offlineDevices": 15,
    "todayDataCount": 10000
  }
}
```

---

### 7.10 批量导入设备

**接口**: `POST /devices/batch-import`

**请求头**: `Authorization: Bearer {token}` （可选）

**请求参数**:
```json
{
  "devices": [
    {
      "deviceCode": "DEVICE-001",
      "deviceName": "设备001",
      "productModel": "TH-SENSOR-001",
      "groupId": 1
    },
    {
      "deviceCode": "DEVICE-002",
      "deviceName": "设备002",
      "productModel": "TH-SENSOR-001",
      "groupId": 1
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "导入完成: 成功2条，失败0条",
  "data": {
    "successCount": 2,
    "failCount": 0,
    "totalCount": 2,
    "errors": []
  }
}
```

---

### 7.11 检查设备编码是否存在

**接口**: `POST /devices/check-exists`

**请求参数**:
```json
{
  "deviceCodes": ["DEVICE-001", "DEVICE-002"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "existingCodes": ["DEVICE-001"]
  }
}
```

---

## 8. 设备数据

### 8.1 获取设备最新数据

**接口**: `GET /device-data/latest/{deviceCode}`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限验证）

**请求参数**: 
- `limit`: 可选，默认10（Query参数）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "deviceCode": "DEVICE-001",
      "addr": "0x01",
      "addrv": "25.5",
      "ctime": "2026-01-13 08:42:49",
      "receiveTime": "2026-01-13 08:42:49"
    }
  ]
}
```

---

### 8.2 查询设备历史数据

**接口**: `POST /device-data/list`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限验证）

**请求参数**:
```json
{
  "deviceCode": "DEVICE-001",
  "startTime": "2026-01-13 00:00:00",  // 可选
  "endTime": "2026-01-13 23:59:59",    // 可选
  "attrs": "0x01,0x02"                  // 可选：属性标识符（多个用逗号分隔）
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "deviceCode": "DEVICE-001",
      "addr": "0x01",
      "addrv": "25.5",
      "ctime": "2026-01-13 08:42:49",
      "receiveTime": "2026-01-13 08:42:49"
    }
  ]
}
```

**注意**: 最多返回1000条数据。

---

## 9. 设备日志

### 9.1 设备日志列表

**接口**: `POST /device-logs/list`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "deviceCode": "DEVICE-001",        // 可选
  "logType": "command",              // 可选：日志类型
  "startTime": "2026-01-13 00:00:00", // 可选
  "endTime": "2026-01-13 23:59:59"    // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "deviceId": 1,
        "deviceCode": "DEVICE-001",
        "logType": "command",
        "logDetail": "设置温度阈值",
        "createTime": "2026-01-13 08:42:49"
      }
    ],
    "total": 100
  }
}
```

---

## 10. 命令下发

### 10.1 查询产品命令列表

**接口**: `POST /commands/product`

**请求参数**:
```json
{
  "productId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "productId": 1,
        "commandName": "设置温度阈值",
        "commandCode": "set_temp_threshold",
        "addr": "0x01",
        "commandValue": "25"
      }
    ]
  }
}
```

---

### 10.2 下发命令给设备

**接口**: `POST /commands/send`

**说明**: 异步下发模式，接口仅负责将命令发送到MQTT，不等待设备响应。

**请求参数**:
```json
{
  "deviceCode": "DEVICE-001",
  "commands": [
    {
      "addr": "0x01",
      "addrv": "25"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "commandId": "CMD1705124569000",
    "status": "已下发",
    "deviceCode": "DEVICE-001",
    "sendTime": "2026-01-13 08:42:49",
    "message": "命令已通过MQTT下发到设备，设备收到后会自行执行"
  }
}
```

---

## 11. 报警日志

### 11.1 报警日志列表

**接口**: `POST /alarm-log/list`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "deviceCode": "DEVICE-001",        // 可选
  "alarmLevel": "critical",           // 可选：critical/warning/info
  "status": 0,                        // 可选：0=未处理，1=已处理
  "startTime": "2026-01-13 00:00:00", // 可选
  "endTime": "2026-01-13 23:59:59"    // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "deviceId": 1,
        "deviceCode": "DEVICE-001",
        "alarmLevel": "critical",
        "alarmMessage": "温度超过阈值",
        "status": 0,
        "createTime": "2026-01-13 08:42:49",
        "handleTime": null,
        "handlerName": null,
        "handleDescription": null
      }
    ],
    "total": 100
  }
}
```

---

### 11.2 处理报警

**接口**: `POST /alarm-log/handle`

**权限**: 只有通知人员才能处理

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "alarmId": 1,
  "handleDescription": "已处理，温度已恢复正常",
  "handleImages": ["image_url_1", "image_url_2"]  // 可选
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 11.3 获取未处理报警数量

**接口**: `POST /alarm-log/unhandled-count`

**请求头**: `Authorization: Bearer {token}` （必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 10
}
```

---

### 11.4 获取报警级别枚举列表

**接口**: `POST /alarm-log/alarm-levels`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "value": "critical",
      "label": "严重"
    },
    {
      "value": "warning",
      "label": "警告"
    },
    {
      "value": "info",
      "label": "提示"
    }
  ]
}
```

---

### 11.5 获取报警统计信息

**接口**: `POST /alarm-log/statistics`

**请求头**: `Authorization: Bearer {token}` （必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "unhandled": 10,
    "critical": 5,
    "warning": 30,
    "info": 65
  }
}
```

---

### 11.6 获取报警分析数据

**接口**: `POST /alarm-log/analysis`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "deviceCodes": ["DEVICE-001", "DEVICE-002"],
  "timeRange": "7days",                    // 可选：7days/30days/custom
  "startTime": "2026-01-06 00:00:00",     // 可选：timeRange为custom时必填
  "endTime": "2026-01-13 23:59:59"        // 可选：timeRange为custom时必填
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "trend": {
      "labels": ["01-06", "01-07", "01-08", "01-09", "01-10", "01-11", "01-12"],
      "critical": [2, 1, 3, 2, 1, 0, 1],
      "warning": [5, 4, 6, 5, 4, 3, 4],
      "info": [10, 8, 12, 10, 9, 7, 9]
    },
    "levelDistribution": {
      "critical": 10,
      "warning": 31,
      "info": 59
    },
    "efficiency": {
      "avgHandleTime": 3600,
      "handleRate": 0.9
    }
  }
}
```

---

## 12. 设备报警配置

### 12.1 配置设备报警阈值（单个或批量）

**接口**: `POST /device-alarm/configure`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "deviceId": 1,                    // 单个配置时使用
  "deviceIds": [1, 2, 3],          // 批量配置时使用
  "enabled": true,                  // 可选，默认true
  "alarmConfig": {
    "notifyUser": 1,                // 必填：处理人ID
    "smsEnabled": true,             // 可选：是否启用短信通知
    "metrics": {
      "temperature": {
        "enabled": true,
        "operator": ">",
        "threshold": 30.0,
        "level": "critical"
      },
      "humidity": {
        "enabled": true,
        "operator": "<",
        "threshold": 20.0,
        "level": "warning"
      }
    }
  }
}
```

**响应示例**:
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

---

### 12.2 获取设备报警配置

**接口**: `GET /device-alarm/config/{deviceId}`

**请求头**: `Authorization: Bearer {token}` （必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "notifyUser": 1,
    "smsEnabled": true,
    "metrics": {
      "temperature": {
        "enabled": true,
        "operator": ">",
        "threshold": 30.0,
        "level": "critical"
      }
    }
  }
}
```

---

### 12.3 切换设备报警启用状态

**接口**: `POST /device-alarm/toggle/{deviceId}`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**: `enabled=true` （Query参数）

**响应示例**:
```json
{
  "code": 200,
  "message": "切换成功",
  "data": "切换成功"
}
```

---

## 13. 消息通知

### 13.1 通知列表

**接口**: `POST /notification/list`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "page": 1,
  "pageSize": 20,
  "isRead": 0  // 可选：0=未读，1=已读
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "userId": 1,
        "title": "报警通知",
        "content": "设备DEVICE-001温度超过阈值",
        "type": "alarm",
        "isRead": 0,
        "createTime": "2026-01-13 08:42:49"
      }
    ],
    "total": 10
  }
}
```

---

### 13.2 获取未读通知数量

**接口**: `POST /notification/unread-count`

**请求头**: `Authorization: Bearer {token}` （必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

---

### 13.3 标记通知为已读

**接口**: `POST /notification/mark-read`

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "notificationId": 1              // 单个标记
}
```
或
```json
{
  "notificationIds": [1, 2, 3]    // 批量标记
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 13.4 标记所有通知为已读

**接口**: `POST /notification/mark-all-read`

**请求头**: `Authorization: Bearer {token}` （必填）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 10
  }
}
```

---

## 14. 统计查询

### 14.1 获取平台统计数据概览

**接口**: `POST /statistics/overview`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限过滤）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceTotal": 100,
    "deviceOnline": 85,
    "deviceOffline": 15,
    "alarmCount": 10,
    "productCount": 5,
    "userCount": 20,
    "todayDataCount": 10000
  }
}
```

---

### 14.2 获取数据上报量趋势

**接口**: `POST /statistics/data-trend`

**请求头**: `Authorization: Bearer {token}` （可选，用于数据权限过滤）

**请求参数**:
```json
{
  "range": "24h"  // 可选：24h/7d/30d，默认24h
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "timeLabels": ["00:00", "01:00", "02:00", ...],
    "dataCounts": [100, 120, 110, ...]
  }
}
```

---

### 14.3 设备分布统计

**接口**: `POST /statistics/device-distribution`

**请求参数**:
```json
{
  "type": "group"  // group=按分组统计，product=按产品统计
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "name": "办公区域",
        "count": 56,
        "percentage": 35.9
      },
      {
        "name": "生产区域",
        "count": 100,
        "percentage": 64.1
      }
    ]
  }
}
```

---

### 16.7 查询录像列表

**接口路径**: `POST /api/video/record/query`

**接口说明**: 查询视频设备的录像列表

**权限要求**: 需要认证，数据权限校验

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | String | 是 | GB28181设备编码 |
| channelId | String | 是 | GB28181通道编码 |
| startTime | String | 是 | 开始时间，格式：yyyy-MM-dd HH:mm:ss（设备时间） |
| endTime | String | 是 | 结束时间，格式：yyyy-MM-dd HH:mm:ss（设备时间） |

**请求示例**:

```json
{
  "deviceId": "43000000801218000197",
  "channelId": "43000000801320004237",
  "startTime": "2026-01-16 00:00:00",
  "endTime": "2026-01-16 23:59:59"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceId": "43000000801218000197",
    "channelId": "43000000801320004237",
    "sn": "248596",
    "name": "Camera 01",
    "sumNum": 4,
    "count": 4,
    "lastTime": null,
    "recordList": [
      {
        "deviceId": "43000000801320004237",
        "name": "Camera 01",
        "filePath": "",
        "fileSize": null,
        "address": "",
        "startTime": "2026-01-16 07:49:57",
        "endTime": "2026-01-16 07:51:10",
        "secrecy": 0,
        "type": "time",
        "recorderId": ""
      }
    ]
  }
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限访问",
  "data": null
}
```

**特殊说明**:
- 调用WVP接口 `GET /api/gb_record/query/{deviceId}/{channelId}` 查询录像
- 时间参数直接传递，不做时区转换（设备时间）
- WVP返回的时间也是设备时间，直接返回给前端
- 数据权限：校验该设备是否在用户可见分组内

---

### 16.8 获取录像回放流地址

**接口路径**: `POST /api/video/record/playback`

**接口说明**: 获取视频设备的录像回放HLS流地址（HTTPS）

**权限要求**: 需要认证，数据权限校验

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | String | 是 | GB28181设备编码 |
| channelId | String | 是 | GB28181通道编码 |
| startTime | String | 是 | 开始时间，格式：yyyy-MM-dd HH:mm:ss（设备时间） |
| endTime | String | 是 | 结束时间，格式：yyyy-MM-dd HH:mm:ss（设备时间） |

**请求示例**:

```json
{
  "deviceId": "43000000801218000197",
  "channelId": "43000000801320004237",
  "startTime": "2026-01-16 07:49:57",
  "endTime": "2026-01-16 07:51:10"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hlsUrl": "https://lxs.fjqiaolong.com:8443/rtp/43000000801218000197_43000000801320004237_20260116074957_20260116075110/hls.m3u8",
    "deviceId": "43000000801218000197",
    "channelId": "43000000801320004237",
    "startTime": "2026-01-16 07:49:57",
    "endTime": "2026-01-16 07:51:10"
  }
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限访问",
  "data": null
}
```

**错误响应**（回放失败）:

```json
{
  "code": 500,
  "message": "获取回放流失败，请稍后重试",
  "data": null
}
```

**特殊说明**:
- 调用WVP接口 `GET /api/playback/start/{deviceId}/{channelId}` 获取回放流
- 从WVP响应中提取 `https_hls` 字段（HTTPS的HLS流地址）
  - 响应示例：`"https_hls": "https://lxs.fjqiaolong.com:8443/rtp/43000000801218000197_43000000801320004237_20260116074957_20260116075110/hls.m3u8"`
  - 如果没有 `https_hls`，则fallback到 `hls` 字段
- 前端使用HTML5 video标签或HLS.js播放该地址（复用VideoPlayer组件）
- 时间参数直接传递，不做时区转换（设备时间）
- 数据权限：校验该设备是否在用户可见分组内

---

## 15. 系统配置

### 15.1 获取设备连接MQTT配置

**接口**: `GET /system/mqtt-config`

**说明**: 从数据库读取，仅用于显示

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "broker": "localhost",
    "port": 1883,
    "username": "admin",
    "password": "admin123."
  }
}
```

---

### 15.2 获取平台连接MQTT配置

**接口**: `GET /system/platform-mqtt-config`

**说明**: 从数据库读取，支持动态重新连接

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "broker": "localhost",
    "port": 1883,
    "clientId": "iot-platform-server",
    "username": "admin",
    "password": "admin123."
  }
}
```

---

### 15.3 获取系统配置

**接口**: `GET /system/config`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "systemName": "IoT物联网平台",
    "version": "1.0.0",
    "mqttConfig": {
      "broker": "localhost",
      "port": 1883,
      "username": "admin",
      "password": "admin123."
    },
    "platformMqttConfig": {
      "broker": "localhost",
      "port": 1883,
      "clientId": "iot-platform-server",
      "username": "admin",
      "password": "admin123."
    }
  }
}
```

---

### 15.4 更新设备MQTT配置

**接口**: `POST /system/device-mqtt-config`

**权限**: 仅admin账号

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "deviceMqttBroker": "localhost",
  "deviceMqttPort": 1883,
  "deviceMqttUsername": "admin",
  "deviceMqttPassword": "admin123."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "设备MQTT配置更新成功",
  "data": {
    "message": "设备MQTT配置更新成功！此配置仅用于在设备详情页显示",
    "config": {
      "broker": "localhost",
      "port": 1883,
      "username": "admin",
      "password": "admin123."
    }
  }
}
```

---

### 15.5 更新平台MQTT配置

**接口**: `POST /system/platform-mqtt-config`

**权限**: 仅admin账号

**请求头**: `Authorization: Bearer {token}` （必填）

**请求参数**:
```json
{
  "platformMqttBroker": "localhost",
  "platformMqttPort": 1883,
  "platformMqttClientId": "iot-platform-server",
  "platformMqttUsername": "admin",
  "platformMqttPassword": "admin123."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "平台MQTT配置更新成功",
  "data": {
    "message": "平台MQTT配置更新成功！MQTT连接已使用新配置重新连接",
    "config": {
      "broker": "localhost",
      "port": 1883,
      "clientId": "iot-platform-server",
      "username": "admin",
      "password": "admin123."
    }
  }
}
```

---

## 16. 视频管理

### 16.1 视频设备列表

**接口路径**: `POST /api/video/list`

**接口说明**: 分页查询视频设备列表（不包含设备状态，只查询数据库基本信息）

**权限要求**: 需要认证，数据权限过滤（按用户分组）

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页数量（10/20/50/100） |
| search | String | 否 | 搜索关键词（视频名称或编码） |
| groupId | Long | 否 | 分组ID（不传则查询所有可见分组） |

**请求示例**:

```json
{
  "page": 1,
  "pageSize": 20,
  "search": "摄像头",
  "groupId": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "deviceId": "340200000013202274845",
        "channelId": "34020000001310000001",
        "name": "办公区入口摄像头",
        "groupId": 1,
        "groupName": "办公区",
        "remark": "办公区入口监控",
        "createTime": "2026-01-10 10:30:00",
        "updateTime": "2026-01-14 15:20:00"
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 20
  }
}
```

**特殊说明**:
- 列表接口不查询设备实时状态（不调用WVP接口），响应速度快（< 100ms）
- 只返回数据库中存储的设备基本信息
- 需要查看设备状态时，点击详情进入详情页实时查询
- 数据权限：超级管理员查看所有设备，普通用户只能查看本分组及下级分组设备

---

### 16.2 视频设备详情

**接口路径**: `POST /api/video/detail/{deviceId}`

**接口说明**: 查询视频设备详情，包含设备基本信息和实时状态（调用WVP接口）

**权限要求**: 需要认证，数据权限校验

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | String | 是 | GB28181设备编码（20位） |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "deviceId": "340200000013202274845",
    "channelId": "34020000001310000001",
    "name": "办公区入口摄像头",
    "groupId": 1,
    "groupName": "办公区",
    "remark": "办公区入口监控",
    "createTime": "2026-01-10 10:30:00",
    "updateTime": "2026-01-14 15:20:00",
    "status": {
      "onLine": true,
      "ip": "192.168.1.100",
      "port": 5060,
      "channelCount": 4,
      "transport": "UDP",
      "registerTime": "2026-01-10 10:30:00",
      "keepaliveTime": "2026-01-14 16:25:30"
    }
  }
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限访问",
  "data": null
}
```

**错误响应**（WVP查询失败）:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "deviceId": "340200000013202274845",
    "channelId": "34020000001310000001",
    "name": "办公区入口摄像头",
    "groupId": 1,
    "groupName": "办公区",
    "status": null
  }
}
```

**特殊说明**:
- 调用WVP接口 `GET /api/device/query/devices/{deviceId}` 获取设备实时状态
- 如果WVP查询失败，status 字段返回 null，前端显示"无法获取状态"
- 响应时间取决于WVP接口响应速度（通常 < 1秒）

---

### 16.3 获取视频流

**接口路径**: `POST /api/video/play`

**接口说明**: 获取视频设备的实时HLS流地址（HTTPS）

**权限要求**: 需要认证，数据权限校验

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceId | String | 是 | GB28181设备编码（20位） |
| channelId | String | 是 | GB28181通道编码（20位） |

**请求示例**:

```json
{
  "deviceId": "340200000013202274845",
  "channelId": "34020000001310000001"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hlsUrl": "https://lxs.fjqiaolong.com:8443/rtp/340200000013202274845_34020000001310000001/hls.m3u8",
    "deviceId": "340200000013202274845",
    "channelId": "34020000001310000001"
  }
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限访问",
  "data": null
}
```

**错误响应**（设备离线）:

```json
{
  "code": 500,
  "message": "设备离线，无法获取视频流",
  "data": null
}
```

**错误响应**（播放失败）:

```json
{
  "code": 500,
  "message": "获取视频流失败，请稍后重试",
  "data": null
}
```

**特殊说明**:
- 调用WVP接口 `GET /api/play/start/{deviceId}/{channelId}` 获取视频流
- 从WVP响应中提取 `https_hls` 字段（HTTPS的HLS流地址）
- 前端使用HTML5 video标签或HLS.js播放该地址
- 数据权限：校验该设备是否在用户可见分组内

---

### 16.4 添加视频设备

**接口路径**: `POST /api/video/add`

**接口说明**: 添加视频设备（只在本地数据库创建记录，不调用WVP接口）

**权限要求**: 需要认证，需要管理员权限

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 视频设备名称（1-50字符） |
| deviceId | String | 是 | GB28181设备编码（20位数字） |
| channelId | String | 是 | GB28181通道编码（20位数字） |
| groupId | Long | 是 | 所属分组ID |
| remark | String | 否 | 备注说明（最多500字符） |

**请求示例**:

```json
{
  "name": "办公区入口摄像头",
  "deviceId": "340200000013202274845",
  "channelId": "34020000001310000001",
  "groupId": 1,
  "remark": "办公区入口监控"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1
  }
}
```

**错误响应**（设备已存在）:

```json
{
  "code": 400,
  "message": "该设备和通道已存在",
  "data": null
}
```

**错误响应**（分组不存在或无权限）:

```json
{
  "code": 403,
  "message": "分组不存在或无权限",
  "data": null
}
```

**特殊说明**:
- 只在 `tb_video_device` 表插入记录，不调用WVP接口
- WVP平台上的设备已通过GB28181协议自动注册
- 只是记录设备映射关系，后续用于查询状态和播放视频
- 唯一性校验：`(deviceId, channelId)` 组合必须唯一
- 数据权限：校验 `groupId` 是否在用户可见分组内

---

### 16.5 编辑视频设备

**接口路径**: `POST /api/video/update`

**接口说明**: 编辑视频设备信息（只修改本地数据库，不调用WVP接口）

**权限要求**: 需要认证，需要管理员权限

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |
| name | String | 是 | 视频设备名称（1-50字符） |
| groupId | Long | 是 | 所属分组ID |
| remark | String | 否 | 备注说明（最多500字符） |

**请求示例**:

```json
{
  "id": 1,
  "name": "办公区入口摄像头（已修改）",
  "groupId": 2,
  "remark": "更新后的备注"
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "修改成功",
  "data": null
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限",
  "data": null
}
```

**特殊说明**:
- 只更新 `tb_video_device` 表，不调用WVP接口
- `deviceId` 和 `channelId` 不允许修改（唯一标识）
- 数据权限：校验该设备是否在用户可见分组内

---

### 16.6 删除视频设备

**接口路径**: `POST /api/video/delete`

**接口说明**: 删除视频设备（只删除本地数据库记录，不调用WVP接口）

**权限要求**: 需要认证，需要管理员权限

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 设备ID |

**请求示例**:

```json
{
  "id": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误响应**（设备不存在或无权限）:

```json
{
  "code": 404,
  "message": "设备不存在或无权限",
  "data": null
}
```

**特殊说明**:
- 只删除 `tb_video_device` 表记录，不调用WVP接口
- WVP平台上的设备仍然存在
- 数据权限：校验该设备是否在用户可见分组内

---

## 17. 可视化公共接口 API

### 17.1 获取设备数据（免token，公开接口）- ⚠️ 供外部智能体使用

**接口路径**: `GET /visualization/data/public/{deviceCode}`

**接口说明**: 通过设备编码获取设备实时数据，无需token（供外部智能体生成的HTML页面调用）

**权限要求**: 无需认证

**⚠️ 安全说明**:
- 此接口不需要token，存在安全风险
- 建议仅在内网环境或受信任环境使用
- 如果部署在公网，建议添加IP白名单或访问频率限制

**请求示例**:

```
GET /visualization/data/public/88886666
```

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "data": {
      "tem": "24.78",
      "hum": "47.88"
    },
    "units": {
      "tem": "°C",
      "hum": "%"
    },
    "attrNames": {
      "tem": "温度",
      "hum": "湿度"
    },
    "deviceCode": "88886666",
    "deviceId": 1,
    "reportTime": "2026-01-16 22:43:11"
  }
}
```

**字段说明**:
- `data`: 设备数据值（key是属性的addr字段）
- `units`: 单位映射（key是属性的addr字段，value是单位，如果没有单位则为空字符串）
- `attrNames`: 属性名称映射（key是属性的addr字段，value是属性名称）

**HTML中调用示例**:

```html
<script>
async function loadData() {
    const deviceCode = '88886666';  // 设备编码
    const apiUrl = 'http://your-server:8080/visualization/data/public/' + deviceCode;
    
    try {
        const response = await fetch(apiUrl);
        const result = await response.json();
        if (result.code === 200) {
            const deviceData = result.data.data;
            const units = result.data.units || {};
            const attrNames = result.data.attrNames || {};
            
            // 更新页面显示（带单位）
            if (deviceData.tem !== undefined) {
                const unit = units.tem || '';
                document.getElementById('tem-value').textContent = deviceData.tem + unit;
            }
            if (deviceData.hum !== undefined) {
                const unit = units.hum || '';
                document.getElementById('hum-value').textContent = deviceData.hum + unit;
            }
        }
    } catch (error) {
        console.error('加载数据失败:', error);
    }
}

loadData();
setInterval(loadData, 3000);
</script>
```

**⚠️ HTML中调用接口的注意事项**:

1. **CORS跨域问题**:
   - 如果HTML部署在不同域名，需要后端配置CORS
   - 已配置 `@CrossOrigin(origins = "*")`，允许所有域名访问

2. **安全风险**:
   - deviceCode暴露在URL中，可能被恶意访问
   - 建议添加访问频率限制或IP白名单

3. **推荐方案**:
   - 方案A：使用token方式（更安全，推荐）
   - 方案B：添加访问密钥（在URL参数中传递密钥）
   - 方案C：仅在内网环境使用此公开接口

---

## 📝 更新日志

### v1.2.0 (2026-01-17)
- ✨ 新增可视化公共接口
  - 17.1 获取设备数据（免token，公开接口）- 供外部智能体使用

### v1.1.0 (2026-01-14)
- ✨ 新增视频管理模块（6个接口）
  - 16.1 视频设备列表
  - 16.2 视频设备详情
  - 16.3 获取视频流
  - 16.4 添加视频设备
  - 16.5 编辑视频设备
  - 16.6 删除视频设备

### v1.0.0 (2026-01-13)
- 初始版本
- 包含所有现有API接口文档

---

## 🔗 相关文档

- [开发规约](../.cursorrules)
- [数据库迁移脚本](../sql/migrations/)

---

*最后更新：2026-01-17*
