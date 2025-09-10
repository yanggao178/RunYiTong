#!/usr/bin/env python
"""
Django CMS 数据库初始化脚本
用于创建初始数据和配置Django CMS
"""

import os
import sys
import django
from django.core.management import execute_from_command_line
import sqlite3
from decimal import Decimal

# 设置Django环境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'cms_project.settings')
django.setup()

from cms.api import create_page, publish_page

from django.contrib.auth.models import User
from django.contrib.sites.models import Site
from cms.models import Page
from cms.api import create_page, add_plugin
from medical_cms.models import MedicalDepartment, Doctor, MedicalNews, MedicalService, ProductCategory, Product
from django.utils import timezone


def create_superuser():
    """创建超级用户"""
    print("创建超级用户...")
    if not User.objects.filter(username='admin').exists():
        User.objects.create_superuser(
            username='admin',
            email='admin@medical.com',
            password='admin123',
            first_name='系统',
            last_name='管理员'
        )
        print("✓ 超级用户创建成功: admin/admin123")
    else:
        print("✓ 超级用户已存在")


def setup_site():
    """配置站点信息"""
    print("配置站点信息...")
    site = Site.objects.get(pk=1)
    site.domain = 'localhost:8080'
    site.name = 'AI医疗管理系统'
    site.save()
    print("✓ 站点信息配置完成")


def create_medical_departments():
    """创建医疗科室"""
    print("创建医疗科室...")
    departments = [
        {'name': '内科', 'description': '内科疾病诊疗'},
        {'name': '外科', 'description': '外科手术治疗'},
        {'name': '儿科', 'description': '儿童疾病专科'},
        {'name': '妇产科', 'description': '妇科和产科疾病'},
        {'name': '骨科', 'description': '骨骼和关节疾病'},
        {'name': '心血管科', 'description': '心脏血管疾病'},
        {'name': '神经科', 'description': '神经系统疾病'},
        {'name': '眼科', 'description': '眼部疾病治疗'},
    ]
    
    for dept_data in departments:
        dept, created = MedicalDepartment.objects.get_or_create(
            name=dept_data['name'],
            defaults={'description': dept_data['description']}
        )
        if created:
            print(f"  ✓ 创建科室: {dept.name}")
    
    print("✓ 医疗科室创建完成")


def create_sample_doctors():
    """创建示例医生"""
    print("创建示例医生...")
    
    # 创建医生用户
    doctors_data = [
        {
            'username': 'dr_zhang',
            'first_name': '张',
            'last_name': '医生',
            'email': 'dr.zhang@medical.com',
            'title': 'chief',
            'department': '内科',
            'specialization': '心血管内科，高血压，糖尿病'
        },
        {
            'username': 'dr_li',
            'first_name': '李',
            'last_name': '医生',
            'email': 'dr.li@medical.com',
            'title': 'associate',
            'department': '外科',
            'specialization': '普通外科，腹腔镜手术'
        },
        {
            'username': 'dr_wang',
            'first_name': '王',
            'last_name': '医生',
            'email': 'dr.wang@medical.com',
            'title': 'attending',
            'department': '儿科',
            'specialization': '儿童呼吸系统疾病'
        },
    ]
    
    for doctor_data in doctors_data:
        # 创建用户
        user, created = User.objects.get_or_create(
            username=doctor_data['username'],
            defaults={
                'first_name': doctor_data['first_name'],
                'last_name': doctor_data['last_name'],
                'email': doctor_data['email'],
                'password': 'pbkdf2_sha256$260000$dummy$dummy'  # 临时密码
            }
        )
        
        if created:
            user.set_password('doctor123')
            user.save()
        
        # 创建医生记录
        department = MedicalDepartment.objects.get(name=doctor_data['department'])
        doctor, created = Doctor.objects.get_or_create(
            user=user,
            defaults={
                'title': doctor_data['title'],
                'department': department,
                'specialization': doctor_data['specialization'],
                'bio': f"{user.get_full_name()}，{department.name}专家，擅长{doctor_data['specialization']}。"
            }
        )
        
        if created:
            print(f"  ✓ 创建医生: {user.get_full_name()}")
    
    print("✓ 示例医生创建完成")


