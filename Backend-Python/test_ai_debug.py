#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
from dotenv import load_dotenv

# 加载环境变量
load_dotenv()

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from ai.ai_prescription import generate_tcm_prescription

def test_ai_prescription():
    """测试AI处方生成功能"""
    print("="*60)
    print("🧪 开始测试AI处方生成功能")
    print("="*60)
    
    # 检查环境变量
    api_key = os.getenv("OPENAI_API_KEY")
    ai_model = os.getenv("AI_MODEL", "deepseek-chat")
    
    print(f"🔍 API Key存在: {bool(api_key)}")
    if api_key:
        print(f"🔍 API Key长度: {len(api_key)}")
        print(f"🔍 API Key前10位: {api_key[:10]}")
    print(f"🔍 AI Model: {ai_model}")
    print()
    
    # 测试症状
    test_symptoms = "头痛发热咳嗽，伴有咽痛，舌苔薄白，脉浮数"
    print(f"🎯 测试症状: {test_symptoms}")
    print()
    
    try:
        print("🚀 开始调用AI生成处方...")
        result = generate_tcm_prescription(
            symptoms=test_symptoms,
            api_key=api_key,
            patient_info=None,
            model=ai_model,
            max_tokens=1000
        )
        
        print("✅ AI调用成功！")
        print(f"📋 结果类型: {type(result)}")
        print(f"📋 结果键: {list(result.keys()) if isinstance(result, dict) else 'Not a dict'}")
        print()
        
        # 打印结果的详细信息
        if isinstance(result, dict):
            print("📊 详细结果:")
            for key, value in result.items():
                print(f"  {key}: {type(value)} - {str(value)[:100]}...")
        else:
            print(f"📊 结果内容: {str(result)[:500]}...")
            
    except Exception as e:
        print(f"❌ AI调用失败: {str(e)}")
        print(f"❌ 异常类型: {type(e).__name__}")
        import traceback
        print(f"❌ 详细错误:")
        traceback.print_exc()
    
    print("="*60)
    print("🏁 测试完成")
    print("="*60)

if __name__ == "__main__":
    test_ai_prescription()