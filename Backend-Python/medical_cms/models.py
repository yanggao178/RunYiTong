from django.db import models
from django.contrib.auth.models import User
from django.utils.translation import gettext_lazy as _
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
import sqlite3
import os
from datetime import datetime


class MedicalDepartment(models.Model):
    """医疗科室模型"""
    name = models.CharField(_('科室名称'), max_length=100)
    description = models.TextField(_('科室描述'), blank=True)
    image = models.ImageField(
        verbose_name=_('科室图片'),
        upload_to='department_images/',
        blank=True,
        null=True
    )
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    is_active = models.BooleanField(_('是否启用'), default=True)
    
    class Meta:
        verbose_name = _('医疗科室')
        verbose_name_plural = _('医疗科室')
        ordering = ['name']
    
    def __str__(self):
        return self.name


class Doctor(models.Model):
    """医生模型"""
    TITLE_CHOICES = [
        ('resident', _('住院医师')),
        ('attending', _('主治医师')),
        ('associate', _('副主任医师')),
        ('chief', _('主任医师')),
    ]
    
    user = models.OneToOneField(User, on_delete=models.CASCADE, verbose_name=_('用户'))
    title = models.CharField(_('职称'), max_length=20, choices=TITLE_CHOICES)
    department = models.ForeignKey(
        MedicalDepartment,
        on_delete=models.CASCADE,
        verbose_name=_('所属科室')
    )
    specialization = models.CharField(_('专业特长'), max_length=200, blank=True)
    bio = models.TextField(_('个人简介'), blank=True)
    photo = models.ImageField(
        verbose_name=_('医生照片'),
        upload_to='doctor_photos/',
        blank=True,
        null=True
    )
    phone = models.CharField(_('联系电话'), max_length=20, blank=True)
    email = models.EmailField(_('邮箱'), blank=True)
    is_available = models.BooleanField(_('是否可预约'), default=True)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('医生')
        verbose_name_plural = _('医生')
        ordering = ['department', 'title']
    
    def __str__(self):
        return f"{self.user.get_full_name() or self.user.username} - {self.get_title_display()}"


class MedicalNews(models.Model):
    """医疗新闻模型"""
    title = models.CharField(_('标题'), max_length=200)
    slug = models.SlugField(_('URL别名'), unique=True)
    content = models.TextField(_('内容'))
    excerpt = models.TextField(_('摘要'), max_length=300, blank=True)
    featured_image = models.ImageField(
        verbose_name=_('特色图片'),
        upload_to='news_images/',
        blank=True,
        null=True
    )
    author = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        verbose_name=_('作者')
    )
    category = models.CharField(_('分类'), max_length=50, blank=True)
    tags = models.CharField(_('标签'), max_length=200, blank=True, help_text=_('用逗号分隔多个标签'))
    is_published = models.BooleanField(_('是否发布'), default=False)
    published_at = models.DateTimeField(_('发布时间'), blank=True, null=True)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    views_count = models.PositiveIntegerField(_('浏览次数'), default=0)
    
    class Meta:
        verbose_name = _('医疗新闻')
        verbose_name_plural = _('医疗新闻')
        ordering = ['-published_at', '-created_at']
    
    def __str__(self):
        return self.title


class MedicalService(models.Model):
    """医疗服务模型"""
    name = models.CharField(_('服务名称'), max_length=100)
    description = models.TextField(_('服务描述'))
    department = models.ForeignKey(
        MedicalDepartment,
        on_delete=models.CASCADE,
        verbose_name=_('所属科室')
    )
    price = models.DecimalField(_('价格'), max_digits=10, decimal_places=2, blank=True, null=True)
    duration = models.PositiveIntegerField(_('服务时长(分钟)'), blank=True, null=True)
    image = models.ImageField(
        verbose_name=_('服务图片'),
        upload_to='service_images/',
        blank=True,
        null=True
    )
    is_active = models.BooleanField(_('是否启用'), default=True)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('医疗服务')
        verbose_name_plural = _('医疗服务')
        ordering = ['department', 'name']
    
    def __str__(self):
        return f"{self.department.name} - {self.name}"


# 原CMS插件模型已移除，因为Django CMS已被卸载


class ProductCategory(models.Model):
    """商品分类模型"""
    name = models.CharField(_('分类名称'), max_length=100)
    description = models.TextField(_('分类描述'), blank=True)
    image = models.ImageField(
        verbose_name=_('分类图片'),
        upload_to='category_images/',
        blank=True,
        null=True
    )
    parent = models.ForeignKey(
        'self',
        on_delete=models.CASCADE,
        verbose_name=_('父分类'),
        blank=True,
        null=True,
        related_name='children'
    )
    is_active = models.BooleanField(_('是否启用'), default=True)
    sort_order = models.PositiveIntegerField(_('排序'), default=0)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('商品分类')
        verbose_name_plural = _('商品分类')
        ordering = ['sort_order', 'name']
    
    def __str__(self):
        if self.parent:
            return f"{self.parent.name} - {self.name}"
        return self.name


