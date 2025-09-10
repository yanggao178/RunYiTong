#!/usr/bin/env python
"""
清理重复CMS页面的脚本
"""

import os
import sys
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')

try:
    django.setup()
    
    from cms.models import Page
    
    print("=== 清理重复CMS页面 ===")
    
    # 查找所有标记为首页的页面
    home_pages = Page.objects.filter(is_home=True)
    print(f"找到 {home_pages.count()} 个标记为首页的页面")
    
    if home_pages.count() > 1:
        # 保留第一个，删除其他的
        home_pages_to_delete = list(home_pages)[1:]  # 跳过第一个
        for page in home_pages_to_delete:
            print(f"删除重复的首页: {page.get_title()} (id: {page.id})")
            page.delete()
        
        print(f"✓ 删除了 {len(home_pages_to_delete)} 个重复的首页")
    
    # 确保只有一个首页
    home_pages = Page.objects.filter(is_home=True)
    if home_pages.count() == 1:
        home_page = home_pages.first()
        print(f"✓ 确保首页唯一: {home_page.get_title()} (id: {home_page.id}, is_home: {home_page.is_home})")
    elif home_pages.count() == 0:
        print("❌ 没有找到首页")
    else:
        print(f"⚠️ 仍有 {home_pages.count()} 个首页")
    
    # 显示所有页面
    all_pages = Page.objects.all()
    print(f"\n当前所有页面 ({all_pages.count()} 个):")
    for page in all_pages:
        title = page.get_title() if hasattr(page, 'get_title') else 'Unknown'
        slug = page.get_slug() if hasattr(page, 'get_slug') else 'Unknown'
        page_id = page.id
        is_home = getattr(page, 'is_home', False)
        print(f"- {title} (slug: \"{slug}\", id: {page_id}, is_home: {is_home})")
    
    print("✓ CMS页面清理完成")
    
except Exception as e:
    print(f"❌ 清理过程中出现错误: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)