def create_sample_news():
    """创建示例新闻"""
    print("创建示例新闻...")
    
    admin_user = User.objects.get(username='admin')
    
    news_data = [
        {
            'title': '我院引进先进医疗设备',
            'slug': 'new-medical-equipment',
            'content': '为了更好地为患者服务，我院最近引进了一批先进的医疗设备，包括高端CT、MRI等影像设备，大大提升了诊断准确性。',
            'excerpt': '我院引进先进医疗设备，提升诊断准确性',
            'category': '医院动态',
            'tags': '医疗设备,CT,MRI'
        },
        {
            'title': '春季健康体检活动开始',
            'slug': 'spring-health-checkup',
            'content': '春季是体检的好时节，我院推出春季健康体检套餐，包含全面的身体检查项目，为您的健康保驾护航。',
            'excerpt': '春季健康体检活动，全面身体检查',
            'category': '健康活动',
            'tags': '体检,健康,春季'
        },
        {
            'title': '专家义诊活动通知',
            'slug': 'expert-free-clinic',
            'content': '本月底将举行专家义诊活动，多位知名专家将为市民提供免费咨询和初步诊断服务。',
            'excerpt': '专家义诊活动，免费咨询诊断',
            'category': '公益活动',
            'tags': '义诊,专家,免费'
        },
    ]
    
    for news_item in news_data:
        news, created = MedicalNews.objects.get_or_create(
            slug=news_item['slug'],
            defaults={
                'title': news_item['title'],
                'content': news_item['content'],
                'excerpt': news_item['excerpt'],
                'author': admin_user,
                'category': news_item['category'],
                'tags': news_item['tags'],
                'is_published': True,
                'published_at': timezone.now()
            }
        )
        
        if created:
            print(f"  ✓ 创建新闻: {news.title}")
    
    print("✓ 示例新闻创建完成")


def create_cms_pages():
    """创建CMS页面"""
    print("创建CMS页面...")
    
    from django.contrib.auth.models import User
    from cms.models import Page
    
    # 获取或创建首页
    home_pages = Page.objects.filter(is_home=True)
    if home_pages.exists():
        home_page = home_pages.first()
        print(f"  ✓ 首页已存在: {home_page.get_title()} (id: {home_page.id})")
    else:
        # 创建首页
        home_page = create_page(
            title='首页',
            template='base.html',
            language='zh-hans',
            slug='',  # 首页使用空slug
            in_navigation=True
        )
        # 设置为首页
        home_page.is_home = True
        home_page.save()
        print("  ✓ 创建首页")
    
    # 创建医疗服务页面
    existing_medical = Page.objects.filter(title_set__title='医疗服务')
    if existing_medical.exists():
        medical_page = existing_medical.first()
        print("  ✓ 医疗服务页面已存在，跳过创建")
    else:
        medical_page = create_page(
            title='医疗服务',
            template='medical_page.html',
            language='zh-hans',
            parent=home_page,
            slug='medical-services',
            in_navigation=True
        )
        # 发布页面
        admin_user = User.objects.filter(is_superuser=True).first()
        if admin_user:
            try:
                publish_page(medical_page, admin_user, 'zh-hans')
            except:
                pass  # 忽略发布错误
        print("  ✓ 创建医疗服务页面")
    
    # 创建专家团队页面
    existing_experts = Page.objects.filter(title_set__title='专家团队')
    if existing_experts.exists():
        experts_page = existing_experts.first()
        print("  ✓ 专家团队页面已存在，跳过创建")
    else:
        experts_page = create_page(
            title='专家团队',
            template='base.html',
            language='zh-hans',
            parent=home_page,
            slug='experts',
            in_navigation=True
        )
        # 发布页面
        admin_user = User.objects.filter(is_superuser=True).first()
        if admin_user:
            try:
                publish_page(experts_page, admin_user, 'zh-hans')
            except:
                pass  # 忽略发布错误
        print("  ✓ 创建专家团队页面")
    
    # 创建新闻中心页面
    existing_news = Page.objects.filter(title_set__title='新闻中心')
    if existing_news.exists():
        news_page = existing_news.first()
        print("  ✓ 新闻中心页面已存在，跳过创建")
    else:
        news_page = create_page(
            title='新闻中心',
            template='news_page.html',
            language='zh-hans',
            parent=home_page,
            slug='news',
            in_navigation=True
        )
        # 发布页面
        admin_user = User.objects.filter(is_superuser=True).first()
        if admin_user:
            try:
                publish_page(news_page, admin_user, 'zh-hans')
            except:
                pass  # 忽略发布错误
        print("  ✓ 创建新闻中心页面")
    
    # 创建联系我们页面
    existing_contact = Page.objects.filter(title_set__title='联系我们')
    if existing_contact.exists():
        contact_page = existing_contact.first()
        print("  ✓ 联系我们页面已存在，跳过创建")
    else:
        contact_page = create_page(
            title='联系我们',
            template='contact_page.html',
            language='zh-hans',
            parent=home_page,
            slug='contact',
            in_navigation=True
        )
        # 发布页面
        admin_user = User.objects.filter(is_superuser=True).first()
        if admin_user:
            try:
                publish_page(contact_page, admin_user, 'zh-hans')
            except:
                pass  # 忽略发布错误
        print("  ✓ 创建联系我们页面")
    
    print("✓ CMS页面创建完成")


