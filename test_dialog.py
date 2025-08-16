#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
图片处理对话框测试脚本
用于触发对话框显示并监控相关日志
"""

import subprocess
import time
import threading
import sys
import os

def run_adb_command(command):
    """执行adb命令"""
    try:
        result = subprocess.run(command, shell=True, capture_output=True, text=True, encoding='utf-8')
        return result.stdout, result.stderr, result.returncode
    except Exception as e:
        print(f"执行命令失败: {e}")
        return "", str(e), -1

def clear_logcat():
    """清空logcat缓冲区"""
    print("清空logcat缓冲区...")
    stdout, stderr, code = run_adb_command("adb logcat -c")
    if code != 0:
        print(f"清空logcat失败: {stderr}")
        return False
    print("logcat缓冲区已清空")
    return True

def start_logcat_monitoring():
    """开始监控logcat日志"""
    print("开始监控logcat日志...")
    print("监控以下标签: PrescriptionFragment, ImageProcessingDialog, DialogDebugHelper")
    print("=" * 80)
    
    # 使用PowerShell兼容的命令
    cmd = 'adb logcat | Select-String "PrescriptionFragment|ImageProcessingDialog|DialogDebugHelper"'
    
    try:
        process = subprocess.Popen(
            ["powershell", "-Command", cmd],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding='utf-8',
            bufsize=1,
            universal_newlines=True
        )
        
        for line in iter(process.stdout.readline, ''):
            if line.strip():
                print(f"[LOG] {line.strip()}")
                
    except KeyboardInterrupt:
        print("\n停止日志监控")
        process.terminate()
    except Exception as e:
        print(f"监控日志时发生错误: {e}")

def launch_app():
    """启动应用"""
    print("启动应用...")
    package_name = "com.wenteng.frontend_android"
    activity_name = "com.wenteng.frontend_android.MainActivity"
    
    stdout, stderr, code = run_adb_command(f"adb shell am start -n {package_name}/{activity_name}")
    if code != 0:
        print(f"启动应用失败: {stderr}")
        return False
    
    print("应用启动成功")
    return True

def simulate_image_selection():
    """模拟图片选择操作"""
    print("\n请手动操作:")
    print("1. 在应用中导航到处方页面")
    print("2. 点击选择图片按钮")
    print("3. 选择一张图片")
    print("4. 观察图片处理选项对话框是否正常显示")
    print("\n按Enter键继续监控日志，或按Ctrl+C退出...")
    
    try:
        input()
    except KeyboardInterrupt:
        return False
    
    return True

def main():
    """主函数"""
    print("图片处理对话框测试脚本")
    print("=" * 50)
    
    # 检查adb是否可用
    stdout, stderr, code = run_adb_command("adb devices")
    if code != 0:
        print(f"adb不可用: {stderr}")
        print("请确保:")
        print("1. Android SDK已安装")
        print("2. adb在系统PATH中")
        print("3. 设备已连接并启用USB调试")
        return
    
    print("检测到的设备:")
    print(stdout)
    
    # 清空日志
    if not clear_logcat():
        return
    
    # 启动应用
    if not launch_app():
        return
    
    # 等待应用启动
    print("等待应用完全启动...")
    time.sleep(3)
    
    # 提示用户操作
    if not simulate_image_selection():
        return
    
    # 开始监控日志
    try:
        start_logcat_monitoring()
    except KeyboardInterrupt:
        print("\n测试结束")

if __name__ == "__main__":
    main()