class ProductImage(models.Model):
    """商品图库图片模型"""
    product = models.ForeignKey('Product', on_delete=models.CASCADE, related_name='gallery_images')
    image = models.ImageField(upload_to='product_gallery/')
    order = models.PositiveIntegerField(default=0)
    
    class Meta:
        ordering = ['order']
        verbose_name = _('商品图库图片')
        verbose_name_plural = _('商品图库图片')
    
    def __str__(self):
        return f"{self.product.name} - 图片 {self.order}"

class Product(models.Model):
    """商品模型"""
    STATUS_CHOICES = [
        ('draft', _('草稿')),
        ('active', _('上架')),
        ('inactive', _('下架')),
        ('out_of_stock', _('缺货')),
    ]
    
    name = models.CharField(_('商品名称'), max_length=200)
    slug = models.SlugField(_('URL别名'), unique=True, blank=True)
    description = models.TextField(_('商品描述'))
    short_description = models.TextField(_('简短描述'), max_length=500, blank=True)
    category = models.ForeignKey(
        ProductCategory,
        on_delete=models.CASCADE,
        verbose_name=_('商品分类')
    )
    department = models.ForeignKey(
        MedicalDepartment,
        on_delete=models.CASCADE,
        verbose_name=_('相关科室'),
        blank=True,
        null=True
    )
    price = models.DecimalField(_('价格'), max_digits=10, decimal_places=2)
    original_price = models.DecimalField(_('原价'), max_digits=10, decimal_places=2, blank=True, null=True)
    stock_quantity = models.PositiveIntegerField(_('库存数量'), default=0)
    min_stock_level = models.PositiveIntegerField(_('最低库存'), default=5)
    sku = models.CharField(_('商品编码'), max_length=100, unique=True, blank=True)
    barcode = models.CharField(_('条形码'), max_length=100, blank=True)
    weight = models.DecimalField(_('重量(克)'), max_digits=8, decimal_places=2, blank=True, null=True)
    dimensions = models.CharField(_('尺寸(长x宽x高cm)'), max_length=100, blank=True)
    featured_image = models.ImageField(
        verbose_name=_('主图片'),
        upload_to='product_images/',
        blank=True,
        null=True
    )
    tags = models.CharField(_('标签'), max_length=200, blank=True, help_text=_('用逗号分隔多个标签'))
    status = models.CharField(_('状态'), max_length=20, choices=STATUS_CHOICES, default='draft')
    is_featured = models.BooleanField(_('是否推荐'), default=False)
    is_prescription_required = models.BooleanField(_('是否需要处方'), default=False)
    manufacturer = models.CharField(_('生产厂家'), max_length=200, blank=True)
    expiry_date = models.DateField(_('有效期'), blank=True, null=True)
    usage_instructions = models.TextField(_('使用说明'), blank=True)
    side_effects = models.TextField(_('副作用'), blank=True)
    contraindications = models.TextField(_('禁忌症'), blank=True)
    views_count = models.PositiveIntegerField(_('浏览次数'), default=0)
    sales_count = models.PositiveIntegerField(_('销售数量'), default=0)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('商品')
        verbose_name_plural = _('商品')
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['status', 'is_featured']),
            models.Index(fields=['category', 'status']),
            models.Index(fields=['department', 'status']),
        ]
    
    def __str__(self):
        return self.name
    
    def save(self, *args, **kwargs):
        if not self.slug:
            from django.utils.text import slugify
            import uuid
            self.slug = slugify(self.name) + '-' + str(uuid.uuid4())[:8]
        if not self.sku:
            import uuid
            self.sku = 'PRD-' + str(uuid.uuid4())[:8].upper()
        super().save(*args, **kwargs)
    
    @property
    def is_in_stock(self):
        return self.stock_quantity > 0
    
    @property
    def is_low_stock(self):
        return self.stock_quantity <= self.min_stock_level
    
    @property
    def discount_percentage(self):
        if self.original_price and self.original_price > self.price:
            return int((self.original_price - self.price) / self.original_price * 100)
        return 0


# 原商品列表插件已移除，因为Django CMS已被卸载


