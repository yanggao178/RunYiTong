#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android日志查看工具
用于查看Android应用的调试日志输出，特别是面诊相关的日志
"""

import subprocess
import sys
import time
import re
from datetime import datetime

def run_adb_command(command):
    """
    执行ADB命令
    """
    try:
        result = subprocess.run(command, shell=True, capture_output=True, text=True, encoding='utf-8')
        return result.stdout, result.stderr, result.returncode
    except Exception as e:
        return "", str(e), 1

def check_adb_connection():
    """
    检查ADB连接状态
    """
    print("检查ADB连接状态...")
    stdout, stderr, returncode = run_adb_command("adb devices")
    
    if returncode != 0:
        print(f"ADB命令执行失败: {stderr}")
        return False
    
    lines = stdout.strip().split('\n')
    if len(lines) < 2:
        print("没有检测到Android设备")
        return False
    
    devices = []
    for line in lines[1:]:
        if '\tdevice' in line:
            device_id = line.split('\t')[0]
            devices.append(device_id)
    
    if not devices:
        print("没有检测到已连接的Android设备")
        return False
    
    print(f"检测到 {len(devices)} 个设备: {', '.join(devices)}")
    return True

def clear_logcat():
    """
    清空logcat缓冲区
    """
    print("清空logcat缓冲区...")
    stdout, stderr, returncode = run_adb_command("adb logcat -c")
    if returncode == 0:
        print("logcat缓冲区已清空")
    else:
        print(f"清空logcat失败: {stderr}")

def monitor_face_diagnosis_logs():
    """
    监控面诊相关的日志
    """
    print("开始监控面诊相关日志...")
    print("请在Android应用中进行面诊操作")
    print("按 Ctrl+C 停止监控")
    print("=" * 80)
    
    # 过滤条件：包含面诊相关的标签
    filter_tags = [
        "PrescriptionFragment",
        "NetworkDebugHelper", 
        "ApiClient",
        "OkHttp",
        "Retrofit"
    ]
    
    # 构建logcat命令
    tag_filters = " ".join([f"-s {tag}:*" for tag in filter_tags])
    command = f"adb logcat {tag_filters}"
    
    try:
        process = subprocess.Popen(
            command,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding='utf-8',
            bufsize=1,
            universal_newlines=True
        )
        
        print(f"执行命令: {command}")
        print("=" * 80)
        
        while True:
            line = process.stdout.readline()
            if not line:
                break
            
            # 添加时间戳并高亮重要信息
            timestamp = datetime.now().strftime("%H:%M:%S")
            line = line.strip()
            
            if line:
                # 高亮错误和重要信息
                if "ERROR" in line or "Exception" in line:
                    print(f"[{timestamp}] 🔴 {line}")
                elif "面诊" in line or "FaceDiagnosis" in line:
                    print(f"[{timestamp}] 🎯 {line}")
                elif "onResponse" in line or "onFailure" in line:
                    print(f"[{timestamp}] 📡 {line}")
                elif "调试" in line or "Debug" in line:
                    print(f"[{timestamp}] 🔧 {line}")
                else:
                    print(f"[{timestamp}] ℹ️  {line}")
                    
    except KeyboardInterrupt:
        print("\n停止监控日志")
        process.terminate()
    except Exception as e:
        print(f"监控日志时发生错误: {e}")

def show_recent_logs():
    """
    显示最近的日志
    """
    print("获取最近的面诊相关日志...")
    
    # 过滤条件
    filter_tags = [
        "PrescriptionFragment",
        "NetworkDebugHelper", 
        "ApiClient",
        "OkHttp",
        "Retrofit"
    ]
    
    tag_filters = " ".join([f"-s {tag}:*" for tag in filter_tags])
    command = f"adb logcat -d {tag_filters} | tail -50"
    
    stdout, stderr, returncode = run_adb_command(command)
    
    if returncode != 0:
        print(f"获取日志失败: {stderr}")
        return
    
    if not stdout.strip():
        print("没有找到相关日志")
        return
    
    print("=" * 80)
    print("最近50条相关日志:")
    print("=" * 80)
    
    for line in stdout.strip().split('\n'):
        if line.strip():
            # 高亮重要信息
            if "ERROR" in line or "Exception" in line:
                print(f"🔴 {line}")
            elif "面诊" in line or "FaceDiagnosis" in line:
                print(f"🎯 {line}")
            elif "onResponse" in line or "onFailure" in line:
                print(f"📡 {line}")
            elif "调试" in line or "Debug" in line:
                print(f"🔧 {line}")
            else:
                print(f"ℹ️  {line}")

def main():
    """
    主函数
    """
    print("Android面诊日志查看工具")
    print("=" * 40)
    
    # 检查ADB连接
    if not check_adb_connection():
        print("\n请确保:")
        print("1. Android设备已连接并开启USB调试")
        print("2. ADB工具已安装并在PATH中")
        print("3. 设备已授权调试权限")
        return
    
    while True:
        print("\n请选择操作:")
        print("1. 查看最近的日志")
        print("2. 实时监控日志")
        print("3. 清空日志缓冲区")
        print("4. 退出")
        
        choice = input("请输入选择 (1-4): ").strip()
        
        if choice == "1":
            show_recent_logs()
        elif choice == "2":
            clear_logcat()
            monitor_face_diagnosis_logs()
        elif choice == "3":
            clear_logcat()
        elif choice == "4":
            print("退出程序")
            break
        else:
            print("无效选择，请重新输入")

if __name__ == "__main__":
    main()