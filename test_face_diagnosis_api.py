#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试面诊API连接的工具脚本
模拟Android客户端的API调用来验证后端服务器连接
"""

import requests
import json
import os
from pathlib import Path

def test_health_endpoint():
    """测试健康检查端点"""
    print("\n=== 测试健康检查端点 ===")
    try:
        response = requests.get("http://192.168.0.6:8000/health", timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"健康检查失败: {e}")
        return False

def test_face_diagnosis_endpoint():
    """测试面诊API端点"""
    print("\n=== 测试面诊API端点 ===")
    
    # 创建一个测试图片文件（1x1像素的PNG）
    test_image_data = b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\tpHYs\x00\x00\x0b\x13\x00\x00\x0b\x13\x01\x00\x9a\x9c\x18\x00\x00\x00\x0cIDATx\x9cc```\x00\x00\x00\x04\x00\x01\xdd\x8d\xb4\x1c\x00\x00\x00\x00IEND\xaeB`\x82'
    
    try:
        # 准备multipart/form-data请求
        files = {
            'image': ('test_face.png', test_image_data, 'image/png')
        }
        
        print("发送POST请求到面诊API...")
        response = requests.post(
            "http://192.168.0.6:8000/api/v1/prescriptions/analyze-face",
            files=files,
            timeout=30
        )
        
        print(f"状态码: {response.status_code}")
        print(f"响应头: {dict(response.headers)}")
        print(f"响应内容: {response.text[:500]}...")  # 只显示前500字符
        
        if response.status_code == 200:
            try:
                json_response = response.json()
                print(f"JSON解析成功: {json.dumps(json_response, indent=2, ensure_ascii=False)[:300]}...")
                return True
            except json.JSONDecodeError as e:
                print(f"JSON解析失败: {e}")
                return False
        else:
            print(f"API调用失败，状态码: {response.status_code}")
            return False
            
    except requests.exceptions.ConnectException as e:
        print(f"连接失败: {e}")
        return False
    except requests.exceptions.Timeout as e:
        print(f"请求超时: {e}")
        return False
    except Exception as e:
        print(f"请求异常: {e}")
        return False

def test_localhost_fallback():
    """测试localhost连接作为备用方案"""
    print("\n=== 测试localhost连接 ===")
    try:
        response = requests.get("http://localhost:8000/health", timeout=5)
        print(f"localhost状态码: {response.status_code}")
        print(f"localhost响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"localhost连接失败: {e}")
        return False

def main():
    """主测试函数"""
    print("开始测试Android客户端与后端服务器的连接...")
    
    # 测试健康检查
    health_ok = test_health_endpoint()
    
    # 测试localhost作为备用
    localhost_ok = test_localhost_fallback()
    
    # 测试面诊API
    face_api_ok = test_face_diagnosis_endpoint()
    
    print("\n=== 测试结果汇总 ===")
    print(f"健康检查 (192.168.0.6:8000): {'✓ 成功' if health_ok else '✗ 失败'}")
    print(f"localhost连接 (localhost:8000): {'✓ 成功' if localhost_ok else '✗ 失败'}")
    print(f"面诊API (192.168.0.6:8000): {'✓ 成功' if face_api_ok else '✗ 失败'}")
    
    if health_ok and face_api_ok:
        print("\n✓ 所有测试通过！Android客户端应该能够正常连接后端服务器。")
    elif localhost_ok:
        print("\n⚠ 192.168.0.6连接有问题，但localhost正常。可能需要检查网络配置。")
    else:
        print("\n✗ 连接测试失败。请检查后端服务器是否正在运行。")

if __name__ == "__main__":
    main()