def create_product_categories():
    """创建商品分类"""
    print("创建商品分类...")
    categories = [
        {
            'name': '中药材',
            'description': '传统中药材，包括各种草药、根茎类药材',
            'parent': None
        },
        {
            'name': '中成药',
            'description': '经过加工制成的中药制剂',
            'parent': None
        },
        {
            'name': '保健品',
            'description': '具有保健功能的营养补充剂',
            'parent': None
        },
        {
            'name': '医疗器械',
            'description': '各类医疗设备和器械',
            'parent': None
        },
        {
            'name': '常用药材',
            'description': '日常常用的中药材',
            'parent': '中药材'
        },
        {
            'name': '名贵药材',
            'description': '珍贵稀有的中药材',
            'parent': '中药材'
        },
        {
            'name': '感冒药',
            'description': '治疗感冒的中成药',
            'parent': '中成药'
        },
        {
            'name': '消化药',
            'description': '调理消化系统的中成药',
            'parent': '中成药'
        }
    ]
    
    # 先创建父分类
    parent_categories = {}
    for cat_data in categories:
        if cat_data['parent'] is None:
            cat, created = ProductCategory.objects.get_or_create(
                name=cat_data['name'],
                defaults={
                    'description': cat_data['description'],
                    'is_active': True,
                    'sort_order': len(parent_categories) * 10
                }
            )
            parent_categories[cat_data['name']] = cat
            if created:
                print(f"  ✓ 创建分类: {cat.name}")
    
    # 再创建子分类
    for cat_data in categories:
        if cat_data['parent'] is not None:
            parent_cat = parent_categories.get(cat_data['parent'])
            if parent_cat:
                cat, created = ProductCategory.objects.get_or_create(
                    name=cat_data['name'],
                    defaults={
                        'description': cat_data['description'],
                        'parent': parent_cat,
                        'is_active': True,
                        'sort_order': 10
                    }
                )
                if created:
                    print(f"  ✓ 创建子分类: {parent_cat.name} - {cat.name}")
    
    print("✓ 商品分类创建完成")


