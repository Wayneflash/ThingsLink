#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IoT设备模拟器
用于模拟设备连接MQTT服务器、上报数据和接收命令
支持多设备模拟
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

# MQTT配置 - 从后端配置获取
MQTT_BROKER = "127.0.0.1"  # 实际部署时请替换为实际服务器地址
MQTT_PORT = 1883
MQTT_USERNAME = "admin"
MQTT_PASSWORD = "admin123."  # 与EMQX配置保持一致

# 设备列表
# protocol: "MQTT1.0" 或 "MQTT2.0"，默认为 "MQTT1.0"
DEVICES = [
    {
        'code': "888866666",
        'name': "模拟设备-88887777",
        'type': "environment",  # 环境监测设备
        'protocol': "MQTT2.0",  # 使用MQTT1.0协议
        'status': {'window': 0}  # 窗户状态: 0-关闭, 1-打开
    },
    {
        'code': "88889999",
        'name': "模拟设备-88889999",
        'type': "environment",  # 环境监测设备
        'protocol': "MQTT2.0",  # 使用MQTT2.0协议（测试用）
        'status': {'window': 0}  # 窗户状态: 0-关闭, 1-打开
    },
    {
        'code': "88882222",
        'name': "模拟设备-88882222",
        'type': "air_quality",  # 空气质量监测设备
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {}  # 空气质量设备暂无控制属性
    },
    # 电表设备
    {
        'code': "DIANBIAO1",
        'name': "智能电表-1",
        'type': "electric_meter",  # 电表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_energy': 1500.0}  # 初始累计电量(kWh)
    },
    {
        'code': "DIANBIAO2",
        'name': "智能电表-2",
        'type': "electric_meter",  # 电表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_energy': 2300.0}  # 初始累计电量(kWh)
    },
    # 水表设备
    {
        'code': "SHUIBIAO1",
        'name': "智能水表-1",
        'type': "water_meter",  # 水表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_flow': 850.0, 'valve_status': 1}  # 初始累计流量(m³)，阀门状态
    },
    {
        'code': "SHUIBIAO2",
        'name': "智能水表-2",
        'type': "water_meter",  # 水表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_flow': 1200.0, 'valve_status': 1}  # 初始累计流量(m³)，阀门状态
    },
    # 气表设备
    {
        'code': "QIBIAO1",
        'name': "智能气表-1",
        'type': "gas_meter",  # 气表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_gas': 450.0}  # 初始累计用气量(m³)
    },
    {
        'code': "QIBIAO2",
        'name': "智能气表-2",
        'type': "gas_meter",  # 气表
        'protocol': "MQTT1.0",  # 使用MQTT1.0协议
        'status': {'total_gas': 680.0}  # 初始累计用气量(m³)
    }
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

# 设备模拟器类
class DeviceSimulator:
    def __init__(self, device_info):
        self.device_code = device_info['code']
        self.device_name = device_info['name']
        self.device_type = device_info.get('type', 'environment')  # 设备类型：environment（环境监测）或 air_quality（空气质量）
        self.protocol = device_info.get('protocol', 'MQTT1.0')  # 协议类型：MQTT1.0 或 MQTT2.0
        self.device_status = device_info['status']
        self.client = None
        
        # MQTT主题
        self.topic_report = f"ssc/{self.device_code}/report"
        self.topic_command = f"ssc/{self.device_code}/command"
        
        # 消息ID计数器（用于MQTT2.0）
        self.message_id_counter = 0
        
    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print_success(f"[{self.device_code}] MQTT连接成功")
            # 订阅命令主题
            client.subscribe(self.topic_command)
            print_info(f"[{self.device_code}] 已订阅命令主题: {self.topic_command}")
        else:
            print_error(f"[{self.device_code}] MQTT连接失败，错误码: {rc}")

    def on_message(self, client, userdata, msg):
        """处理接收到的消息"""
        try:
            payload = msg.payload.decode('utf-8')
            print_info(f"[{self.device_code}] 收到消息 - 主题: {msg.topic}")
            print(f"   内容: {payload}")
            
            # 解析命令
            if msg.topic == self.topic_command:
                try:
                    command_data = json.loads(payload)
                    print_success(f"[{self.device_code}] 成功解析命令")
                    
                    # 判断命令格式（MQTT1.0 或 MQTT2.0）
                    commands_to_process = []
                    
                    if 'params' in command_data:
                        # MQTT2.0 格式
                        if command_data.get('params') and len(command_data['params']) > 0:
                            param = command_data['params'][0]
                            if 'properties' in param:
                                for prop in param['properties']:
                                    commands_to_process.append({
                                        'addr': prop.get('name', ''),
                                        'addrv': str(prop.get('value', ''))
                                    })
                    elif 'content' in command_data:
                        # MQTT1.0 格式
                        for cmd in command_data['content']:
                            commands_to_process.append({
                                'addr': cmd.get('addr', ''),
                                'addrv': cmd.get('addrv', '')
                            })
                    
                    # 处理命令
                    if commands_to_process:
                        for cmd in commands_to_process:
                            addr = cmd.get('addr', '')
                            addrv = cmd.get('addrv', '')
                            print_info(f"[{self.device_code}] 执行命令 - 地址: {addr}, 值: {addrv}")
                            
                            # 处理设备控制命令
                            if addr == 'window':
                                # 更新window状态
                                self.device_status['window'] = int(addrv)
                                print_success(f"[{self.device_code}] window状态已更新为: {self.device_status['window']}")
                                # 立即上报新状态
                                self.publish_data()
                            elif addr == 'valve_status':
                                # 更新阀门状态（水表）
                                self.device_status['valve_status'] = int(addrv)
                                print_success(f"[{self.device_code}] 阀门状态已更新为: {self.device_status['valve_status']}")
                                # 立即上报新状态
                                self.publish_data()
                            else:
                                print_warning(f"[{self.device_code}] 未知命令地址: {addr}")
                    else:
                        print_warning(f"[{self.device_code}] 命令数据格式不正确，无法解析命令")
                        
                except json.JSONDecodeError:
                    print_error(f"[{self.device_code}] 命令解析失败")
                    
        except Exception as e:
            print_error(f"[{self.device_code}] 处理消息时出错: {e}")

    def on_publish(self, client, userdata, mid):
        print_success(f"[{self.device_code}] 数据上报成功，消息ID: {mid}")

    def on_disconnect(self, client, userdata, rc):
        if rc != 0:
            # 错误码说明：0=正常断开，其他=异常断开
            # 7=网络错误或服务器主动断开
            error_msg = {
                1: "协议版本错误",
                2: "客户端ID无效",
                3: "服务器不可用",
                4: "用户名或密码错误",
                5: "未授权",
                7: "网络错误或服务器主动断开"
            }.get(rc, f"未知错误码: {rc}")
            print_error(f"[{self.device_code}] MQTT连接断开，错误码: {rc} ({error_msg})")

    def generate_sample_data(self):
        """生成示例数据，根据物模型"""
        now = datetime.now().strftime("%Y/%m/%d %H:%M:%S")
        
        # 根据设备类型生成不同的数据
        device_type = getattr(self, 'device_type', 'environment')
        
        content = []
        status_msg = ""
        
        if device_type == "environment":
            # 环境监测设备：温度、湿度、窗户状态
            if 'window' not in self.device_status:
                self.device_status['window'] = 0  # 默认关闭
            
            # 报警阈值：温度 > 25℃，湿度 > 50%
            # 模拟数据在阈值上下波动，用于测试报警触发和恢复
            
            # 温度：在22-28℃波动，会超过25℃阈值
            temperature = random.uniform(22.0, 28.0)
            
            # 湿度：在45-55%波动，会超过50%阈值
            humidity = random.uniform(45.0, 55.0)
            
            content = [
                {
                    "addr": "tem",      # 温度
                    "addrv": f"{temperature:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "hum",      # 湿度
                    "addrv": f"{humidity:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "window",      # 窗户状态
                    "addrv": f"{self.device_status['window']}",  # 窗户状态值
                    "ctime": now,
                    "pid": self.device_code
                }
            ]
            
            # 打印当前数据情况，方便观察报警触发/恢复
            temp_status = "🔥 超阈" if temperature > 25.0 else "✅ 正常"
            hum_status = "💧 超阈" if humidity > 50.0 else "✅ 正常"
            status_msg = f"温度: {temperature:.2f}℃ {temp_status} | 湿度: {humidity:.2f}% {hum_status}"
            
        elif device_type == "air_quality":
            # 空气质量监测设备：CO2、O2
            # CO2：在400-1200 ppm 波动（正常范围：400-1000 ppm）
            co2 = random.uniform(400.0, 1200.0)
            
            # O2：在19.5-21.0% 波动（正常范围：20.8-21.0%）
            o2 = random.uniform(19.5, 21.0)
            
            content = [
                {
                    "addr": "CO2",      # 二氧化碳
                    "addrv": f"{co2:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "O2",      # 氧气
                    "addrv": f"{o2:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                }
            ]
            
            # 打印当前数据情况
            co2_status = "⚠️ 偏高" if co2 > 1000.0 else "✅ 正常"
            o2_status = "⚠️ 偏低" if o2 < 20.0 else "✅ 正常"
            status_msg = f"CO2: {co2:.2f}ppm {co2_status} | O2: {o2:.2f}% {o2_status}"
            
        elif device_type == "electric_meter":
            # 智能电表：电压、电流、有功功率、无功功率、功率因数、总有功电能(累计值)
            
            # 初始化累计值
            if 'total_energy' not in self.device_status:
                self.device_status['total_energy'] = 1000.0  # 默认初始值
            
            # 电压：220V左右，正常范围 200-240V
            voltage = random.uniform(210.0, 230.0)
            
            # 电流：5-50A，根据用电情况变化
            current = random.uniform(5.0, 50.0)
            
            # 有功功率 = 电压 × 电流 × 功率因数 / 1000 (kW)
            power_factor = random.uniform(0.85, 0.98)  # 功率因数 0.85-0.98
            active_power = (voltage * current * power_factor) / 1000.0  # kW
            
            # 无功功率：有功功率的10-20%
            reactive_power = active_power * random.uniform(0.10, 0.20)
            
            # 累计电量：每次增加 0.1-0.5 kWh（模拟3分钟用电量）
            energy_increment = random.uniform(0.1, 0.5)
            self.device_status['total_energy'] += energy_increment
            
            content = [
                {
                    "addr": "voltage",
                    "addrv": f"{voltage:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "current",
                    "addrv": f"{current:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "active_power",
                    "addrv": f"{active_power:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "reactive_power",
                    "addrv": f"{reactive_power:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "power_factor",
                    "addrv": f"{power_factor:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "total_energy",
                    "addrv": f"{self.device_status['total_energy']:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                }
            ]
            
            status_msg = f"电压: {voltage:.1f}V | 电流: {current:.1f}A | 功率: {active_power:.2f}kW | 累计: {self.device_status['total_energy']:.2f}kWh"
            
        elif device_type == "water_meter":
            # 智能水表：总流量(累计值)、瞬时流量、水压、阀门状态
            
            # 初始化累计值
            if 'total_flow' not in self.device_status:
                self.device_status['total_flow'] = 500.0  # 默认初始值
            
            # 初始化阀门状态
            if 'valve_status' not in self.device_status:
                self.device_status['valve_status'] = 1  # 默认打开
            
            # 瞬时流量：0.5-5.0 m³/h，根据用水情况变化
            instantaneous_flow = random.uniform(0.5, 5.0) if self.device_status['valve_status'] == 1 else 0.0
            
            # 水压：0.2-0.6 MPa，正常范围
            water_pressure = random.uniform(0.2, 0.6)
            
            # 累计流量：每次增加 0.01-0.05 m³（模拟3分钟用水量）
            flow_increment = (instantaneous_flow / 60.0) * 3.0  # 3分钟用水量
            self.device_status['total_flow'] += flow_increment
            
            content = [
                {
                    "addr": "total_flow",
                    "addrv": f"{self.device_status['total_flow']:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "instantaneous_flow",
                    "addrv": f"{instantaneous_flow:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "water_pressure",
                    "addrv": f"{water_pressure:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "valve_status",
                    "addrv": f"{self.device_status['valve_status']}",
                    "ctime": now,
                    "pid": self.device_code
                }
            ]
            
            status_msg = f"瞬时流量: {instantaneous_flow:.2f}m³/h | 水压: {water_pressure:.2f}MPa | 累计: {self.device_status['total_flow']:.2f}m³"
            
        elif device_type == "gas_meter":
            # 智能气表：总用气量(累计值)、瞬时流量、气压、气体温度
            
            # 初始化累计值
            if 'total_gas' not in self.device_status:
                self.device_status['total_gas'] = 300.0  # 默认初始值
            
            # 瞬时流量：0.2-3.0 m³/h，根据用气情况变化
            instantaneous_flow = random.uniform(0.2, 3.0)
            
            # 气压：200-300 kPa，正常范围
            gas_pressure = random.uniform(200.0, 300.0)
            
            # 气体温度：15-25℃，常温范围
            gas_temperature = random.uniform(15.0, 25.0)
            
            # 累计用气量：每次增加 0.005-0.02 m³（模拟3分钟用气量）
            gas_increment = (instantaneous_flow / 60.0) * 3.0  # 3分钟用气量
            self.device_status['total_gas'] += gas_increment
            
            content = [
                {
                    "addr": "total_gas",
                    "addrv": f"{self.device_status['total_gas']:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "instantaneous_flow",
                    "addrv": f"{instantaneous_flow:.3f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "gas_pressure",
                    "addrv": f"{gas_pressure:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                },
                {
                    "addr": "gas_temperature",
                    "addrv": f"{gas_temperature:.2f}",
                    "ctime": now,
                    "pid": self.device_code
                }
            ]
            
            status_msg = f"瞬时流量: {instantaneous_flow:.2f}m³/h | 气压: {gas_pressure:.0f}kPa | 温度: {gas_temperature:.1f}℃ | 累计: {self.device_status['total_gas']:.2f}m³"
        
        # 根据协议类型生成不同格式的数据
        if self.protocol == "MQTT2.0":
            data = self.generate_v2_data(content, status_msg)
        else:
            # MQTT1.0 格式（默认）
            data = {
                "did": self.device_code,
                "content": content
            }
            print(f"   [{self.device_code}] {status_msg}")
        
        return data
    
    def generate_v2_data(self, content_list, status_msg):
        """生成MQTT2.0格式的数据"""
        import time
        
        # 生成消息ID
        self.message_id_counter += 1
        msg_id = str(self.message_id_counter % 4294967296)  # 限制在32位整数范围内
        
        # 构建properties列表
        properties = []
        current_timestamp = int(time.time())  # Unix时间戳（秒）
        
        for item in content_list:
            property_item = {
                "name": item["addr"],
                "value": float(item["addrv"]) if self._is_numeric(item["addrv"]) else item["addrv"],
                "timestamp": current_timestamp
            }
            properties.append(property_item)
        
        # MQTT2.0格式
        data = {
            "id": msg_id,
            "version": "V1.0",
            "ack": 1,
            "params": [
                {
                    "clientID": self.device_code,
                    "properties": properties
                }
            ]
        }
        
        print(f"   [{self.device_code}] [MQTT2.0] {status_msg}")
        return data
    
    def _is_numeric(self, value):
        """判断字符串是否为数字"""
        try:
            float(value)
            return True
        except ValueError:
            return False

    def publish_data(self):
        """上报数据到平台"""
        try:
            data = self.generate_sample_data()
            payload = json.dumps(data, ensure_ascii=False)
            
            result = self.client.publish(self.topic_report, payload, qos=1)
            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                print_info(f"[{self.device_code}] 上报数据到 {self.topic_report}")
                # print(f"   数据: {payload}")  # 注释掉，避免输出过多
            else:
                print_error(f"[{self.device_code}] 数据上报失败，错误码: {result.rc}")
            
        except Exception as e:
            print_error(f"[{self.device_code}] 上报数据时出错: {e}")

    def run(self):
        """运行设备模拟器"""
        print_title(f"设备模拟器启动 - {self.device_name}")
        print_info(f"设备编码: {self.device_code}")
        print_info(f"设备名称: {self.device_name}")
        print_info(f"MQTT服务器: {MQTT_BROKER}:{MQTT_PORT}")
        print_info(f"上报主题: {self.topic_report}")
        print_info(f"命令主题: {self.topic_command}")
        
        # 创建MQTT客户端（兼容paho-mqtt 2.0+）
        # 使用UUID确保客户端ID唯一，避免多次运行时的冲突
        import uuid
        unique_client_id = f"device_{self.device_code}_{uuid.uuid4().hex[:8]}"
        print_info(f"客户端ID: {unique_client_id}")
        
        try:
            # paho-mqtt 2.0+ 需要指定callback_api_version
            self.client = mqtt.Client(
                client_id=unique_client_id,
                callback_api_version=mqtt.CallbackAPIVersion.VERSION1,
                clean_session=True  # 使用clean session，避免旧会话冲突
            )
        except AttributeError:
            # 兼容旧版本（1.x）
            self.client = mqtt.Client(unique_client_id, clean_session=True)
        
        self.client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
        
        # 设置回调函数
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_publish = self.on_publish
        self.client.on_disconnect = self.on_disconnect
        
        try:
            # 连接MQTT服务器
            print_info(f"[{self.device_code}] 正在连接MQTT服务器...")
            # 设置keepalive为60秒，避免连接超时
            self.client.connect(MQTT_BROKER, MQTT_PORT, keepalive=60)
            
            # 启动网络循环
            self.client.loop_start()
            
            # 等待连接建立
            wait_count = 0
            while not self.client.is_connected() and wait_count < 10:
                time.sleep(0.5)
                wait_count += 1
            
            if not self.client.is_connected():
                print_error(f"[{self.device_code}] MQTT连接超时，请检查服务器是否运行")
                return
            
            print_success(f"[{self.device_code}] 设备模拟器启动成功，开始周期性上报数据...")
            
            # 等待一下确保连接稳定
            time.sleep(1)
            
            # 周期性上报数据
            while True:
                if self.client.is_connected():
                    self.publish_data()
                else:
                    print_warning(f"[{self.device_code}] MQTT连接已断开，尝试重连...")
                    try:
                        self.client.reconnect()
                    except:
                        pass
                time.sleep(180)  # 每3分钟上报一次数据
            
        except Exception as e:
            print_error(f"[{self.device_code}] 启动设备模拟器时出错: {e}")

def signal_handler(sig, frame):
    """处理Ctrl+C退出"""
    print_warning("\n正在断开MQTT连接...")
    print_info("设备模拟器已退出")
    sys.exit(0)

if __name__ == "__main__":
    print_title("IoT设备模拟器启动")
    print_info(f"MQTT服务器: {MQTT_BROKER}:{MQTT_PORT}")
    print_info(f"设备数量: {len(DEVICES)}")
    
    # 设置信号处理器，用于优雅退出
    signal.signal(signal.SIGINT, signal_handler)
    
    # 为每个设备创建并启动一个线程
    threads = []
    for device_info in DEVICES:
        simulator = DeviceSimulator(device_info)
        thread = threading.Thread(target=simulator.run, daemon=True)
        thread.start()
        threads.append(thread)
        time.sleep(1)  # 错开启动时间，避免连接冲突
    
    print_success(f"所有设备模拟器已启动，共 {len(DEVICES)} 个设备")
    print_info("按 Ctrl+C 退出程序")
    
    # 等待所有线程结束
    try:
        for thread in threads:
            thread.join()
    except KeyboardInterrupt:
        signal_handler(None, None)
