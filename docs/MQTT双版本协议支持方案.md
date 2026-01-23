# MQTT 双版本协议支持方案

## 一、需求概述

### 1.1 核心需求
- **协议选择**：产品创建时，协议类型默认 `MQTT1.0`，可选择 `MQTT2.0`
- **双版本支持**：系统需要同时支持两个版本的MQTT协议
- **上报和下发**：两个版本的格式完全不同，需要分别处理

### 1.2 协议格式对比

#### MQTT1.0 格式
**上报格式：**
```json
{
  "did": "11223344",
  "content": [
    {
      "addr": "tem",
      "addrv": "25.5",
      "ctime": "2025/01/23 10:30:00",
      "pid": "11223344"
    }
  ]
}
```

**下发格式：**
```json
{
  "did": "11223344",
  "content": [
    {
      "addr": "window",
      "addrv": "1",
      "pid": "11223344"
    }
  ],
  "utime": "2025/01/23 10:30:00"
}
```

#### MQTT2.0 格式
**上报格式：**
```json
{
  "id": "49",
  "version": "V1.0",
  "ack": 1,
  "params": [
    {
      "clientID": "11223344",
      "properties": [
        {
          "name": "tem",
          "value": 25.5,
          "timestamp": 1769148150
        }
      ]
    }
  ]
}
```

**下发格式：**
```json
{
  "id": "123",
  "version": "1.0",
  "ack": 0,
  "params": [
    {
      "clientID": "11223344",
      "properties": [
        {
          "name": "window",
          "value": "1"
        }
      ]
    }
  ]
}
```

---

## 二、技术方案设计

### 2.1 协议识别策略

#### 通过产品协议字段识别
- **识别依据**：通过设备编码查询设备 → 产品 → protocol字段
- **逻辑**：
  ```java
  Device device = deviceService.getByDeviceCode(deviceCode);
  Product product = productService.getById(device.getProductId());
  String protocol = product.getProtocol(); // "MQTT1.0" 或 "MQTT2.0"
  ```
- **优点**：
  - 协议类型在产品创建时明确确定，无需猜测
  - 逻辑简单清晰，易于维护
  - 避免因格式相似导致的误判

### 2.2 架构设计

```
MQTT消息接收
    ↓
MqttMessageHandler.handleMessage()
    ↓
根据设备所属产品协议字段识别
    ↓
    ├─→ MQTT1.0 → DeviceReportDTO → DeviceDataService
    └─→ MQTT2.0 → DeviceReportV2DTO → DeviceDataService
    ↓
统一数据转换（转换为统一的数据结构）
    ↓
数据保存、告警检查等后续处理
```

### 2.3 核心类设计

#### 2.3.1 DTO类
- ✅ `DeviceCommandDTO` - MQTT1.0下发（已存在）
- ✅ `DeviceCommandV2DTO` - MQTT2.0下发（已创建）
- ✅ `DeviceReportDTO` - MQTT1.0上报（已存在）
- ❌ `DeviceReportV2DTO` - MQTT2.0上报（**需要创建**）

#### 2.3.2 服务类修改
- `MqttMessageHandler` - 增加协议识别和格式转换逻辑
- `DeviceDataService` - 增加统一的数据处理方法（接收统一格式）
- `MqttPublisher` - 已支持双版本下发（无需修改）

---

## 三、详细实现方案

### 3.1 创建 DeviceReportV2DTO

**文件路径**：`backend/src/main/java/com/iot/platform/dto/DeviceReportV2DTO.java`

**结构**：
```java
@Data
public class DeviceReportV2DTO {
    private String id;           // 消息ID
    private String version;       // "V1.0"
    private Integer ack;          // 1
    private List<DeviceParam> params;
    
    @Data
    public static class DeviceParam {
        private String clientID;
        private List<Property> properties;
    }
    
    @Data
    public static class Property {
        private String name;
        private Object value;      // 可以是数字、字符串等
        private Long timestamp;   // Unix时间戳（秒）
    }
}
```

### 3.2 修改 MqttMessageHandler

**修改点**：
1. 增加MQTT2.0格式解析方法 `parseV2Report(String payload)`
2. 增加格式转换方法 `convertV2ToUnified(DeviceReportV2DTO)`
3. 修改 `handleDeviceReport()` 方法，支持双版本

**核心逻辑**：
```java
private void handleDeviceReport(String deviceCode, String payload) {
    // 1. 根据设备编码查询产品协议
    Device device = deviceService.getByDeviceCode(deviceCode);
    Product product = productService.getById(device.getProductId());
    String protocol = product.getProtocol(); // "MQTT1.0" 或 "MQTT2.0"
    
    // 2. 根据协议解析
    UnifiedReportData unifiedData;
    if ("MQTT2.0".equals(protocol)) {
        DeviceReportV2DTO v2DTO = parseV2Report(payload);
        unifiedData = convertV2ToUnified(v2DTO);
    } else {
        DeviceReportDTO v1DTO = parseV1Report(payload);
        unifiedData = convertV1ToUnified(v1DTO);
    }
    
    // 3. 统一处理
    deviceDataService.handleUnifiedReport(unifiedData);
}
```

