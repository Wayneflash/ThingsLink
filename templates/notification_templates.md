# 消息通知模板

## 短信推送模板

### 模板1：设备报警通知（简洁版）
```
【物联网平台】{user_name}，设备{device_code}发生{alarm_level}报警：{alarm_message}。触发时间：{trigger_time}，请及时处理。
```

**变量说明：**
- `{user_name}` - 用户姓名
- `{device_code}` - 设备编码
- `{alarm_level}` - 报警级别（严重/警告/提示）
- `{alarm_message}` - 报警内容
- `{trigger_time}` - 触发时间

---

### 模板2：设备报警通知（详细版）
```
【物联网平台】{user_name}，您的设备{device_code}在{trigger_time}触发{alarm_level}报警。报警详情：{alarm_message}。请登录平台查看详情并及时处理。
```

---

### 模板3：严重报警紧急通知
```
【紧急通知】{user_name}，设备{device_code}发生严重报警！{alarm_message}。触发时间：{trigger_time}。请立即处理！
```

---

## 邮件推送模板

### 模板1：标准报警通知邮件
```
主题：【物联网平台】设备报警通知 - {alarm_level}

尊敬的 {user_name}：

您好！

您的设备发生报警，详情如下：

设备编码：{device_code}
报警级别：{alarm_level}
报警内容：{alarm_message}
触发时间：{trigger_time}

请登录物联网平台查看详情并及时处理。

如有疑问，请联系系统管理员。

此致
敬礼！

物联网平台
{current_time}
```

---

### 模板2：HTML格式报警通知邮件
```
主题：【物联网平台】设备报警通知 - {alarm_level}

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>设备报警通知</title>
    <style>
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px 8px 0 0; }
        .header h1 { margin: 0; font-size: 24px; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
        .info-item { margin-bottom: 15px; padding: 12px; background: white; border-left: 4px solid #667eea; border-radius: 4px; }
        .info-label { font-weight: bold; color: #666; }
        .info-value { color: #333; margin-top: 5px; }
        .alarm-level { display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 14px; font-weight: bold; }
        .alarm-critical { background: #fee; color: #c00; }
        .alarm-warning { background: #fff3e0; color: #e65100; }
        .alarm-info { background: #e3f2fd; color: #1976d2; }
        .alarm-message { background: #fff3cd; padding: 15px; border-radius: 4px; border-left: 4px solid #ffc107; margin: 20px 0; }
        .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; }
        .btn { display: inline-block; padding: 10px 25px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>设备报警通知</h1>
        </div>
        <div class="content">
            <p>尊敬的 <strong>{user_name}</strong>：</p>
            <p>您的设备发生报警，请及时查看并处理。</p>
            
            <div class="info-item">
                <div class="info-label">设备编码</div>
                <div class="info-value">{device_code}</div>
            </div>
            
            <div class="info-item">
                <div class="info-label">报警级别</div>
                <div class="info-value">
                    <span class="alarm-level alarm-{alarm_level}">{alarm_level_text}</span>
                </div>
            </div>
            
            <div class="info-item">
                <div class="info-label">触发时间</div>
                <div class="info-value">{trigger_time}</div>
            </div>
            
            <div class="alarm-message">
                <div class="info-label">报警内容</div>
                <div class="info-value">{alarm_message}</div>
            </div>
            
            <div style="text-align: center;">
                <a href="{platform_url}" class="btn">登录平台查看详情</a>
            </div>
        </div>
        <div class="footer">
            <p>此邮件由系统自动发送，请勿回复。</p>
            <p>发送时间：{current_time}</p>
        </div>
    </div>
</body>
</html>
```

---

### 模板3：严重报警紧急通知邮件
```
主题：【紧急】设备严重报警通知 - 请立即处理

尊敬的 {user_name}：

您的设备发生严重报警，需要立即处理！

【设备信息】
设备编码：{device_code}

【报警详情】
报警级别：严重
报警内容：{alarm_message}
触发时间：{trigger_time}

【处理建议】
1. 立即登录物联网平台查看设备实时状态
2. 检查设备是否正常运行
3. 根据报警内容采取相应措施
4. 处理完成后在系统中记录处理结果

【快速链接】
点击此处直接跳转到报警详情页面：{alarm_detail_url}

如有紧急情况，请联系：{support_phone}

此致
敬礼！

物联网平台
{current_time}
```

---

## 变量映射表

| 模板变量 | 对应数据字段 | 说明 |
|---------|-------------|------|
| `{user_name}` | User.realName 或 User.username | 用户姓名 |
| `{device_code}` | AlarmLog.deviceCode | 设备编码 |
| `{alarm_level}` | AlarmLog.alarmLevel | 报警级别（critical/warning/info） |
| `{alarm_level_text}` | 映射后的文本 | 严重/警告/提示 |
| `{alarm_message}` | AlarmLog.alarmMessage | 报警内容 |
| `{trigger_time}` | AlarmLog.triggerTime | 触发时间 |
| `{current_time}` | 当前时间 | 邮件发送时间 |
| `{platform_url}` | 系统配置 | 平台访问地址 |
| `{alarm_detail_url}` | 拼接链接 | 报警详情页面链接 |
| `{support_phone}` | 系统配置 | 技术支持电话 |

---

## 报警级别映射

| 代码值 | 显示文本 | 颜色标识 |
|--------|---------|---------|
| critical | 严重 | 红色 |
| warning | 警告 | 橙色 |
| info | 提示 | 蓝色 |

---

## 使用建议

1. **短信模板**：建议使用模板1（简洁版）作为默认短信模板，模板3用于严重报警
2. **邮件模板**：建议使用模板2（HTML格式）作为默认邮件模板，模板3用于严重报警
3. **变量替换**：在发送通知时，需要将模板中的变量替换为实际数据
4. **链接生成**：{alarm_detail_url} 需要根据系统配置和报警ID动态生成
5. **平台地址**：{platform_url} 建议在系统配置中统一管理
