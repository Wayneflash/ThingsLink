#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试命令控制功能
向设备发送window状态控制命令
"""

import paho.mqtt.client as mqtt
import json
import time

# MQTT配置
MQTT_BROKER = "localhost"
MQTT_PORT = 1883
MQTT_USERNAME = "admin"
MQTT_PASSWORD = "public"

# 设备信息
DEVICE_CODE = "888866666"

# 命令主题
TOPIC_COMMAND = f"ssc/{DEVICE_CODE}/command"

def send_window_command(window_value):
    """发送window状态命令"""
    # 创建MQTT客户端
    client = mqtt.Client(f"command_sender_{int(time.time())}")
    client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    
    try:
        # 连接MQTT服务器
        client.connect(MQTT_BROKER, MQTT_PORT, 60)
        client.loop_start()
        
        # 构造命令数据
        command_data = {
            "did": DEVICE_CODE,
            "content": [
                {
                    "addr": "window",
                    "addrv": str(window_value),  # window状态值 (0 或 1)
                    "ctime": time.strftime("%Y/%m/%d %H:%M:%S", time.localtime()),
                    "pid": DEVICE_CODE
                }
            ]
        }
        
        # 发送命令
        payload = json.dumps(command_data, ensure_ascii=False)
        result = client.publish(TOPIC_COMMAND, payload, qos=1)
        
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print(f"✓ 成功发送命令到 {TOPIC_COMMAND}")
            print(f"  命令内容: {payload}")
            print(f"  设置window状态为: {window_value}")
        else:
            print(f"✗ 命令发送失败，错误码: {result.rc}")
        
        # 等待片刻以便观察设备响应
        time.sleep(2)
        
    except Exception as e:
        print(f"✗ 发送命令时出错: {e}")
    finally:
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    print("=== 命令控制功能测试 ===")
    print(f"目标设备: {DEVICE_CODE}")
    print(f"命令主题: {TOPIC_COMMAND}")
    print()
    
    # 测试发送window状态命令
    print("发送命令: 设置window为开启状态 (值: 1)")
    send_window_command(1)
    print()
    
    time.sleep(5)  # 等待
    
    print("发送命令: 设置window为关闭状态 (值: 0)")
    send_window_command(0)
    print()
    
    print("命令发送完成。请检查设备模拟器终端的输出。")