#!/usr/bin/env python
"""
检查CMS页面状态的脚本
"""

import os
import sys
import django

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')

try:
    django.setup()
    
    from cms.models import Page
    
    print("=== Django CMS 页面检查 ===")
    pages = Page.objects.all()
    print(f"页面总数: {pages.count()}")
    
    for page in pages:
        title = page.get_title() if hasattr(page, 'get_title') else 'Unknown'
        slug = page.get_slug() if hasattr(page, 'get_slug') else 'Unknown'
        page_id = page.id
        is_home = getattr(page, 'is_home', False)
        print(f"- {title} (slug: \"{slug}\", id: {page_id}, is_home: {is_home})")
    
    print("✓ Django CMS 页面检查完成")
    
except Exception as e:
    print(f"❌ 检查过程中出现错误: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)