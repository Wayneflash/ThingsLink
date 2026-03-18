#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IoT设备模拟器 - 远程测试环境
连接开发服务器 117.72.162.225 进行测试
"""

import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import threading
import signal
import sys
import os

# 设置Windows控制台编码为UTF-8（避免中文乱码）
if sys.platform == 'win32':
    try:
        os.system('chcp 65001 >nul 2>&1')
    except:
        pass

# MQTT配置 - 远程开发服务器
MQTT_BROKER = "117.72.162.225"  # 开发服务器地址
MQTT_PORT = 11883  # MQTT端口
MQTT_USERNAME = "admin"
MQTT_PASSWORD = "admin123."  # 与EMQX配置保持一致

# 测试设备列表
DEVICES = [
    {
        'code': "88886666",
        'name': "温湿度传感器-88886666",
        'type': "environment",  # 环境监测设备（温湿度）
        'protocol': "MQTT1.0",
        'status': {'window': 0}  # 窗户状态
    },
    {
        'code': "88887777",
        'name': "温湿度传感器-88887777",
        'type': "environment",  # 环境监测设备（温湿度）
        'protocol': "MQTT1.0",
        'status': {'window': 0}  # 窗户状态
    },
]

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
    print(f"{Colors.GREEN}[OK] {msg}{Colors.END}")

def print_error(msg):
    print(f"{Colors.RED}[ERROR] {msg}{Colors.END}")

def print_info(msg):
    print(f"{Colors.CYAN}[INFO] {msg}{Colors.END}")

def print_warning(msg):
    print(f"{Colors.YELLOW}[WARN] {msg}{Colors.END}")

def print_title(msg):
    print(f"\n{Colors.MAGENTA}{'='*10} {msg} {'='*10}{Colors.END}")

# 设备模拟器类（简化版）
class DeviceSimulator:
    def __init__(self, device_info):
        self.device_code = device_info['code']
        self.device_name = device_info['name']
        self.device_type = device_info.get('type', 'environment')
        self.protocol = device_info.get('protocol', 'MQTT1.0')
        self.device_status = device_info['status']
        self.client = None
        
        # MQTT主题
        self.topic_report = f"ssc/{self.device_code}/report"
        self.topic_command = f"ssc/{self.device_code}/command"
        
    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print_success(f"[{self.device_code}] MQTT连接成功")
            print_info(f"[{self.device_code}] 连接标志: {flags}")
            
            # 订阅命令主题
            result, mid = client.subscribe(self.topic_command, qos=1)
            if result == 0:
                print_success(f"[{self.device_code}] ✓ 订阅命令主题成功")
                print_info(f"[{self.device_code}]   主题: {self.topic_command}")
                print_info(f"[{self.device_code}]   QoS: 1")
                print_info(f"[{self.device_code}]   消息ID: {mid}")
            else:
                print_error(f"[{self.device_code}] ✗ 订阅命令主题失败")
                print_error(f"[{self.device_code}]   错误码: {result}")
                print_error(f"[{self.device_code}]   消息ID: {mid}")
        else:
            print_error(f"[{self.device_code}] MQTT连接失败")
            print_error(f"[{self.device_code}]   错误码: {rc}")
            print_error(f"[{self.device_code}]   错误描述: {self._get_mqtt_error_desc(rc)}")

    def on_message(self, client, userdata, msg):
        try:
            payload = msg.payload.decode('utf-8')
            print_info(f"[{self.device_code}] 收到命令消息")
            print_info(f"[{self.device_code}]   主题: {msg.topic}")
            print_info(f"[{self.device_code}]   QoS: {msg.qos}")
            print_info(f"[{self.device_code}]   retain: {msg.retain}")
            print_info(f"[{self.device_code}]   payload: {payload}")
            
            command_data = json.loads(payload)
            print_info(f"[{self.device_code}] 解析后的命令数据: {json.dumps(command_data, ensure_ascii=False, indent=2)}")
            
            # 处理命令
            if 'content' in command_data:
                print_info(f"[{self.device_code}] 命令内容数量: {len(command_data['content'])}")
                for i, cmd in enumerate(command_data['content']):
                    addr = cmd.get('addr', '')
                    addrv = cmd.get('addrv', '')
                    print_info(f"[{self.device_code}] 执行命令 [{i+1}/{len(command_data['content'])}]: {addr} = {addrv}")
                    
                    # 更新状态
                    if addr in self.device_status:
                        old_value = self.device_status[addr]
                        new_value = float(addrv) if '.' in str(addrv) else int(addrv)
                        self.device_status[addr] = new_value
                        print_success(f"[{self.device_code}] ✓ 状态已更新: {addr} {old_value} -> {new_value}")
                        # 立即上报新状态
                        print_info(f"[{self.device_code}] 正在立即上报新状态...")
                        self.publish_data()
                    else:
                        print_warning(f"[{self.device_code}] 属性 '{addr}' 不在设备状态列表中")
            else:
                print_warning(f"[{self.device_code}] 命令数据中没有 'content' 字段")
                        
        except json.JSONDecodeError as e:
            print_error(f"[{self.device_code}] JSON解析失败: {e}")
            print_error(f"[{self.device_code}] 原始payload: {payload}")
        except Exception as e:
            print_error(f"[{self.device_code}] 处理消息时出错: {e}")
            import traceback
            print_error(f"[{self.device_code}] 错误堆栈: {traceback.format_exc()}")

    def on_publish(self, client, userdata, mid):
        print_success(f"[{self.device_code}] ✓ 数据上报成功")
        print_info(f"[{self.device_code}]   消息ID: {mid}")
        print_info(f"[{self.device_code}]   主题: {self.topic_report}")

    def on_disconnect(self, client, userdata, rc):
        if rc != 0:
            print_error(f"[{self.device_code}] MQTT连接断开")
            print_error(f"[{self.device_code}]   错误码: {rc}")
            print_error(f"[{self.device_code}]   错误描述: {self._get_mqtt_error_desc(rc)}")
        else:
            print_info(f"[{self.device_code}] MQTT正常断开")
    
    def _get_mqtt_error_desc(self, rc):
        """获取MQTT错误码描述"""
        error_map = {
            0: "连接成功",
            1: "连接被拒绝 - 协议版本错误",
            2: "连接被拒绝 - 客户端标识符无效",
            3: "连接被拒绝 - 服务器不可用",
            4: "连接被拒绝 - 用户名或密码错误",
            5: "连接被拒绝 - 未授权",
        }
        return error_map.get(rc, f"未知错误码: {rc}")

    def generate_sample_data(self):
        """生成示例数据"""
        now = datetime.now().strftime("%Y/%m/%d %H:%M:%S")
        content = []
        
        if self.device_type == "environment":
            temperature = random.uniform(20.0, 30.0)
            humidity = random.uniform(40.0, 60.0)
            content = [
                {"addr": "tem", "addrv": f"{temperature:.2f}", "ctime": now, "pid": self.device_code},
                {"addr": "hum", "addrv": f"{humidity:.2f}", "ctime": now, "pid": self.device_code},
                {"addr": "window", "addrv": str(self.device_status.get('window', 0)), "ctime": now, "pid": self.device_code}
            ]
            print(f"   [{self.device_code}] 温度: {temperature:.1f}℃ | 湿度: {humidity:.1f}%")
            
        elif self.device_type == "electric_meter":
            voltage = random.uniform(210.0, 230.0)
            current = random.uniform(5.0, 30.0)
            power = (voltage * current) / 1000.0
            self.device_status['total_energy'] += random.uniform(0.1, 0.3)
            
            content = [
                {"addr": "voltage", "addrv": f"{voltage:.2f}", "ctime": now, "pid": self.device_code},
                {"addr": "current", "addrv": f"{current:.2f}", "ctime": now, "pid": self.device_code},
                {"addr": "active_power", "addrv": f"{power:.3f}", "ctime": now, "pid": self.device_code},
                {"addr": "total_energy", "addrv": f"{self.device_status['total_energy']:.3f}", "ctime": now, "pid": self.device_code}
            ]
            print(f"   [{self.device_code}] 电压: {voltage:.1f}V | 电流: {current:.1f}A | 累计: {self.device_status['total_energy']:.2f}kWh")
        
        return {"did": self.device_code, "content": content}

    def publish_data(self):
        """上报数据"""
        try:
            data = self.generate_sample_data()
            payload = json.dumps(data, ensure_ascii=False)
            print_info(f"[{self.device_code}] 准备上报数据")
            print_info(f"[{self.device_code}]   主题: {self.topic_report}")
            print_info(f"[{self.device_code}]   QoS: 1")
            print_info(f"[{self.device_code}]   数据: {payload[:200]}..." if len(payload) > 200 else f"[{self.device_code}]   数据: {payload}")
            
            result = self.client.publish(self.topic_report, payload, qos=1)
            
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print_info(f"[{self.device_code}] 消息已发送到MQTT broker")
                print_info(f"[{self.device_code}]   消息ID: {result.mid}")
            else:
                print_error(f"[{self.device_code}] 消息发送失败")
                print_error(f"[{self.device_code}]   错误码: {result.rc}")
                
        except Exception as e:
            print_error(f"[{self.device_code}] 上报数据时出错: {e}")
            import traceback
            print_error(f"[{self.device_code}] 错误堆栈: {traceback.format_exc()}")

    def run(self):
        """运行设备模拟器"""
        print_title(f"设备模拟器启动 - {self.device_name}")
        print_info(f"设备编码: {self.device_code}")
        print_info(f"MQTT服务器: {MQTT_BROKER}:{MQTT_PORT}")
        print_info(f"上报主题: {self.topic_report}")
        print_info(f"命令主题: {self.topic_command}")
        
        import uuid
        unique_client_id = f"device_{self.device_code}_{uuid.uuid4().hex[:8]}"
        print_info(f"客户端ID: {unique_client_id}")
        
        try:
            self.client = mqtt.Client(
                client_id=unique_client_id,
                callback_api_version=mqtt.CallbackAPIVersion.VERSION1,
                clean_session=True
            )
        except AttributeError:
            self.client = mqtt.Client(unique_client_id, clean_session=True)
        
        self.client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_publish = self.on_publish
        self.client.on_disconnect = self.on_disconnect
        
        try:
            print_info(f"正在连接 {MQTT_BROKER}:{MQTT_PORT}...")
            self.client.connect(MQTT_BROKER, MQTT_PORT, 60)
            self.client.loop_start()
            
            # 等待连接成功
            time.sleep(2)
            
            # 定时上报数据
            print_info("开始定时上报数据（每3分钟）...")
            while True:
                self.publish_data()
                time.sleep(180)  # 3分钟 = 180秒
                
        except KeyboardInterrupt:
            print_warning(f"\n[{self.device_code}] 用户中断，正在停止...")
            self.client.loop_stop()
            self.client.disconnect()
            print_success(f"[{self.device_code}] 已断开连接")
        except Exception as e:
            print_error(f"[{self.device_code}] 连接失败: {e}")


def main():
    print_title("IoT设备模拟器 - 远程测试环境")
    print_info(f"目标服务器: {MQTT_BROKER}:{MQTT_PORT}")
    print_info(f"可用设备: {len(DEVICES)} 个")
    for i, dev in enumerate(DEVICES):
        print(f"  {i+1}. {dev['name']} ({dev['code']}) - {dev['type']}")
    
    print_info("正在启动所有设备...")
    
    try:
        # 启动所有设备（多线程）
        threads = []
        for dev in DEVICES:
            simulator = DeviceSimulator(dev)
            t = threading.Thread(target=simulator.run, daemon=True)
            t.start()
            threads.append(t)
            time.sleep(1)  # 错开启动时间
        
        print_success(f"已启动 {len(DEVICES)} 个设备")
        print_info("按 Ctrl+C 停止所有设备")
        
        # 主线程等待
        while True:
            time.sleep(1)
            
    except KeyboardInterrupt:
        print_warning("\n用户中断，正在停止所有设备...")
        print_success("所有设备已停止")
    except Exception as e:
        print_error(f"启动设备失败: {e}")


if __name__ == "__main__":
    main()