def create_sample_products():
    """从ai_medical.db数据库读取商品数据并创建到Django CMS"""
    print("从ai_medical.db数据库读取商品数据...")
    
    # 连接到ai_medical.db数据库
    db_path = os.path.join(os.path.dirname(__file__), 'ai_medical.db')
    if not os.path.exists(db_path):
        print(f"  ⚠️ 数据库文件不存在: {db_path}")
        return
    
    try:
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 查询products表中的所有商品
        cursor.execute("""
            SELECT id, name, price, description, image_url, category, stock, 
                   specification, manufacturer, purchase_count, created_time, updated_time
            FROM products
        """)
        
        products_data = cursor.fetchall()
        conn.close()
        
        if not products_data:
            print("  ⚠️ ai_medical.db中没有找到商品数据")
            return
            
        print(f"  找到 {len(products_data)} 个商品")
        
    except Exception as e:
        print(f"  ⚠️ 读取ai_medical.db数据库失败: {e}")
        return
    
    # 获取默认分类和科室
    try:
        # 尝试获取现有分类，如果不存在则创建默认分类
        default_category, _ = ProductCategory.objects.get_or_create(
            name='从AI数据库导入',
            defaults={'description': '从ai_medical.db数据库导入的商品分类'}
        )
        
        default_department, _ = MedicalDepartment.objects.get_or_create(
            name='综合科室',
            defaults={'description': '综合医疗科室'}
        )
        
    except Exception as e:
        print(f"  ⚠️ 获取默认分类或科室失败: {e}")
        return
    
    # 分类映射字典
    category_mapping = {
        '中药材': '常用药材',
        '中成药': '感冒药', 
        '保健品': '保健品',
        '医疗器械': '医疗器械',
        '西药': '感冒药'
    }
    
    created_count = 0
    for product_row in products_data:
        try:
            # 解析数据库字段
            ai_id, name, price, description, image_url, category, stock, \
            specification, manufacturer, purchase_count, created_time, updated_time = product_row
            
            # 尝试匹配分类
            product_category = default_category
            if category and category in category_mapping:
                try:
                    product_category = ProductCategory.objects.get(name=category_mapping[category])
                except ProductCategory.DoesNotExist:
                    pass
            
            # 构建商品数据
            product_data = {
                'name': name,
                'description': description or f'{name}的详细描述',
                'short_description': description[:100] if description else f'{name}',
                'category': product_category,
                'department': default_department,
                'price': Decimal(str(price)) if price else Decimal('0.00'),
                'stock_quantity': stock if stock else 0,
                'manufacturer': manufacturer or '未知厂商',
                'usage_instructions': specification or '请遵医嘱使用',
                'status': 'active',
                'is_featured': purchase_count > 10 if purchase_count else False,
                'sales_count': purchase_count if purchase_count else 0
            }
            
            # 创建或更新商品
            product, created = Product.objects.get_or_create(
                name=name,
                defaults=product_data
            )
            
            if created:
                created_count += 1
                print(f"  ✓ 创建商品: {product.name} - ¥{product.price} (库存: {product.stock_quantity})")
            else:
                print(f"  - 商品已存在: {product.name}")
                
        except Exception as e:
            print(f"  ⚠️ 处理商品数据失败: {e}")
            continue
    
    print(f"✓ 商品数据导入完成，共创建 {created_count} 个新商品")


def main():
    """主函数"""
    print("=" * 50)
    print("Django CMS 医疗管理系统初始化")
    print("=" * 50)
    
    try:
        # 执行数据库迁移
        print("执行数据库迁移...")
        execute_from_command_line(['manage.py', 'migrate'])
        print("✓ 数据库迁移完成")
        
        # 创建初始数据
        create_superuser()
        setup_site()
        create_medical_departments()
        create_sample_doctors()
        create_sample_news()
        create_cms_pages()
        create_product_categories()
        create_sample_products()
        
        print("\n" + "=" * 50)
        print("初始化完成！")
        print("=" * 50)
        print("管理员账号: admin")
        print("管理员密码: admin123")
        print("访问地址: http://localhost:8080/")
        print("管理后台: http://localhost:8080/admin/")
        print("=" * 50)
        
    except Exception as e:
        print(f"初始化过程中出现错误: {e}")
        sys.exit(1)


if __name__ == '__main__':
    main()