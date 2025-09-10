#!/usr/bin/env python
"""
检查Django CMS状态的脚本
"""

import os
import sys
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')

try:
    django.setup()
    
    from django.contrib.auth.models import User
    from cms.models import Page
    from medical_cms.models import MedicalDepartment, Doctor, Product
    
    print("=== Django CMS 状态检查 ===")
    print(f"用户数量: {User.objects.count()}")
    print(f"CMS页面数量: {Page.objects.count()}")
    print(f"医疗科室数量: {MedicalDepartment.objects.count()}")
    print(f"医生数量: {Doctor.objects.count()}")
    print(f"商品数量: {Product.objects.count()}")
    
    # 检查管理员用户
    admin_users = User.objects.filter(is_superuser=True)
    print(f"管理员用户数量: {admin_users.count()}")
    
    if admin_users.exists():
        print("管理员账号:")
        for admin in admin_users:
            print(f"  - {admin.username} ({admin.email})")
    else:
        print("⚠️ 没有找到管理员用户！")
    
    # 检查CMS页面
    if Page.objects.count() == 0:
        print("⚠️ 没有创建CMS页面！")
    else:
        print("CMS页面:")
        for page in Page.objects.all()[:5]:  # 显示前5个页面
            print(f"  - {page}")
    
    print("✓ Django CMS 状态检查完成")
    
except Exception as e:
    print(f"❌ 检查过程中出现错误: {e}")
    sys.exit(1)