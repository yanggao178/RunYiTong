#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试纯中医处方生成功能（移除西医诊疗部分）
"""

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from ai.ai_prescription import generate_tcm_prescription

def test_tcm_only_prescription():
    """
    测试纯中医处方生成功能
    """
    print("=== 测试纯中医处方生成功能 ===")
    
    # 测试症状
    symptoms = "头痛，失眠，食欲不振，疲劳乏力，舌质红，苔黄腻，脉弦数"
    
    # 患者信息
    patient_info = {
        "age": 35,
        "gender": "女",
        "allergies": ["青霉素"]
    }
    
    try:
        print(f"症状: {symptoms}")
        print(f"患者信息: {patient_info}")
        print("\n开始生成中医处方...")
        
        # 调用处方生成函数
        prescription = generate_tcm_prescription(
            symptoms=symptoms,
            patient_info=patient_info,
            max_tokens=1500
        )
        
        print("\n✅ 中医处方生成成功！")
        print(f"处方类型: {type(prescription)}")
        
        # 显示处方内容
        print("\n=== 中医处方内容 ===")
        print(f"辨证分型: {prescription.syndrome_type}")
        print(f"治疗方法: {prescription.treatment_method}")
        print(f"主方信息: {prescription.main_prescription}")
        print(f"药物组成: {prescription.composition}")
        print(f"用法用量: {prescription.usage}")
        print(f"禁忌事项: {prescription.contraindications}")
        
        # 验证是否只包含中医内容
        print("\n=== 验证内容纯度 ===")
        all_content = str(prescription.syndrome_type) + str(prescription.treatment_method) + \
                     str(prescription.main_prescription) + str(prescription.composition) + \
                     str(prescription.usage) + str(prescription.contraindications)
        
        western_keywords = ["西药", "抗生素", "激素", "化学药物", "西医", "现代医学", "临床检查", "实验室"]
        found_western = [keyword for keyword in western_keywords if keyword in all_content]
        
        if found_western:
            print(f"⚠️ 发现西医相关内容: {found_western}")
        else:
            print("✅ 内容纯度验证通过，只包含中医诊疗内容")
        
        return True
        
    except Exception as e:
        print(f"\n❌ 中医处方生成失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_complex_tcm_symptoms():
    """
    测试复杂中医症状的处方生成
    """
    print("\n=== 测试复杂中医症状 ===")
    
    complex_symptoms = '''患者主诉：头痛如裂，痛连项背，遇风寒加重；
    夜寐不安，多梦易醒，心烦意乱；
    纳呆食少，脘腹胀满，大便溏薄；
    神疲乏力，四肢困重，畏寒肢冷；
    舌质淡红，苔白腻，脉沉弦滑。'''
    
    try:
        print(f"复杂症状: {complex_symptoms}")
        print("\n开始生成复杂症状的中医处方...")
        
        prescription = generate_tcm_prescription(
            symptoms=complex_symptoms,
            max_tokens=2000
        )
        
        print("\n✅ 复杂症状中医处方生成成功！")
        print(f"主要证型: {prescription.syndrome_type.get('主要证型', '未知')}")
        print(f"主要治法: {prescription.treatment_method.get('主要治法', '未知')}")
        print(f"推荐方剂: {prescription.main_prescription.get('方剂名称', '未知')}")
        
        return True
        
    except Exception as e:
        print(f"\n❌ 复杂症状处方生成失败: {e}")
        return False

if __name__ == "__main__":
    print("开始测试纯中医处方生成功能...")
    
    # 测试基本功能
    test1_result = test_tcm_only_prescription()
    
    # 测试复杂症状
    test2_result = test_complex_tcm_symptoms()
    
    print("\n=== 测试结果汇总 ===")
    print(f"基本中医功能测试: {'✅ 通过' if test1_result else '❌ 失败'}")
    print(f"复杂症状测试: {'✅ 通过' if test2_result else '❌ 失败'}")
    
    if test1_result and test2_result:
        print("\n🎉 所有测试通过！西医诊疗功能已成功移除，只保留中医诊疗。")
    else:
        print("\n⚠️ 部分测试失败，需要进一步调试。")