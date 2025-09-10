#!/usr/bin/env python
"""
修复首页设置的脚本
"""

import os
import sys
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')

try:
    django.setup()
    
    from cms.models import Page
    from cms.api import publish_page
    from django.contrib.auth.models import User
    
    print("=== 修复Django CMS首页设置 ===")
    
    # 查找首页（slug为空的页面）
    home_pages = Page.objects.filter(title_set__title='首页')
    if home_pages.exists():
        home_page = home_pages.first()
        print(f"找到首页: {home_page.get_title()} (id: {home_page.id})")
        
        # 设置为首页
        home_page.is_home = True
        home_page.save()
        print("✓ 已将该页面设置为首页")
        
        # 发布页面
        admin_user = User.objects.filter(is_superuser=True).first()
        if admin_user:
            try:
                publish_page(home_page, admin_user, 'zh-hans')
                print("✓ 首页已发布")
            except Exception as e:
                print(f"⚠️ 发布首页时出现警告: {e}")
        else:
            print("⚠️ 未找到管理员用户，跳过发布步骤")
    else:
        print("❌ 未找到首页")
        
    # 验证修复结果
    home_pages = Page.objects.filter(is_home=True)
    if home_pages.exists():
        home_page = home_pages.first()
        print(f"✓ 首页已正确设置: {home_page.get_title()} (slug: \"{home_page.get_slug()}\", id: {home_page.id})")
    else:
        print("❌ 首页仍未正确设置")
    
    print("✓ Django CMS首页修复完成")
    
except Exception as e:
    print(f"❌ 修复过程中出现错误: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)