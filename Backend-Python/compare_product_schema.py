#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
比较Django CMS Product模型与ai_medical.db products表的字段结构
"""

import sqlite3
import os

def get_products_table_schema():
    """获取ai_medical.db中products表的结构"""
    db_path = 'ai_medical.db'
    
    if not os.path.exists(db_path):
        print(f"错误: 找不到数据库文件 {db_path}")
        return None
    
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 获取表结构
        cursor.execute("PRAGMA table_info(products)")
        columns = cursor.fetchall()
        
        print("=== ai_medical.db products表结构 ===")
        print("字段名\t\t类型\t\t是否非空\t默认值")
        print("-" * 60)
        
        for col in columns:
            cid, name, type_name, notnull, default_value, pk = col
            print(f"{name:<15}\t{type_name:<15}\t{bool(notnull):<10}\t{default_value or 'NULL'}")
        
        # 获取示例数据
        cursor.execute("SELECT * FROM products LIMIT 1")
        sample = cursor.fetchone()
        
        if sample:
            print("\n=== 示例数据 ===")
            column_names = [desc[0] for desc in cursor.description]
            for i, value in enumerate(sample):
                print(f"{column_names[i]}: {value}")
        
        conn.close()
        return columns
        
    except Exception as e:
        print(f"读取数据库错误: {e}")
        return None

def analyze_django_product_model():
    """分析Django Product模型的字段"""
    print("\n=== Django CMS Product模型字段分析 ===")
    print("基于models.py中的Product模型定义:")
    print("-" * 60)
    
    django_fields = {
        'id': 'AutoField (主键)',
        'name': 'CharField(max_length=200) - 商品名称',
        'slug': 'SlugField(unique=True, blank=True) - URL别名',
        'description': 'TextField - 商品描述',
        'short_description': 'TextField(max_length=500, blank=True) - 简短描述',
        'category': 'ForeignKey(ProductCategory) - 商品分类',
        'department': 'ForeignKey(MedicalDepartment, blank=True, null=True) - 相关科室',
        'price': 'DecimalField(max_digits=10, decimal_places=2) - 价格',
        'original_price': 'DecimalField(max_digits=10, decimal_places=2, blank=True, null=True) - 原价',
        'stock_quantity': 'PositiveIntegerField(default=0) - 库存数量',
        'min_stock_level': 'PositiveIntegerField(default=5) - 最低库存',
        'sku': 'CharField(max_length=100, unique=True, blank=True) - 商品编码',
        'barcode': 'CharField(max_length=100, blank=True) - 条形码',
        'weight': 'DecimalField(max_digits=8, decimal_places=2, blank=True, null=True) - 重量',
        'dimensions': 'CharField(max_length=100, blank=True) - 尺寸',
        'featured_image': 'FilerImageField - 主图片',
        'gallery_images': 'ManyToManyField - 商品图库',
        'tags': 'CharField(max_length=200, blank=True) - 标签',
        'status': 'CharField(max_length=20, choices=STATUS_CHOICES, default="draft") - 状态',
        'is_featured': 'BooleanField(default=False) - 是否推荐',
        'is_prescription_required': 'BooleanField(default=False) - 是否需要处方',
        'manufacturer': 'CharField(max_length=200, blank=True) - 生产厂家',
        'expiry_date': 'DateField(blank=True, null=True) - 有效期',
        'usage_instructions': 'TextField(blank=True) - 使用说明',
        'side_effects': 'TextField(blank=True) - 副作用',
        'contraindications': 'TextField(blank=True) - 禁忌症',
        'views_count': 'PositiveIntegerField(default=0) - 浏览次数',
        'sales_count': 'PositiveIntegerField(default=0) - 销售数量',
        'created_at': 'DateTimeField(auto_now_add=True) - 创建时间',
        'updated_at': 'DateTimeField(auto_now=True) - 更新时间'
    }
    
    for field, description in django_fields.items():
        print(f"{field:<25}: {description}")

def compare_schemas():
    """比较两个模型的字段映射"""
    print("\n=== 字段映射对比分析 ===")
    print("-" * 60)
    
    # 当前信号处理函数中的字段映射
    current_mapping = {
        'name': 'instance.name',
        'price': 'float(instance.price)',
        'description': 'instance.description',
        'image_url': 'image_url (从featured_image构建)',
        'category': 'ai_category (通过category_mapping转换)',
        'stock': 'instance.stock_quantity',
        'created_time': 'now (当前时间)',
        'updated_time': 'now (当前时间)',
        'specification': 'instance.usage_instructions or ""',
        'manufacturer': 'instance.manufacturer or ""',
        'purchase_count': 'instance.sales_count'
    }
    
    print("当前同步到ai_medical.db的字段映射:")
    for ai_field, django_source in current_mapping.items():
        print(f"{ai_field:<20} <- {django_source}")
    
    print("\n=== 建议的改进 ===")
    print("1. 确保所有必要字段都被同步")
    print("2. 检查数据类型转换的准确性")
    print("3. 处理外键关系的映射")
    print("4. 考虑添加更多字段的同步支持")

if __name__ == '__main__':
    print("Django CMS Product模型与ai_medical.db products表结构对比")
    print("=" * 80)
    
    # 获取数据库表结构
    get_products_table_schema()
    
    # 分析Django模型
    analyze_django_product_model()
    
    # 对比分析
    compare_schemas()