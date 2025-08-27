#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
详细的面诊API测试脚本
模拟Android客户端的完整请求流程，包括multipart/form-data图片上传
"""

import requests
import json
import io
from PIL import Image
import base64
import time

def create_test_image():
    """
    创建一个测试用的PNG图片
    """
    # 创建一个简单的测试图片 (100x100像素，白色背景)
    img = Image.new('RGB', (100, 100), color='white')
    
    # 将图片保存到内存中的字节流
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='PNG')
    img_bytes.seek(0)
    
    return img_bytes

def test_health_endpoint(base_url):
    """
    测试健康检查端点
    """
    print(f"\n=== 测试健康检查端点: {base_url}/health ===")
    try:
        response = requests.get(f"{base_url}/health", timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应头: {dict(response.headers)}")
        print(f"响应内容: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"健康检查失败: {e}")
        return False

def test_face_diagnosis_api(base_url):
    """
    测试面诊API端点，模拟Android客户端的multipart/form-data请求
    """
    print(f"\n=== 测试面诊API端点: {base_url}/api/v1/prescriptions/analyze-face ===")
    
    # 创建测试图片
    test_image = create_test_image()
    
    # 准备multipart/form-data请求
    files = {
        'image': ('test_face.png', test_image, 'image/png')
    }
    
    try:
        print("发送POST请求...")
        start_time = time.time()
        
        response = requests.post(
            f"{base_url}/api/v1/prescriptions/analyze-face",
            files=files,
            timeout=30
        )
        
        end_time = time.time()
        request_duration = end_time - start_time
        
        print(f"请求耗时: {request_duration:.2f}秒")
        print(f"状态码: {response.status_code}")
        print(f"响应头: {dict(response.headers)}")
        
        # 打印响应内容
        try:
            response_json = response.json()
            print(f"响应JSON: {json.dumps(response_json, indent=2, ensure_ascii=False)}")
            
            # 检查响应结构
            if 'success' in response_json:
                print(f"API成功标志: {response_json['success']}")
            if 'message' in response_json:
                print(f"API消息: {response_json['message']}")
            if 'data' in response_json:
                print(f"API数据存在: {response_json['data'] is not None}")
                if response_json['data']:
                    print(f"数据类型: {type(response_json['data'])}")
            
        except json.JSONDecodeError:
            print(f"响应内容（非JSON）: {response.text[:500]}...")
        
        return response.status_code in [200, 422]  # 422是预期的错误（图片太小）
        
    except requests.exceptions.Timeout:
        print("请求超时")
        return False
    except requests.exceptions.ConnectionError as e:
        print(f"连接错误: {e}")
        return False
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def test_with_larger_image(base_url):
    """
    使用更大的图片测试面诊API
    """
    print(f"\n=== 使用更大图片测试面诊API ===")
    
    # 创建一个更大的测试图片 (200x200像素)
    img = Image.new('RGB', (200, 200), color='lightblue')
    
    # 添加一些简单的"面部"特征
    from PIL import ImageDraw
    draw = ImageDraw.Draw(img)
    # 画两个"眼睛"
    draw.ellipse([50, 60, 70, 80], fill='black')
    draw.ellipse([130, 60, 150, 80], fill='black')
    # 画一个"嘴巴"
    draw.arc([80, 120, 120, 140], 0, 180, fill='red', width=3)
    
    img_bytes = io.BytesIO()
    img.save(img_bytes, format='PNG')
    img_bytes.seek(0)
    
    files = {
        'image': ('larger_face.png', img_bytes, 'image/png')
    }
    
    try:
        print("发送POST请求（更大图片）...")
        start_time = time.time()
        
        response = requests.post(
            f"{base_url}/api/v1/prescriptions/analyze-face",
            files=files,
            timeout=60  # 更长的超时时间
        )
        
        end_time = time.time()
        request_duration = end_time - start_time
        
        print(f"请求耗时: {request_duration:.2f}秒")
        print(f"状态码: {response.status_code}")
        
        try:
            response_json = response.json()
            print(f"响应摘要: success={response_json.get('success')}, message={response_json.get('message')}")
            
            if response_json.get('success') and response_json.get('data'):
                print("✅ API返回了成功的面诊分析数据")
                data = response_json['data']
                if 'facial_analysis' in data:
                    print("  - 包含面部分析数据")
                if 'tcm_diagnosis' in data:
                    print("  - 包含中医诊断数据")
                if 'recommendations' in data:
                    print("  - 包含调理建议数据")
            else:
                print(f"⚠️ API返回错误或无数据: {response_json.get('message')}")
                
        except json.JSONDecodeError:
            print(f"响应内容（非JSON）: {response.text[:200]}...")
        
        return response.status_code == 200
        
    except Exception as e:
        print(f"请求失败: {e}")
        return False

def main():
    """
    主测试函数
    """
    print("🔍 详细面诊API测试开始")
    print("=" * 50)
    
    # 测试目标
    test_urls = [
        "http://192.168.0.6:8000",
        "http://localhost:8000"
    ]
    
    for base_url in test_urls:
        print(f"\n🎯 测试目标: {base_url}")
        print("-" * 30)
        
        # 1. 健康检查
        health_ok = test_health_endpoint(base_url)
        if not health_ok:
            print(f"❌ {base_url} 健康检查失败，跳过后续测试")
            continue
        
        print(f"✅ {base_url} 健康检查通过")
        
        # 2. 小图片测试（预期会有错误）
        small_image_test = test_face_diagnosis_api(base_url)
        
        # 3. 大图片测试
        large_image_test = test_with_larger_image(base_url)
        
        # 总结
        print(f"\n📊 {base_url} 测试总结:")
        print(f"  健康检查: {'✅' if health_ok else '❌'}")
        print(f"  小图片测试: {'✅' if small_image_test else '❌'}")
        print(f"  大图片测试: {'✅' if large_image_test else '❌'}")
        
        if health_ok and (small_image_test or large_image_test):
            print(f"🎉 {base_url} 面诊API基本可用")
        else:
            print(f"⚠️ {base_url} 面诊API存在问题")
    
    print("\n" + "=" * 50)
    print("🏁 详细面诊API测试完成")
    print("\n💡 如果测试通过但Android应用仍有问题，可能的原因:")
    print("   1. Android网络权限配置")
    print("   2. Android HTTP客户端配置")
    print("   3. 回调函数执行问题")
    print("   4. UI线程更新问题")
    print("   5. 异常处理逻辑问题")

if __name__ == "__main__":
    main()