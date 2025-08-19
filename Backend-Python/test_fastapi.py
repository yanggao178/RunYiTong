#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试FastAPI商品API
"""

import requests
import json

def test_fastapi_products():
    """测试FastAPI商品API"""
    base_url = "http://localhost:8000"
    
    print("测试FastAPI商品API...")
    
    # 测试根路径
    try:
        response = requests.get(f"{base_url}/")
        print(f"根路径状态码: {response.status_code}")
        if response.status_code == 200:
            print(f"根路径响应: {response.json()}")
    except Exception as e:
        print(f"根路径测试失败: {e}")
    
    # 测试健康检查
    try:
        response = requests.get(f"{base_url}/health")
        print(f"健康检查状态码: {response.status_code}")
        if response.status_code == 200:
            print(f"健康检查响应: {response.json()}")
    except Exception as e:
        print(f"健康检查失败: {e}")
    
    # 测试商品API
    try:
        response = requests.get(f"{base_url}/api/v1/products/?skip=0&limit=5")
        print(f"商品API状态码: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"商品API响应: {json.dumps(data, indent=2, ensure_ascii=False)}")
        else:
            print(f"商品API错误响应: {response.text}")
    except Exception as e:
        print(f"商品API测试失败: {e}")

if __name__ == "__main__":
    test_fastapi_products()