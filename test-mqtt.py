#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
MQTT 数据上报测试脚本
模拟设备上报数据，验证后端接收和存储
"""

import paho.mqtt.client as mqtt
import json
import time
from datetime import datetime

# MQTT 配置
BROKER = "localhost"
PORT = 1883
USERNAME = "admin"
PASSWORD = "public"

# 设备信息
DEVICE_ID = "TEM1111"
TOPIC = f"ssc/{DEVICE_ID}/report"

# 颜色输出
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    YELLOW = '\033[93m'
    END = '\033[0m'

def print_success(msg):
    print(f"{Colors.GREEN}✓ {msg}{Colors.END}")

def print_error(msg):
    print(f"{Colors.RED}✗ {msg}{Colors.END}")

def print_info(msg):
    print(f"{Colors.CYAN}ℹ {msg}{Colors.END}")

def print_title(msg):
    print(f"\n{Colors.YELLOW}{'='*10} {msg} {'='*10}{Colors.END}")

# MQTT 回调函数
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print_success(f"MQTT 连接成功: {BROKER}:{PORT}")
    else:
        print_error(f"MQTT 连接失败，错误码: {rc}")

def on_publish(client, userdata, mid):
    print_success(f"数据上报成功，消息ID: {mid}")

def on_disconnect(client, userdata, rc):
    if rc != 0:
        print_error(f"意外断开连接，错误码: {rc}")

# 生成测试数据
def generate_report_data():
    """生成符合 API 文档格式的上报数据"""
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    data = {
        "did": DEVICE_ID,
        "content": [
            {
                "addr": "tem",
                "addrv": "25.3",
                "ctime": now,
                "pid": DEVICE_ID
            },
            {
                "addr": "hum",
                "addrv": "65.8",
                "ctime": now,
                "pid": DEVICE_ID
            },
            {
                "addr": "window",
                "addrv": "0",
                "ctime": now,
                "pid": DEVICE_ID
            }
        ]
    }
    
    return data

def main():
    print(f"{Colors.YELLOW}")
    print("=" * 50)
    print("   MQTT 数据上报测试")
    print("=" * 50)
    print(f"{Colors.END}")
    
    # 创建 MQTT 客户端
    client = mqtt.Client(client_id="test_device_publisher")
    client.username_pw_set(USERNAME, PASSWORD)
    
    # 设置回调
    client.on_connect = on_connect
    client.on_publish = on_publish
    client.on_disconnect = on_disconnect
    
    try:
        # 连接 MQTT 服务器
        print_info(f"正在连接 MQTT 服务器: {BROKER}:{PORT}")
        client.connect(BROKER, PORT, 60)
        client.loop_start()
        
        # 等待连接成功
        time.sleep(1)
        
        # 发送 3 条测试数据
        print_title("开始上报数据")
        
        for i in range(3):
            print_info(f"准备上报第 {i+1} 条数据...")
            
            # 生成数据
            data = generate_report_data()
            payload = json.dumps(data, ensure_ascii=False)
            
            print_info(f"上报主题: {TOPIC}")
            print_info(f"数据内容: {payload}")
            
            # 发布数据
            result = client.publish(TOPIC, payload, qos=1)
            
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print_success(f"第 {i+1} 条数据发送成功")
            else:
                print_error(f"第 {i+1} 条数据发送失败")
            
            # 等待 2 秒再发送下一条
            if i < 2:
                time.sleep(2)
        
        # 等待消息发送完成
        time.sleep(2)
        
        print_title("测试完成")
        print_success("所有测试数据已上报！")
        print_info("请检查后端日志确认数据是否被正确接收和存储")
        print_info("可以通过以下 API 查询数据：")
        print(f"{Colors.CYAN}  POST http://localhost:8080/device-data/latest{Colors.END}")
        print(f"{Colors.CYAN}  Body: {{\"deviceCode\": \"{DEVICE_ID}\"}}{Colors.END}")
        
    except Exception as e:
        print_error(f"测试失败: {e}")
    
    finally:
        client.loop_stop()
        client.disconnect()
        print_info("MQTT 连接已关闭")

if __name__ == "__main__":
    main()
