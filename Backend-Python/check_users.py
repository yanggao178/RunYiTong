#!/usr/bin/env python
"""
检查Django CMS用户信息
"""

import os
import sys
import django
from pathlib import Path

# 添加当前目录到Python路径
current_dir = Path(__file__).resolve().parent
sys.path.insert(0, str(current_dir))

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')

try:
    django.setup()
    
    from django.contrib.auth.models import User
    
    print("=" * 50)
    print("Django CMS 用户信息检查")
    print("=" * 50)
    
    users = User.objects.all()
    
    if users.exists():
        print(f"找到 {users.count()} 个用户:")
        for user in users:
            print(f"  用户名: {user.username}")
            print(f"  邮箱: {user.email}")
            print(f"  是否超级用户: {user.is_superuser}")
            print(f"  是否激活: {user.is_active}")
            print(f"  最后登录: {user.last_login}")
            print(f"  创建时间: {user.date_joined}")
            print("  " + "-" * 40)
    else:
        print("没有找到任何用户！")
        
    # 检查admin用户
    try:
        admin_user = User.objects.get(username='admin')
        print(f"admin用户存在:")
        print(f"  密码是否可用: {admin_user.has_usable_password()}")
        print(f"  是否激活: {admin_user.is_active}")
        print(f"  是否超级用户: {admin_user.is_superuser}")
    except User.DoesNotExist:
        print("admin用户不存在！")
        
except Exception as e:
    print(f"检查过程中出现错误: {e}")
    import traceback
    traceback.print_exc()