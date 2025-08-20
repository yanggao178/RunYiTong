#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试AI处方生成函数的JSON解析修复
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from ai.ai_prescription import generate_tcm_prescription

def test_prescription_generation():
    """
    测试处方生成功能
    """
    print("=== 测试AI处方生成功能 ===")
    
    # 测试症状
    symptoms = "头痛，失眠，食欲不振，疲劳乏力"
    
    # 患者信息
    patient_info = {
        "age": 35,
        "gender": "女",
        "allergies": ["青霉素"]
    }
    
    try:
        print(f"症状: {symptoms}")
        print(f"患者信息: {patient_info}")
        print("\n开始生成处方...")
        
        # 调用处方生成函数
        prescription = generate_tcm_prescription(
            symptoms=symptoms,
            patient_info=patient_info,
            max_tokens=1500
        )
        
        print("\n✅ 处方生成成功！")
        print(f"处方类型: {type(prescription)}")
        
        # 显示处方内容
        print("\n=== 处方内容 ===")
        print(f"辨证分型: {prescription.syndrome_type}")
        print(f"治疗方法: {prescription.treatment_method}")
        print(f"主方信息: {prescription.main_prescription}")
        print(f"药物组成: {prescription.composition}")
        print(f"用法用量: {prescription.usage}")
        print(f"禁忌事项: {prescription.contraindications}")
        
        return True
        
    except Exception as e:
        print(f"\n❌ 处方生成失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_json_error_handling():
    """
    测试JSON错误处理
    """
    print("\n=== 测试JSON错误处理 ===")
    
    # 使用可能导致JSON解析错误的复杂症状描述
    complex_symptoms = '''患者主诉："头痛如裂"，伴有"心烦意乱"，
    夜间"辗转反侧"难以入睡，食欲"一落千丈"，
    全身"疲惫不堪"，舌质红，苔黄腻，脉弦数。'''
    
    try:
        print(f"复杂症状: {complex_symptoms}")
        print("\n开始生成处方（测试JSON错误处理）...")
        
        prescription = generate_tcm_prescription(
            symptoms=complex_symptoms,
            max_tokens=2000
        )
        
        print("\n✅ 复杂症状处方生成成功！")
        print(f"辨证分型: {prescription.syndrome_type}")
        
        return True
        
    except Exception as e:
        print(f"\n❌ 复杂症状处方生成失败: {e}")
        return False

if __name__ == "__main__":
    print("开始测试AI处方生成功能的JSON解析修复...")
    
    # 测试基本功能
    test1_result = test_prescription_generation()
    
    # 测试JSON错误处理
    test2_result = test_json_error_handling()
    
    print("\n=== 测试结果汇总 ===")
    print(f"基本功能测试: {'✅ 通过' if test1_result else '❌ 失败'}")
    print(f"JSON错误处理测试: {'✅ 通过' if test2_result else '❌ 失败'}")
    
    if test1_result and test2_result:
        print("\n🎉 所有测试通过！JSON解析错误已修复。")
    else:
        print("\n⚠️ 部分测试失败，需要进一步调试。")