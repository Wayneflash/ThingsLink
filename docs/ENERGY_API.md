# 能源统计API文档

## 1. 获取能耗统计

### 接口信息
- **接口路径**：`POST /api/energy/statistics`
- **请求方法**：POST
- **权限要求**：需要认证
- **权限标识**：`energy:statistics:list`

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 是 | 开始时间（格式：yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间（格式：yyyy-MM-dd HH:mm:ss） |
| deviceId | Long | 否 | 设备ID（不传则查询所有设备） |
| groupId | Long | 否 | 分组ID（不传则查询所有分组） |
| energyType | String | 否 | 能源类型（electric/water/gas） |

### 请求示例
```json
{
  "startTime": "2026-01-01 00:00:00",
  "endTime": "2026-01-18 23:59:59",
  "deviceId": null,
  "groupId": null,
  "energyType": "electric"
}
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalConsumption": 1234.56,
    "unit": "kWh",
    "avgConsumption": 123.45,
    "yoy": 10.5,
    "mom": 5.3,
    "top10Devices": [
      {
        "deviceId": 1,
        "deviceName": "电表001",
        "deviceCode": "METER_001",
        "totalConsumption": 456.78,
        "unit": "kWh",
        "recordCount": 120
      }
    ]
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| totalConsumption | Double | 总能耗 |
| unit | String | 单位（kWh/m³） |
| avgConsumption | Double | 平均能耗 |
| yoy | Double | 同比增长率（%） |
| mom | Double | 环比增长率（%） |
| top10Devices | Array | 能耗Top10设备列表 |

---

## 2. 获取能耗趋势

### 接口信息
- **接口路径**：`POST /api/energy/trend`
- **请求方法**：POST
- **权限要求**：需要认证
- **权限标识**：`energy:trend:list`

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 是 | 开始时间（格式：yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间（格式：yyyy-MM-dd HH:mm:ss） |
| deviceId | Long | 否 | 设备ID（不传则查询所有设备） |
| energyType | String | 否 | 能源类型（electric/water/gas） |
| dateFormat | String | 否 | 时间格式（day/hour） |

### 请求示例
```json
{
  "startTime": "2026-01-01 00:00:00",
  "endTime": "2026-01-18 23:59:59",
  "deviceId": 1,
  "energyType": "electric",
  "dateFormat": "day"
}
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "timeLabel": "2026-01-01",
      "consumption": 123.45,
      "unit": "kWh"
    },
    {
      "timeLabel": "2026-01-02",
      "consumption": 134.56,
      "unit": "kWh"
    }
  ]
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| timeLabel | String | 时间标签 |
| consumption | Double | 能耗值 |
| unit | String | 单位（kWh/m³） |

---

## 3. 获取能耗报表

### 接口信息
- **接口路径**：`POST /api/energy/report`
- **请求方法**：POST
- **权限要求**：需要认证
- **权限标识**：`energy:report:list`

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 是 | 开始时间（格式：yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间（格式：yyyy-MM-dd HH:mm:ss） |
| deviceId | Long | 否 | 设备ID（不传则查询所有设备） |
| groupId | Long | 否 | 分组ID（不传则查询所有分组） |
| energyType | String | 否 | 能源类型（electric/water/gas） |
| reportType | String | 否 | 报表类型（daily/monthly/yearly） |
| page | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |

### 请求示例
```json
{
  "startTime": "2026-01-01 00:00:00",
  "endTime": "2026-01-18 23:59:59",
  "deviceId": null,
  "groupId": null,
  "energyType": "electric",
  "reportType": "daily",
  "page": 1,
  "pageSize": 10
}
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "deviceId": 1,
        "deviceName": "电表001",
        "deviceCode": "METER_001",
        "date": "2026-01-01",
        "consumption": 123.45,
        "unit": "kWh"
      }
    ],
    "total": 18,
    "page": 1,
    "pageSize": 10
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| records | Array | 报表数据列表 |
| deviceId | Long | 设备ID |
| deviceName | String | 设备名称 |
| deviceCode | String | 设备编码 |
| date | String | 日期 |
| consumption | Double | 能耗值 |
| unit | String | 单位（kWh/m³） |
| total | Integer | 总记录数 |
| page | Integer | 当前页码 |
| pageSize | Integer | 每页数量 |

---

## 4. 获取实时能耗（Dashboard用）

### 接口信息
- **接口路径**：`POST /api/energy/realtime`
- **请求方法**：POST
- **权限要求**：需要认证
- **权限标识**：`energy:statistics:list`

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| timeRange | String | 否 | 时间范围（today/week/month） |

### 请求示例
```json
{
  "timeRange": "today"
}
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "electric": {
      "totalConsumption": 1234.56,
      "unit": "kWh",
      "currentPower": 45.6,
      "topDevices": [
        {
          "deviceId": 1,
          "deviceName": "电表001",
          "consumption": 456.78,
          "unit": "kWh"
        }
      ]
    },
    "water": {
      "totalConsumption": 234.56,
      "unit": "m³",
      "currentFlow": 1.2,
      "topDevices": [...]
    },
    "gas": {
      "totalConsumption": 345.67,
      "unit": "m³",
      "currentFlow": 0.8,
      "topDevices": [...]
    }
  }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| electric | Object | 电力数据 |
| water | Object | 水耗数据 |
| gas | Object | 气耗数据 |
| totalConsumption | Double | 总能耗 |
| unit | String | 单位（kWh/m³） |
| currentPower | Double | 当前功率（仅电力） |
| currentFlow | Double | 当前流量（仅水/气） |
| topDevices | Array | Top设备列表 |

---

## 5. 导出报表

### 接口信息
- **接口路径**：`POST /api/energy/export`
- **请求方法**：POST
- **权限要求**：需要认证
- **权限标识**：`energy:report:list`

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 是 | 开始时间（格式：yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间（格式：yyyy-MM-dd HH:mm:ss） |
| deviceId | Long | 否 | 设备ID（不传则查询所有设备） |
| groupId | Long | 否 | 分组ID（不传则查询所有分组） |
| energyType | String | 否 | 能源类型（electric/water/gas） |
| reportType | String | 否 | 报表类型（daily/monthly/yearly） |

### 请求示例
```json
{
  "startTime": "2026-01-01 00:00:00",
  "endTime": "2026-01-18 23:59:59",
  "deviceId": null,
  "groupId": null,
  "energyType": "electric",
  "reportType": "daily"
}
```

### 响应格式
- **响应类型**：文件流（Excel）
- **文件名**：`energy_report_{timestamp}.xlsx`
- **Content-Type**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

## 6. 错误码说明

| 错误码 | 说明 |
|---------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 500 | 服务器错误 |

---

## 7. 数据权限说明

- **超级管理员**：查看所有数据（不过滤）
- **普通用户**：只能查看本分组及下级分组数据
- **过滤字段**：查询时自动添加 `group_id` 过滤条件

---

## 8. 注意事项

1. **时间格式**：统一使用 `yyyy-MM-dd HH:mm:ss` 格式
2. **单位换算**：1吨水 ≈ 1立方米（m³）
3. **能耗计算**：周期能耗 = 当前表底示数 - 上一次表底示数
4. **分页查询**：报表接口必须分页，禁止一次性加载所有数据
5. **性能要求**：查询响应时间 < 500ms
