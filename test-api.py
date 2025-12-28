#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
IoT Platform API 自动化测试脚本
功能：测试所有后端接口，自动管理 Token
"""

import requests
import json
from datetime import datetime, timedelta
import random

# 配置
BASE_URL = "http://localhost:8080"
token = None

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

# HTTP 请求封装
def api_request(method, path, body=None, need_auth=True):
    """发送 API 请求"""
    global token
    
    headers = {"Content-Type": "application/json"}
    
    if need_auth and token:
        headers["Authorization"] = f"Bearer {token}"
    
    url = f"{BASE_URL}{path}"
    
    # 记录请求日志
    print_info(f"请求: {method} {url}")
    if body:
        print_info(f"入参: {json.dumps(body, ensure_ascii=False, indent=2)}")
    
    try:
        if method.upper() == "GET":
            response = requests.get(url, headers=headers, timeout=10)
        elif method.upper() == "POST":
            response = requests.post(url, headers=headers, json=body, timeout=10)
        elif method.upper() == "PUT":
            response = requests.put(url, headers=headers, json=body, timeout=10)
        elif method.upper() == "DELETE":
            response = requests.delete(url, headers=headers, timeout=10)
        else:
            print_error(f"不支持的请求方法: {method}")
            return None
        
        # 记录响应日志
        response_data = response.json()
        print_info(f"回参: {json.dumps(response_data, ensure_ascii=False, indent=2)}")
        
        return response_data
    except Exception as e:
        print_error(f"请求失败: {e}")
        return None

# 测试用例
def test_auth():
    """1. 认证接口测试"""
    global token
    print_title("1. 认证接口测试")
    
    print_info("测试用户登录...")
    result = api_request("POST", "/auth/login", {
        "username": "admin",
        "password": "admin123456"
    }, need_auth=False)
    
    if result and result.get("code") == 200:
        token = result["data"]["token"]
        print_success(f"登录成功，Token: {token[:20]}...")
        return True
    else:
        print_error(f"登录失败: {result}")
        return False

def test_user():
    """2. 用户管理接口测试"""
    print_title("2. 用户管理接口")
    
    print_info("查询用户列表...")
    result = api_request("POST", "/users/list", {
        "page": 1,
        "pageSize": 10
    })
    if result and result.get("code") == 200:
        total = result['data'].get('total', len(result['data']['list']))
        print_success(f"用户列表查询成功，共 {total} 条")
    else:
        print_error(f"用户列表查询失败: {result}")
    
    print_info("创建新用户...")
    result = api_request("POST", "/users/create", {
        "username": f"test_user_{datetime.now().strftime('%H%M%S')}",
        "password": "123456",
        "realName": "测试用户",
        "groupId": 1,
        "roleIds": [2]
    })
    if result and result.get("code") == 200:
        user_id = result['data']['id']
        print_success(f"用户创建成功，ID: {user_id}")
        
        # 测试更新用户
        print_info("更新用户信息...")
        update_result = api_request("POST", "/users/update", {
            "id": user_id,
            "realName": "更新后的姓名",
            "email": "test@example.com"
        })
        if update_result and update_result.get("code") == 200:
            print_success("用户更新成功")
        
        # 测试修改状态
        print_info("禁用用户...")
        status_result = api_request("POST", "/users/status", {
            "id": user_id,
            "status": "disabled"
        })
        if status_result and status_result.get("code") == 200:
            print_success("用户状态修改成功")
    else:
        print_error(f"用户创建失败: {result}")

def test_product():
    """3. 产品管理接口测试"""
    print_title("3. 产品管理接口")
    
    print_info("查询产品列表...")
    result = api_request("POST", "/products/list", {
        "page": 1,
        "pageSize": 10
    })
    
    product_id = None
    if result and result.get("code") == 200:
        total = result['data'].get('total', len(result['data']['list']))
        print_success(f"产品列表查询成功，共 {total} 条")
        if result['data']['list']:
            product_id = result['data']['list'][0]['id']
            print_info(f"获取到产品ID: {product_id}")
    else:
        print_error(f"产品列表查询失败: {result}")
    
    print_info("创建新产品...")
    result = api_request("POST", "/products/create", {
        "name": f"测试产品_{datetime.now().strftime('%H%M%S')}",
        "code": f"TEST_{random.randint(1000, 9999)}",
        "model": f"MODEL_{random.randint(1000, 9999)}",
        "protocol": "MQTT",
        "category": "传感器",
        "manufacturer": "测试厂商",
        "description": "自动化测试创建的产品"
    })
    
    test_product_id = None
    if result and result.get("code") == 200:
        test_product_id = result['data']['id']
        print_success(f"产品创建成功，ID: {test_product_id}")
        
        print_info("查询产品详情...")
        detail = api_request("POST", "/products/detail", {"id": test_product_id})
        if detail and detail.get("code") == 200:
            print_success(f"产品详情: {detail['data']['productName']}")
        
        # 测试更新产品
        print_info("更新产品信息...")
        update_result = api_request("POST", "/products/update", {
            "id": test_product_id,
            "description": "更新后的描述"
        })
        if update_result and update_result.get("code") == 200:
            print_success("产品更新成功")
    else:
        print_error(f"产品创建失败: {result}")
    
    return product_id or test_product_id

def test_device_group():
    """4. 设备分组接口测试"""
    print_title("4. 设备分组接口")
    
    # 测试树形结构
    print_info("查询设备分组树...")
    tree_result = api_request("POST", "/device-groups/tree")
    if tree_result and tree_result.get("code") == 200:
        tree_data = tree_result['data'].get('tree', [])
        print_success(f"分组树查询成功，共 {len(tree_data)} 个根分组")
    
    # 测试平铺列表
    print_info("查询设备分组列表...")
    result = api_request("POST", "/device-groups/list")
    
    group_id = None
    if result and result.get("code") == 200:
        groups_list = result['data'].get('list', [])
        print_success(f"分组列表查询成功，共 {len(groups_list)} 个分组")
        if groups_list:
            group_id = groups_list[0]['id']
            print_info(f"获取到分组ID: {group_id}")
    else:
        print_error(f"分组列表查询失败: {result}")
    
    print_info("创建新分组...")
    result = api_request("POST", "/device-groups/create", {
        "name": f"测试分组_{datetime.now().strftime('%H%M%S')}",
        "parentId": 0,
        "icon": "📦",
        "sort": 999,
        "description": "自动化测试创建的分组"
    })
    
    if result and result.get("code") == 200:
        new_group_id = result['data']['id']
        print_success(f"分组创建成功，ID: {new_group_id}")
        
        # 测试更新分组
        print_info("更新分组信息...")
        update_result = api_request("POST", "/device-groups/update", {
            "id": new_group_id,
            "description": "更新后的描述"
        })
        if update_result and update_result.get("code") == 200:
            print_success("分组更新成功")
        
        return new_group_id
    else:
        print_error(f"分组创建失败: {result}")
    
    return group_id

def test_device(product_id, group_id):
    """5. 设备管理接口测试"""
    print_title("5. 设备管理接口")
    
    print_info("查询设备列表...")
    result = api_request("POST", "/devices/list", {
        "page": 1,
        "pageSize": 10
    })
    if result and result.get("code") == 200:
        total = result['data'].get('total', 0)
        print_success(f"设备列表查询成功，共 {total} 条")
    else:
        print_error(f"设备列表查询失败: {result}")
    
    print_info("创建新设备...")
    device_code = f"TEST_{random.randint(10000, 99999)}"
    result = api_request("POST", "/devices", {
        "name": f"测试设备_{datetime.now().strftime('%H%M%S')}",
        "code": device_code,
        "productId": product_id,
        "groupId": group_id,
        "remark": "自动化测试创建"
    })
    
    device_id = None
    if result and result.get("code") == 200:
        device_id = result['data']['id']
        print_success(f"设备创建成功，ID: {device_id}, 编码: {device_code}")
        
        print_info("查询设备详情...")
        detail = api_request("POST", "/devices/detail", {"id": device_id})
        if detail and detail.get("code") == 200:
            print_success(f"设备详情查询成功: {detail['data']['deviceName']}")
        
        # 测试更新设备
        print_info("更新设备信息...")
        update_result = api_request("POST", "/devices/update", {
            "id": device_id,
            "remark": "更新后的备注"
        })
        if update_result and update_result.get("code") == 200:
            print_success("设备更新成功")
    else:
        print_error(f"设备创建失败: {result}")
    
    return device_id, device_code

def test_device_data():
    """6. 设备数据接口测试"""
    print_title("6. 设备数据接口")
    
    print_info("查询设备最新数据...")
    result = api_request("POST", "/device-data/latest", {
        "deviceCode": "TEM1111",
        "limit": 10
    })
    if result and result.get("code") == 200:
        print_success("最新数据查询成功")
    else:
        print_info("最新数据查询无结果（设备可能不存在）")
    
    print_info("查询设备历史数据...")
    end_time = datetime.now()
    start_time = end_time - timedelta(days=7)
    result = api_request("POST", "/device-data/history", {
        "deviceCode": "TEM1111",
        "startTime": start_time.strftime("%Y-%m-%d %H:%M:%S"),
        "endTime": end_time.strftime("%Y-%m-%d %H:%M:%S"),
        "page": 1,
        "pageSize": 10
    })
    if result and result.get("code") == 200:
        data_list = result['data'].get('list', [])
        print_success(f"历史数据查询成功，共 {len(data_list)} 条")
    else:
        print_info("历史数据查询无结果")

def test_command(product_id):
    """7. 命令控制接口测试"""
    print_title("7. 命令控制接口")
    
    if not product_id:
        print_info("跳过命令测试（无有效产品ID）")
        return
    
    print_info("查询产品命令列表...")
    result = api_request("POST", "/commands/product", {"productId": product_id})
    if result and result.get("code") == 200:
        commands = result['data'].get('list', [])
        print_success(f"命令列表查询成功，共 {len(commands)} 条")
    else:
        print_info("命令列表查询无结果")
    
    print_info("下发控制命令...")
    result = api_request("POST", "/commands/send", {
        "deviceCode": "TEM1111",
        "commands": [
            {
                "addr": "window",
                "addrv": "1"
            }
        ]
    })
    if result and result.get("code") == 200:
        print_success("命令下发成功")
    else:
        print_info("命令下发失败（设备可能不存在）")

def test_statistics():
    """8. 统计接口测试"""
    print_title("8. 统计接口")
    
    print_info("查询平台统计数据...")
    result = api_request("POST", "/statistics/overview")
    if result and result.get("code") == 200:
        data = result['data']
        print_success("统计数据查询成功")
        device_total = data.get('deviceTotal', 0)
        device_online = data.get('deviceOnline', 0)
        device_offline = data.get('deviceOffline', 0)
        today_data = data.get('todayDataCount', 0)
        print_info(f"设备总数: {device_total}, 在线: {device_online}, 离线: {device_offline}")
        print_info(f"今日数据量: {today_data}")
    else:
        print_error(f"统计数据查询失败: {result}")
    
    print_info("查询设备分布统计...")
    result = api_request("POST", "/statistics/device-distribution", {"type": "group"})
    if result and result.get("code") == 200:
        print_success("设备分布统计查询成功")
    
    print_info("查询数据趋势（最近24小时）...")
    result = api_request("POST", "/statistics/data-trend", {"range": "24h"})
    if result and result.get("code") == 200:
        print_success("24小时趋势数据查询成功")
    
    print_info("查询数据趋势（最近7天）...")
    result = api_request("POST", "/statistics/data-trend", {"range": "7d"})
    if result and result.get("code") == 200:
        print_success("7天趋势数据查询成功")

def test_role():
    """9. 角色管理接口测试"""
    print_title("9. 角色管理接口")
    
    print_info("查询角色列表...")
    result = api_request("POST", "/roles/list", {
        "page": 1,
        "pageSize": 10
    })
    if result and result.get("code") == 200:
        roles_list = result['data'].get('list', [])
        total = result['data'].get('total', len(roles_list))
        print_success(f"角色列表查询成功，共 {total} 条")
    else:
        print_error(f"角色列表查询失败: {result}")
    
    print_info("创建新角色...")
    result = api_request("POST", "/roles/create", {
        "name": f"测试角色_{datetime.now().strftime('%H%M%S')}",
        "code": f"TEST_ROLE_{random.randint(100, 999)}",
        "description": "自动化测试创建的角色",
        "permissions": [
            {
                "module": "device",
                "actions": ["view"]
            }
        ],
        "menuPermissions": ["dashboard", "device_list"]
    })
    if result and result.get("code") == 200:
        role_id = result['data']['id']
        print_success(f"角色创建成功，ID: {role_id}")
        
        print_info("查询角色详情...")
        detail = api_request("POST", "/roles/detail", {"id": role_id})
        if detail and detail.get("code") == 200:
            print_success(f"角色详情: {detail['data'].get('roleName', detail['data'].get('name', 'N/A'))}")
        
        # 测试更新角色
        print_info("更新角色信息...")
        update_result = api_request("POST", "/roles/update", {
            "id": role_id,
            "description": "更新后的描述"
        })
        if update_result and update_result.get("code") == 200:
            print_success("角色更新成功")
    else:
        print_error(f"角色创建失败: {result}")

# 主函数
def main():
    print(f"{Colors.YELLOW}")
    print("=" * 50)
    print("   IoT Platform API 自动化测试")
    print("=" * 50)
    print(f"{Colors.END}")
    
    # 1. 登录获取 Token
    if not test_auth():
        print_error("登录失败，测试终止")
        return
    
    # 2. 用户管理测试
    test_user()
    
    # 3. 产品管理测试
    product_id = test_product()
    
    # 4. 设备分组测试
    group_id = test_device_group()
    
    # 5. 设备管理测试
    if product_id and group_id:
        device_id, device_code = test_device(product_id, group_id)
    
    # 6. 设备数据测试
    test_device_data()
    
    # 7. 命令控制测试
    if product_id:
        test_command(product_id)
    
    # 8. 统计接口测试
    test_statistics()
    
    # 9. 角色管理测试
    test_role()
    
    # 测试总结
    print_title("测试完成")
    print_success("所有接口测试执行完毕！")
    print_info("Token 在整个测试过程中保持有效")
    print(f"\n{Colors.GREEN}提示: 脚本已自动管理 Token，无需手动更新{Colors.END}\n")

if __name__ == "__main__":
    main()
