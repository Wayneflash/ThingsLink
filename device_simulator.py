#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IoT 设备模拟器 - 模拟温湿度传感器上报数据
使用 ThingsFusion 协议格式
"""

import json
import time
import random
from datetime import datetime
import paho.mqtt.client as mqtt

# MQTT 配置
MQTT_BROKER = "localhost"  # EMQX 地址
MQTT_PORT = 1883
MQTT_USERNAME = "admin"
MQTT_PASSWORD = "public"

# 设备配置
DEVICE_CODE = "TEM1111"
DEVICE_TOPIC_REPORT = f"ssc/{DEVICE_CODE}/report"
DEVICE_TOPIC_COMMAND = f"ssc/{DEVICE_CODE}/command"

# 模拟数据范围
TEMP_RANGE = (15.0, 35.0)  # 温度范围
HUM_RANGE = (30.0, 90.0)   # 湿度范围


class DeviceSimulator:
    """设备模拟器"""
    
    def __init__(self):
        self.client = mqtt.Client(client_id=f"simulator_{DEVICE_CODE}")
        self.client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.window_status = 0  # 窗户状态：0-关闭，1-打开
    
    def on_connect(self, client, userdata, flags, rc):
        """连接回调"""
        if rc == 0:
            print(f"✅ 已连接到 MQTT Broker: {MQTT_BROKER}:{MQTT_PORT}")
            # 订阅命令主题
            client.subscribe(DEVICE_TOPIC_COMMAND)
            print(f"✅ 已订阅命令主题: {DEVICE_TOPIC_COMMAND}")
        else:
            print(f"❌ 连接失败，返回码: {rc}")
    
    def on_message(self, client, userdata, msg):
        """接收命令回调"""
        try:
            payload = msg.payload.decode('utf-8')
            print(f"\n📨 收到命令 - Topic: {msg.topic}")
            print(f"   Payload: {payload}")
            
            # 解析命令
            command = json.loads(payload)
            for content in command.get('content', []):
                if content['addr'] == 'window':
                    self.window_status = int(content['addrv'])
                    print(f"   执行命令: 窗户 -> {'打开' if self.window_status == 1 else '关闭'}")
        except Exception as e:
            print(f"❌ 处理命令失败: {e}")
    
    def generate_data(self):
        """生成模拟数据"""
        # 随机温湿度
        temperature = round(random.uniform(*TEMP_RANGE), 2)
        humidity = round(random.uniform(*HUM_RANGE), 2)
        
        # 当前时间 (UTC格式)
        current_time = datetime.utcnow().strftime("%Y/%m/%d %H:%M:%S")
        
        # 构建上报数据（ThingsFusion 格式）
        data = {
            "did": DEVICE_CODE,
            "content": [
                {
                    "addr": "tem",
                    "addrv": str(temperature),
                    "ctime": current_time,
                    "pid": DEVICE_CODE
                },
                {
                    "addr": "hum",
                    "addrv": str(humidity),
                    "ctime": current_time,
                    "pid": DEVICE_CODE
                },
                {
                    "addr": "window",
                    "addrv": str(self.window_status),
                    "ctime": current_time,
                    "pid": DEVICE_CODE
                }
            ]
        }
        return data
    
    def publish_data(self):
        """上报数据"""
        data = self.generate_data()
        payload = json.dumps(data, ensure_ascii=False)
        
        result = self.client.publish(DEVICE_TOPIC_REPORT, payload, qos=1)
        
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print(f"\n📤 数据上报成功:")
            print(f"   温度: {data['content'][0]['addrv']}℃")
            print(f"   湿度: {data['content'][1]['addrv']}%")
            print(f"   窗户: {'打开' if self.window_status == 1 else '关闭'}")
            print(f"   时间: {data['content'][0]['ctime']}")
        else:
            print(f"❌ 数据上报失败，错误码: {result.rc}")
    
    def start(self, interval=60):
        """启动模拟器"""
        try:
            # 连接 MQTT Broker
            print(f"🔌 正在连接 MQTT Broker: {MQTT_BROKER}:{MQTT_PORT}...")
            self.client.connect(MQTT_BROKER, MQTT_PORT, 60)
            
            # 启动网络循环
            self.client.loop_start()
            
            print(f"\n⏰ 开始模拟数据上报，间隔: {interval}秒")
            print("=" * 60)
            
            # 定时上报数据
            while True:
                self.publish_data()
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n\n⛔ 用户中断，停止模拟器")
        except Exception as e:
            print(f"\n❌ 模拟器运行错误: {e}")
        finally:
            self.client.loop_stop()
            self.client.disconnect()
            print("👋 模拟器已停止")


def main():
    print("=" * 60)
    print("     IoT 设备模拟器 - 温湿度传感器 (TEM1111)")
    print("=" * 60)
    print(f"设备编码: {DEVICE_CODE}")
    print(f"上报主题: {DEVICE_TOPIC_REPORT}")
    print(f"命令主题: {DEVICE_TOPIC_COMMAND}")
    print("=" * 60)
    
    simulator = DeviceSimulator()
    simulator.start(interval=10)  # 每10秒上报一次数据


if __name__ == "__main__":
    main()