# 信号处理器：同步商品数据到ai_medical.db
@receiver(post_save, sender=Product)
def sync_product_to_ai_db(sender, instance, created, **kwargs):
    """当Product模型保存时，同步数据到ai_medical.db
    
    新表结构字段映射说明:
    - Django Product -> ai_medical.db products 完整字段映射
    - 支持所有Django CMS Product模型字段同步到新的表结构
    """
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 准备数据
        now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 映射Django分类ID到AI数据库分类ID
        category_id = instance.category.id if instance.category else None
        department_id = instance.department.id if instance.department else None
        
        # 构建图片URL
        featured_image_url = ''
        if instance.featured_image:
            featured_image_url = f'/media/{instance.featured_image.file.name}'
            featured_image_url = featured_image_url[:500]  # 截断到500字符
        
        # 处理gallery_images (转换为JSON格式)
        gallery_images_json = '[]'
        if instance.gallery_images.exists():
            import json
            gallery_urls = [f'/media/{img.file.name}' for img in instance.gallery_images.all()]
            gallery_images_json = json.dumps(gallery_urls)
        
        # 准备所有字段数据
        product_data = {
            'name': (instance.name or '')[:200],
            'slug': (instance.slug or '')[:50],
            'description': instance.description or '',
            'short_description': (instance.short_description or '')[:500],
            'category_id': category_id,
            'department_id': department_id,
            'price': float(instance.price) if instance.price else 0.0,
            'original_price': float(instance.original_price) if instance.original_price else None,
            'stock_quantity': int(instance.stock_quantity) if instance.stock_quantity is not None else 0,
            'min_stock_level': int(instance.min_stock_level) if instance.min_stock_level is not None else 5,
            'sku': (instance.sku or '')[:100],
            'barcode': (instance.barcode or '')[:100],
            'weight': float(instance.weight) if instance.weight else None,
            'dimensions': (instance.dimensions or '')[:100],
            'featured_image_url': featured_image_url,
            'gallery_images': gallery_images_json,
            'tags': (instance.tags or '')[:200],
            'status': instance.status or 'draft',
            'is_featured': 1 if instance.is_featured else 0,
            'is_prescription_required': 1 if instance.is_prescription_required else 0,
            'manufacturer': (instance.manufacturer or '')[:200],
            'expiry_date': instance.expiry_date.strftime('%Y-%m-%d') if instance.expiry_date else None,
            'usage_instructions': instance.usage_instructions or '',
            'side_effects': instance.side_effects or '',
            'contraindications': instance.contraindications or '',
            'views_count': int(instance.views_count) if instance.views_count is not None else 0,
            'sales_count': int(instance.sales_count) if instance.sales_count is not None else 0,
            'created_at': instance.created_at.strftime('%Y-%m-%d %H:%M:%S') if instance.created_at else now,
            'updated_at': now
        }
        
        if created:
            # 新建商品：插入到ai_medical.db
            columns = ', '.join(product_data.keys())
            placeholders = ', '.join(['?' for _ in product_data])
            values = list(product_data.values())
            
            cursor.execute(f"""
                INSERT INTO products ({columns})
                VALUES ({placeholders})
            """, values)
            print(f"✓ 新商品已同步到ai_medical.db: {product_data['name']}")
        else:
            # 更新商品：根据ID或名称更新ai_medical.db中的记录
            set_clause = ', '.join([f'{key} = ?' for key in product_data.keys() if key != 'created_at'])
            update_values = [value for key, value in product_data.items() if key != 'created_at']
            
            # 首先尝试根据slug更新（如果有slug）
            if instance.slug:
                cursor.execute(f"""
                    UPDATE products SET {set_clause}
                    WHERE slug = ?
                """, update_values + [instance.slug])
            
            # 如果没有更新到记录，尝试根据名称更新
            if cursor.rowcount == 0:
                cursor.execute(f"""
                    UPDATE products SET {set_clause}
                    WHERE name = ?
                """, update_values + [product_data['name']])
            
            if cursor.rowcount > 0:
                print(f"✓ 商品已更新到ai_medical.db: {product_data['name']}")
            else:
                # 如果更新失败，插入新记录
                columns = ', '.join(product_data.keys())
                placeholders = ', '.join(['?' for _ in product_data])
                values = list(product_data.values())
                
                cursor.execute(f"""
                    INSERT INTO products ({columns})
                    VALUES ({placeholders})
                """, values)
                print(f"✓ 新商品已插入到ai_medical.db: {product_data['name']}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 同步商品到ai_medical.db失败: {e}")
        import traceback
        traceback.print_exc()


@receiver(post_delete, sender=Product)
def delete_product_from_ai_db(sender, instance, **kwargs):
    """当Product模型删除时，从ai_medical.db中删除对应记录"""
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 根据slug或名称删除记录
        if instance.slug:
            cursor.execute("DELETE FROM products WHERE slug = ?", (instance.slug,))
        
        if cursor.rowcount == 0:
            cursor.execute("DELETE FROM products WHERE name = ?", (instance.name,))
        
        if cursor.rowcount > 0:
            print(f"✓ 商品已从ai_medical.db删除: {instance.name}")
        else:
            print(f"⚠️ 在ai_medical.db中未找到商品: {instance.name}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 从ai_medical.db删除商品失败: {e}")