### 3.3 创建统一数据模型

**文件路径**：`backend/src/main/java/com/iot/platform/dto/UnifiedReportData.java`

**用途**：将两种格式转换为统一的数据结构，便于后续处理

**结构**：
```java
@Data
public class UnifiedReportData {
    private String deviceCode;           // 设备编码
    private List<PropertyData> properties; // 属性列表
    
    @Data
    public static class PropertyData {
        private String name;      // 属性标识符（addr）
        private String value;      // 属性值（字符串）
        private LocalDateTime ctime; // 采集时间
    }
}
```

### 3.4 修改 DeviceDataService

**修改点**：
1. 新增 `handleUnifiedReport(UnifiedReportData)` 方法
2. 保留原有的 `handleDeviceReport(DeviceReportDTO)` 方法（兼容旧代码）
3. 统一的数据处理逻辑放在 `handleUnifiedReport()` 中

### 3.5 获取设备协议信息

**实现逻辑**：
```java
private String getDeviceProtocol(String deviceCode) {
    try {
        Device device = deviceService.getByDeviceCode(deviceCode);
        if (device == null) {
            log.error("设备不存在: {}", deviceCode);
            return "MQTT1.0"; // 默认使用MQTT1.0
        }
        
        Product product = productService.getById(device.getProductId());
        if (product == null) {
            log.error("产品不存在: {}", device.getProductId());
            return "MQTT1.0"; // 默认使用MQTT1.0
        }
        
        String protocol = product.getProtocol();
        log.info("设备 {} 所属产品 {} 协议类型: {}", deviceCode, product.getName(), protocol);
        return protocol;
    } catch (Exception e) {
        log.error("获取设备协议信息失败，使用默认MQTT1.0", e);
        return "MQTT1.0"; // 默认使用MQTT1.0
    }
}
```

---

## 四、文件修改清单

### 4.1 需要创建的文件
1. ✅ `DeviceCommandV2DTO.java` - MQTT2.0下发DTO（已创建）
2. ❌ `DeviceReportV2DTO.java` - MQTT2.0上报DTO（**待创建**）
3. ❌ `UnifiedReportData.java` - 统一数据模型（**待创建**）

### 4.2 需要修改的文件
1. ❌ `MqttMessageHandler.java` - 增加MQTT2.0解析和格式转换（**待修改**）
2. ❌ `DeviceDataService.java` - 增加统一处理方法（**待修改**）

### 4.3 无需修改的文件（已支持）
1. ✅ `MqttPublisher.java` - 已支持双版本下发
2. ✅ `CommandController.java` - 已支持根据产品协议选择格式
3. ✅ `ProductService.java` - 已支持协议字段处理
4. ✅ `ProductList.vue` - 前端已支持协议选择

---

## 五、测试方案

### 5.1 单元测试
- [ ] MQTT1.0上报格式解析测试
- [ ] MQTT2.0上报格式解析测试
- [ ] 协议自动识别测试
- [ ] 格式转换测试

### 5.2 集成测试
- [ ] MQTT1.0设备上报数据 → 数据库保存
- [ ] MQTT2.0设备上报数据 → 数据库保存
- [ ] MQTT1.0产品 → 下发命令格式验证
- [ ] MQTT2.0产品 → 下发命令格式验证

### 5.3 兼容性测试
- [ ] 旧设备（MQTT1.0）继续正常工作
- [ ] 新设备（MQTT2.0）正常工作
- [ ] 混合场景（同时存在两种协议设备）

---

## 六、实施步骤

### 步骤1：创建DTO类
1. 创建 `DeviceReportV2DTO.java`
2. 创建 `UnifiedReportData.java`

### 步骤2：修改消息处理器
1. 修改 `MqttMessageHandler.java`
   - 增加获取设备协议方法
   - 增加MQTT2.0解析方法
   - 增加格式转换方法
   - 修改消息处理流程

### 步骤3：修改数据服务
1. 修改 `DeviceDataService.java`
   - 增加统一处理方法
   - 保持向后兼容

### 步骤4：测试验证
1. 使用设备模拟器测试MQTT1.0上报
2. 使用设备模拟器测试MQTT2.0上报（需要修改模拟器）
3. 验证数据保存和告警功能

---

## 七、注意事项

### 7.1 兼容性
- ✅ 保持MQTT1.0完全兼容，不影响现有设备
- ✅ 新设备可以选择使用MQTT2.0
- ✅ 两种协议可以共存

### 7.2 性能考虑
- 通过设备编码查询产品协议，需要查询数据库
- 可考虑缓存设备协议信息，减少数据库查询
- 格式转换逻辑简单，性能影响可忽略

### 7.3 错误处理
- 设备不存在时，默认使用MQTT1.0
- 产品不存在时，默认使用MQTT1.0
- 格式解析失败时，记录错误日志，不影响其他消息处理

---

## 八、后续优化建议

1. **设备模拟器支持**：修改 `device_simulator.py` 支持MQTT2.0格式上报
2. **协议版本管理**：考虑未来支持更多协议版本（MQTT3.0等）
3. **性能优化**：缓存设备协议信息，减少数据库查询
4. **文档完善**：更新API文档，说明双版本协议支持
