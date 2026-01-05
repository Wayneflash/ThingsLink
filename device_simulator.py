#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IoT设备模拟器
用于模拟设备连接MQTT服务器、上报数据和接收命令
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import threading
import signal
import sys

# MQTT配置 - 从后端配置获取
MQTT_BROKER = "localhost"  # 实际部署时请替换为实际服务器地址
MQTT_PORT = 1883
MQTT_USERNAME = "admin"
MQTT_PASSWORD = "admin123."

# 设备信息
DEVICE_CODE = "888866666"  # 你的设备编码
DEVICE_NAME = "模拟设备-888866666"

# 设备状态
device_status = {
    'window': 0  # 窗户状态: 0-关闭, 1-打开
}

# MQTT主题
TOPIC_REPORT = f"ssc/{DEVICE_CODE}/report"  # 数据上报主题
TOPIC_COMMAND = f"ssc/{DEVICE_CODE}/command"  # 命令接收主题

# 颜色输出
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    END = '\033[0m'

def print_success(msg):
    print(f"{Colors.GREEN}✓ {msg}{Colors.END}")

def print_error(msg):
    print(f"{Colors.RED}✗ {msg}{Colors.END}")

def print_info(msg):
    print(f"{Colors.CYAN}ℹ {msg}{Colors.END}")

def print_warning(msg):
    print(f"{Colors.YELLOW}⚠ {msg}{Colors.END}")

def print_title(msg):
    print(f"\n{Colors.MAGENTA}{'='*10} {msg} {'='*10}{Colors.END}")

# MQTT回调函数
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print_success(f"MQTT连接成功到 {MQTT_BROKER}:{MQTT_PORT}")
        # 订阅命令主题
        client.subscribe(TOPIC_COMMAND)
        print_info(f"已订阅命令主题: {TOPIC_COMMAND}")
    else:
        print_error(f"MQTT连接失败，错误码: {rc}")

def on_message(client, userdata, msg):
    """处理接收到的消息"""
    try:
        payload = msg.payload.decode('utf-8')
        print_info(f"收到消息 - 主题: {msg.topic}")
        print(f"   内容: {payload}")
        
        # 解析命令
        if msg.topic == TOPIC_COMMAND:
            try:
                command_data = json.loads(payload)
                print_success("成功解析命令")
                
                # 这里可以添加命令处理逻辑
                # 示例：处理设备控制命令
                if 'content' in command_data:
                    for cmd in command_data['content']:
                        addr = cmd.get('addr', '')
                        addrv = cmd.get('addrv', '')
                        print_info(f"执行命令 - 地址: {addr}, 值: {addrv}")
                        
                        # 处理设备控制命令
                        if addr == 'window':
                            # 更新window状态
                            device_status['window'] = int(addrv)
                            print_success(f"window状态已更新为: {device_status['window']}")
                            # 立即上报新状态
                            publish_data(client)
                        else:
                            print_warning(f"未知命令地址: {addr}")
                else:
                    print_warning("命令数据格式不正确，缺少content字段")
                        
            except json.JSONDecodeError:
                print_error("命令解析失败")
                
    except Exception as e:
        print_error(f"处理消息时出错: {e}")

def on_publish(client, userdata, mid):
    print_success(f"数据上报成功，消息ID: {mid}")

def on_disconnect(client, userdata, rc):
    if rc != 0:
        print_error(f"MQTT连接断开，错误码: {rc}")

def generate_sample_data():
    """生成示例数据，根据物模型"""
    now = datetime.now().strftime("%Y/%m/%d %H:%M:%S")
    
    # 设备状态 - 包括可控制的window状态
    global device_status
    if 'window' not in device_status:
        device_status['window'] = 0  # 默认关闭
    
    # 示例数据 - 可以根据实际的物模型调整
    data = {
        "did": DEVICE_CODE,
        "content": [
            {
                "addr": "tem",      # 温度
                "addrv": f"{random.uniform(18.0, 35.0):.2f}",  # 随机温度值
                "ctime": now,
                "pid": DEVICE_CODE
            },
            {
                "addr": "hum",      # 湿度
                "addrv": f"{random.uniform(30.0, 80.0):.2f}",  # 随机湿度值
                "ctime": now,
                "pid": DEVICE_CODE
            },
            {
                "addr": "window",      # 窗户状态
                "addrv": f"{device_status['window']}",  # 窗户状态值
                "ctime": now,
                "pid": DEVICE_CODE
            }
        ]
    }
    
    return data

def publish_data(client):
    """上报数据到平台"""
    try:
        data = generate_sample_data()
        payload = json.dumps(data, ensure_ascii=False)
        
        result = client.publish(TOPIC_REPORT, payload, qos=1)
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print_info(f"上报数据到 {TOPIC_REPORT}")
            print(f"   数据: {payload}")
        else:
            print_error(f"数据上报失败，错误码: {result.rc}")
            
    except Exception as e:
        print_error(f"上报数据时出错: {e}")

def signal_handler(sig, frame):
    """处理Ctrl+C退出"""
    print_warning("\n正在断开MQTT连接...")
    client.disconnect()
    print_info("设备模拟器已退出")
    sys.exit(0)

if __name__ == "__main__":
    print_title("IoT设备模拟器启动")
    print_info(f"设备编码: {DEVICE_CODE}")
    print_info(f"设备名称: {DEVICE_NAME}")
    print_info(f"MQTT服务器: {MQTT_BROKER}:{MQTT_PORT}")
    print_info(f"上报主题: {TOPIC_REPORT}")
    print_info(f"命令主题: {TOPIC_COMMAND}")
    
    # 创建MQTT客户端
    client = mqtt.Client(f"device_{DEVICE_CODE}")
    client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    
    # 设置回调函数
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_publish = on_publish
    client.on_disconnect = on_disconnect
    
    try:
        # 连接MQTT服务器
        client.connect(MQTT_BROKER, MQTT_PORT, 60)
        
        # 设置信号处理器，用于优雅退出
        signal.signal(signal.SIGINT, signal_handler)
        
        # 启动网络循环
        client.loop_start()
        
        print_success("设备模拟器启动成功，开始周期性上报数据...")
        print_info("按 Ctrl+C 退出程序")
        
        # 周期性上报数据
        while True:
            publish_data(client)
            time.sleep(10)  # 每10秒上报一次数据
            
    except Exception as e:
        print_error(f"启动设备模拟器时出错: {e}")
        sys.exit(1)