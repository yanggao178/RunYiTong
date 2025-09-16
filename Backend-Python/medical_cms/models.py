from django.db import models
from django.contrib.auth.models import AbstractUser, BaseUserManager
from django.utils.translation import gettext_lazy as _
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
import json
from django.utils import timezone
import sqlite3
import os
import datetime
from django.core.files.storage import default_storage

# 自定义UserManager
class CustomUserManager(BaseUserManager):
    """自定义用户管理器，支持邮箱和用户名登录"""
    
    def create_user(self, username, email, password=None, **extra_fields):
        """创建并保存普通用户"""
        if not email:
            raise ValueError(_('用户必须有邮箱地址'))
        if not username:
            raise ValueError(_('用户必须有用户名'))
        
        email = self.normalize_email(email)
        user = self.model(username=username, email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user
    
    def create_superuser(self, username, email, password=None, **extra_fields):
        """创建并保存超级用户"""
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        
        if extra_fields.get('is_staff') is not True:
            raise ValueError(_('超级用户必须设置 is_staff=True'))
        if extra_fields.get('is_superuser') is not True:
            raise ValueError(_('超级用户必须设置 is_superuser=True'))
        
        return self.create_user(username, email, password, **extra_fields)

# 自定义User模型，基于AbstractUser但使用ai_medical.db中的users表结构
class User(AbstractUser):
    """自定义用户模型，与ai_medical.db中的users表结构对应"""
    # 删除Django默认的first_name和last_name字段
    first_name = None
    last_name = None
    
    # 重写password字段，与数据库中的hashed_password对应
    password = models.CharField(max_length=255, verbose_name=_('密码'), blank=True)
    
    # 使用full_name替代first_name和last_name
    full_name = models.CharField(max_length=255, verbose_name=_('全名'), blank=True, null=True)
    phone = models.CharField(max_length=20, verbose_name=_('手机号码'), blank=True, null=True)
    avatar_url = models.CharField(max_length=500, verbose_name=_('头像URL'), blank=True, null=True)
    created_time = models.DateTimeField(verbose_name=_('创建时间'), default=timezone.now)
    updated_time = models.DateTimeField(verbose_name=_('更新时间'), auto_now=True)
    
    # 数据库字段映射
    hashed_password = models.CharField(max_length=255, verbose_name=_('哈希密码'), blank=True)
    
    # 添加last_login字段，与AbstractUser兼容
    last_login = models.DateTimeField(verbose_name=_('最后登录时间'), blank=True, null=True)
    
    # 添加is_superuser字段，与AbstractUser兼容
    is_superuser = models.BooleanField(verbose_name=_('是否超级用户'), default=False)
    
    # 添加is_staff字段，与AbstractUser兼容
    is_staff = models.BooleanField(verbose_name=_('是否工作人员'), default=False)
    
    # 添加date_joined字段，与AbstractUser兼容
    date_joined = models.DateTimeField(verbose_name=_('加入时间'), default=timezone.now)
    
    # 重写password的getter和setter方法
    def _get_password(self):
        return self.hashed_password
    
    def _set_password(self, raw_password):
        # 调用AbstractUser的set_password方法来处理密码加密
        from django.contrib.auth.hashers import make_password
        self.hashed_password = make_password(raw_password)
    
    password = property(_get_password, _set_password)
    
    # 设置自定义管理器
    objects = CustomUserManager()
    
    # 解决反向访问器冲突
    groups = models.ManyToManyField(
        'auth.Group',
        verbose_name=_('groups'),
        blank=True,
        help_text=_('The groups this user belongs to. A user will get all permissions granted to each of their groups.'),
        related_name='medical_user_set',
        related_query_name='user',
    )
    user_permissions = models.ManyToManyField(
        'auth.Permission',
        verbose_name=_('user permissions'),
        blank=True,
        help_text=_('Specific permissions for this user.'),
        related_name='medical_user_set',
        related_query_name='user',
    )
    
    class Meta:
        verbose_name = _('用户')
        verbose_name_plural = _('用户')
        db_table = 'users'  # 指向已有的users表
    
    def __str__(self):
        return self.username
    
    def save(self, *args, **kwargs):
        # 确保更新时间总是被设置
        self.updated_time = timezone.now()
        super().save(*args, **kwargs)


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


# 医院相关模型
def get_hospital_image_path(instance, filename):
    # 文件将被上传到 MEDIA_ROOT/hospital_images/hospital_<id>/<filename>
    return f'hospital_images/hospital_{instance.id}/{filename}'

def get_hospital_category_image_path(instance, filename):
    # 文件将被上传到 MEDIA_ROOT/hospital_category_images/<filename>
    return f'hospital_category_images/{filename}'

class HospitalCategory(models.Model):
    """医院分类模型"""
    name = models.CharField(_('分类名称'), max_length=100)
    description = models.TextField(_('分类描述'), blank=True)
    image = models.ImageField(
        verbose_name=_('分类图片'),
        upload_to=get_hospital_category_image_path,
        blank=True,
        null=True
    )
    is_active = models.BooleanField(_('是否启用'), default=True)
    sort_order = models.PositiveIntegerField(_('排序'), default=0)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('医院分类')
        verbose_name_plural = _('医院分类')
        ordering = ['sort_order', 'name']
    
    def __str__(self):
        return self.name

class HospitalImage(models.Model):
    """医院图库图片模型"""
    hospital = models.ForeignKey('Hospital', on_delete=models.CASCADE, related_name='gallery_images')
    image = models.ImageField(upload_to=get_hospital_image_path)
    order = models.PositiveIntegerField(default=0)
    
    class Meta:
        ordering = ['order']
        verbose_name = _('医院图库图片')
        verbose_name_plural = _('医院图库图片')
    
    def __str__(self):
        return f"{self.hospital.name} - 图片 {self.order}"


# 商品相关模型
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


class Hospital(models.Model):
    """医院模型"""
    STATUS_CHOICES = [
        ('draft', _('草稿')),
        ('active', _('已上线')),
        ('inactive', _('已下线')),
    ]
    
    name = models.CharField(_('医院名称'), max_length=200)
    slug = models.SlugField(_('URL别名'), unique=True, blank=True)
    description = models.TextField(_('医院描述'))
    short_description = models.TextField(_('简短描述'), max_length=500, blank=True)
    category = models.ForeignKey(
        HospitalCategory,
        on_delete=models.CASCADE,
        verbose_name=_('医院分类')
    )
    department = models.ForeignKey(
        MedicalDepartment,
        on_delete=models.CASCADE,
        verbose_name=_('相关科室'),
        blank=True,
        null=True
    )
    address = models.CharField(_('医院地址'), max_length=500)
    phone = models.CharField(_('联系电话'), max_length=50)
    email = models.EmailField(_('邮箱'), blank=True)
    website = models.URLField(_('网站'), blank=True)
    rating = models.DecimalField(_('评分'), max_digits=3, decimal_places=1, default=0.0, blank=True)
    featured_image = models.ImageField(
        verbose_name=_('特色图片'),
        upload_to=get_hospital_image_path,
        blank=True,
        null=True
    )
    services_offered = models.TextField(_('提供服务'), blank=True)
    tags = models.CharField(_('标签'), max_length=200, blank=True)
    status = models.CharField(_('状态'), max_length=20, choices=STATUS_CHOICES, default='draft')
    is_featured = models.BooleanField(_('是否推荐'), default=False)
    is_affiliated = models.BooleanField(_('是否合作'), default=False)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('医院')
        verbose_name_plural = _('医院')
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['status', 'is_featured']),
            models.Index(fields=['category', 'status']),
            models.Index(fields=['department', 'status']),
        ]
    
    def __str__(self):
        return self.name
    
    def save(self, *args, **kwargs):
        # 自动生成slug如果为空
        if not self.slug:
            self.slug = slugify(self.name)
        
        # 确保slug唯一
        if self.id is None:
            # 新建对象时检查slug唯一性
            slug = self.slug
            counter = 1
            while Hospital.objects.filter(slug=slug).exists():
                slug = f"{self.slug}-{counter}"
                counter += 1
            self.slug = slug
        else:
            # 更新对象时检查slug唯一性（排除当前对象）
            existing = Hospital.objects.filter(slug=self.slug).exclude(id=self.id).first()
            if existing:
                slug = self.slug
                counter = 1
                while Hospital.objects.filter(slug=slug).exclude(id=self.id).exists():
                    slug = f"{self.slug}-{counter}"
                    counter += 1
                self.slug = slug
        
        super().save(*args, **kwargs)

# 导入信号处理相关模块
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
import sqlite3
from datetime import datetime

@receiver(post_save, sender=Hospital)
@receiver(post_delete, sender=Hospital)
def sync_hospital_to_ai_db(sender, instance, created=None, **kwargs):
    """将医院数据同步到ai_medical.db"""
    # 判断是哪种信号触发的
    signal_type = kwargs.get('signal')
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        # 连接到ai_medical.db
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 创建医院表（如果不存在）
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS hospitals (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                slug TEXT,
                description TEXT,
                short_description TEXT,
                category_id INTEGER,
                department_id INTEGER,
                address TEXT,
                phone TEXT,
                email TEXT,
                website TEXT,
                rating REAL DEFAULT 0,
                featured_image_url TEXT,
                services_offered TEXT,
                tags TEXT,
                status TEXT,
                is_featured INTEGER DEFAULT 0,
                is_affiliated INTEGER DEFAULT 0,
                created_at TEXT,
                updated_at TEXT
            )
        ''')
        
        now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 准备医院数据
        featured_image_url = str(instance.featured_image.url) if instance.featured_image else ''
        
        hospital_data = {
            'id': instance.id,
            'name': instance.name[:200],
            'slug': instance.slug or '',
            'description': instance.description or '',
            'short_description': (instance.short_description or '')[:500],
            'category_id': instance.category.id if instance.category else None,
            'department_id': instance.department.id if instance.department else None,
            'address': instance.address[:500],
            'phone': instance.phone[:50],
            'email': instance.email or '',
            'website': instance.website or '',
            'rating': float(instance.rating) if instance.rating else 0.0,
            'featured_image_url': featured_image_url,
            'services_offered': instance.services_offered or '',
            'tags': (instance.tags or '')[:200],
            'status': instance.status or 'draft',
            'is_featured': 1 if instance.is_featured else 0,
            'is_affiliated': 1 if instance.is_affiliated else 0,
            'created_at': instance.created_at.strftime('%Y-%m-%d %H:%M:%S') if instance.created_at else now,
            'updated_at': now
        }
        
        # 根据信号类型执行不同的操作
        if signal_type == post_save:
            # 保存操作（创建或更新）
            # 检查记录是否存在
            cursor.execute("SELECT id FROM hospitals WHERE id = ?", (instance.id,))
            exists = cursor.fetchone() is not None
            
            if exists:
                # 更新已有记录
                update_fields = ', '.join([f"{key} = ?" for key in hospital_data.keys()])
                values = list(hospital_data.values())
                
                cursor.execute(f"""
                    UPDATE hospitals
                    SET {update_fields}
                    WHERE id = ?
                """, values)
                print(f"✓ 医院已更新到ai_medical.db: {hospital_data['name']}")
            else:
                # 插入新记录
                columns = ', '.join(hospital_data.keys())
                placeholders = ', '.join(['?' for _ in hospital_data])
                values = list(hospital_data.values())
                
                cursor.execute(f"""
                    INSERT INTO hospitals ({columns})
                    VALUES ({placeholders})
                """, values)
                print(f"✓ 新医院已插入到ai_medical.db: {hospital_data['name']}")
        elif signal_type == post_delete:
            # 删除操作
            cursor.execute("DELETE FROM hospitals WHERE id = ?", (instance.id,))
            print(f"✓ 医院已从ai_medical.db删除: {instance.name}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 同步医院到ai_medical.db失败: {e}")
        import traceback
        traceback.print_exc()

class Product(models.Model):
    """商品模型"""
    STATUS_CHOICES = [
        ('draft', _('草稿')),
        ('active', _('上架')),
        ('inactive', _('下架')),
        ('out_of_stock', _('缺货')),
    ]
    
    # 明确定义主键字段
    id = models.BigAutoField(primary_key=True)
    
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
    featured_image_file = models.ImageField(
        verbose_name=_('主图片文件'),
        upload_to='product_images/',
        blank=True,
        null=True
    )
    tags = models.CharField(_('标签'), max_length=200, blank=True, help_text=_('用逗号分隔多个标签'))
    status = models.CharField(_('状态'), max_length=20, choices=STATUS_CHOICES, default='draft')
    is_featured = models.BooleanField(_('是否推荐'), default=False)
    is_prescription_required = models.BooleanField(_('是否需要处方'), default=False)
    manufacturer = models.CharField(_('生产厂家'), max_length=200, blank=True)
    pharmacy_name = models.CharField(_('药店名称'), max_length=200, blank=True)
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
@receiver(post_save, sender=ProductImage)
@receiver(post_delete, sender=ProductImage)
def sync_product_to_ai_db(sender, instance, created=False, **kwargs):
    """当Product或ProductImage模型保存/删除时，同步数据到ai_medical.db
    
    新表结构字段映射说明:
    - Django Product -> ai_medical.db products 完整字段映射
    - 支持所有Django CMS Product模型字段同步到新的表结构
    - 处理ProductImage变更时，会刷新并同步相关联的Product
    """
    
    # 如果是ProductImage实例，则获取对应的Product实例
    if isinstance(instance, ProductImage):
        product = instance.product
        # 为了避免无限循环，检查是否已经在同步过程中
        if hasattr(product, '_syncing_to_ai_db') and product._syncing_to_ai_db:
            return
        product._syncing_to_ai_db = True
        try:
            # 重新获取product实例以确保拥有最新数据
            product.refresh_from_db()
            # 执行同步操作，但传入product作为实例
            perform_product_sync(product)
        finally:
            product._syncing_to_ai_db = False
        return
    
    # 如果是Product实例，直接执行同步操作
    if isinstance(instance, Product):
        perform_product_sync(instance)


def perform_product_sync(instance):
    """执行商品数据同步到ai_medical.db的核心逻辑
    
    处理字段映射、图库图片JSON转换、数据插入/更新等操作
    """
    # 添加调试信息
    print(f"DEBUG: Product sync triggered. Instance ID: {instance.id}")
    
    # 确保实例有ID，如果没有则使用另一种方式获取
    if instance.id is None:
        print("DEBUG: Instance ID is None, trying alternative methods...")
        
        try:
            # 查找具有相同名称的商品
            existing_products = Product.objects.filter(name=instance.name)
            if existing_products.count() == 1:
                instance = existing_products.first()
                print(f"DEBUG: Found product by name, ID: {instance.id}")
            elif existing_products.count() > 1:
                # 如果有多个同名商品，选择最新的
                instance = existing_products.order_by('-created_at').first()
                print(f"DEBUG: Found multiple products by name, using latest, ID: {instance.id}")
        except Exception as e:
            print(f"DEBUG: Error finding product by name: {e}")
        
        # 如果仍然没有ID，直接返回
        if instance.id is None:
            print(f"⚠️ 无法同步商品 '{instance.name}'：ID仍未生成")
            return
    
    # 确保即使刷新后仍然有ID
    if instance.id is None:
        print(f"⚠️ 无法同步商品 '{instance.name}'：ID仍未生成")
        return
    
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 检查ai_medical.db中是否已存在相同ID的商品
        cursor.execute("SELECT COUNT(*) FROM products WHERE id = ?", (instance.id,))
        exists_count = cursor.fetchone()[0]
        
        # 准备数据
        now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 映射Django分类ID到AI数据库分类ID
        category_id = instance.category.id if instance.category else None
        department_id = instance.department.id if instance.department else None
        
        # 正确处理ImageFieldFile对象
        featured_image_file = str(instance.featured_image_file) if instance.featured_image_file else ''
        if len(featured_image_file) > 500:
            featured_image_file = featured_image_file[:500]  # 截断到500字符
        
        # 处理gallery_images (转换为JSON格式)
        gallery_images_json = '[]'
        # 检查实例是否有主键，只有有主键的实例才能访问关联对象
        if instance.pk and hasattr(instance, 'gallery_images') and instance.gallery_images.exists():
            import json
            gallery_urls = [f'/media/{img.image.name}' for img in instance.gallery_images.all()]
            gallery_images_json = json.dumps(gallery_urls)
        
        # 准备所有字段数据
        product_data = {
            'id': instance.id,
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
            'featured_image_file': featured_image_file,
            'gallery_images': gallery_images_json,
            'tags': (instance.tags or '')[:200],
            'status': instance.status or 'draft',
            'is_featured': 1 if instance.is_featured else 0,
            'is_prescription_required': 1 if instance.is_prescription_required else 0,
            'manufacturer': (instance.manufacturer or '')[:200],
            'pharmacy_name': (instance.pharmacy_name or '')[:200],
            'expiry_date': instance.expiry_date.strftime('%Y-%m-%d') if instance.expiry_date else None,
            'usage_instructions': instance.usage_instructions or '',
            'side_effects': instance.side_effects or '',
            'contraindications': instance.contraindications or '',
            'views_count': int(instance.views_count) if instance.views_count is not None else 0,
            'sales_count': int(instance.sales_count) if instance.sales_count is not None else 0,
            'created_at': instance.created_at.strftime('%Y-%m-%d %H:%M:%S') if instance.created_at else now,
            'updated_at': now
        }
        
        if exists_count > 0:
            # 更新商品：根据ID更新ai_medical.db中的记录
            set_clause = ', '.join([f'{key} = ?' for key in product_data.keys() if key != 'created_at'])
            update_values = [value for key, value in product_data.items() if key != 'created_at']
            
            cursor.execute(f"""
                UPDATE products SET {set_clause}
                WHERE id = ?
            """, update_values + [instance.id])
            
            if cursor.rowcount > 0:
                print(f"✓ 商品已更新到ai_medical.db: {product_data['name']}")
            else:
                print(f"⚠️ 更新商品到ai_medical.db失败: {product_data['name']}")
        else:
            # 新建商品：插入到ai_medical.db
            columns = ', '.join(product_data.keys())
            placeholders = ', '.join(['?' for _ in product_data])
            values = list(product_data.values())
            
            cursor.execute(f"""
                INSERT INTO products ({columns})
                VALUES ({placeholders})
            """, values)
            print(f"✓ 新商品已同步到ai_medical.db: {product_data['name']}")
        
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


# 信号处理器：同步用户数据到ai_medical.db
@receiver(post_save, sender=User)
@receiver(post_save, sender='auth.User')
def sync_user_to_ai_db(sender, instance, created, **kwargs):
    """当User模型保存时，同步数据到ai_medical.db
    
    同步所有Django CMS User模型的增删改查操作到ai_medical.db的users表
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
        now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 准备所有字段数据
        # 处理可能是auth.User或medical_cms.User的情况
        if hasattr(instance, 'full_name'):
            full_name = instance.full_name or ''
            phone = instance.phone or ''
            avatar_url = instance.avatar_url or ''
            hashed_password = instance.hashed_password or ''
            created_time = instance.created_time.strftime('%Y-%m-%d %H:%M:%S') if instance.created_time else now
        else:
            # 如果是auth.User，则使用first_name和last_name组合成full_name
            full_name = f"{instance.first_name or ''} {instance.last_name or ''}".strip()
            phone = ''
            avatar_url = ''
            hashed_password = instance.password or ''
            created_time = now
        
        # 处理last_login
        last_login = instance.last_login.strftime('%Y-%m-%d %H:%M:%S') if instance.last_login else None
        
        user_data = {
            'username': (instance.username or '')[:150],
            'email': (instance.email or '')[:254],
            'hashed_password': hashed_password,
            'full_name': full_name[:255],
            'phone': phone[:20],
            'avatar_url': avatar_url[:500],
            'is_active': 1 if instance.is_active else 0,
            'created_time': created_time,
            'updated_time': now,
            'last_login': last_login,
            'is_superuser': 1 if instance.is_superuser else 0,
            'is_staff': 1 if instance.is_staff else 0,
        }
        
        if created:
            # 新建用户：插入到ai_medical.db
            columns = ', '.join(user_data.keys())
            placeholders = ', '.join(['?' for _ in user_data])
            values = list(user_data.values())
            
            cursor.execute(f"""
                INSERT INTO users ({columns})
                VALUES ({placeholders})
            """, values)
            print(f"✓ 新用户已同步到ai_medical.db: {user_data['username']}")
        else:
            # 更新用户：根据用户名更新ai_medical.db中的记录
            set_clause = ', '.join([f'{key} = ?' for key in user_data.keys() if key != 'created_time'])
            update_values = [value for key, value in user_data.items() if key != 'created_time']
            
            cursor.execute(f"""
                UPDATE users SET {set_clause}
                WHERE username = ?
            """, update_values + [user_data['username']])
            
            if cursor.rowcount > 0:
                print(f"✓ 用户已更新到ai_medical.db: {user_data['username']}")
            else:
                # 如果更新失败，插入新记录
                columns = ', '.join(user_data.keys())
                placeholders = ', '.join(['?' for _ in user_data])
                values = list(user_data.values())
                
                cursor.execute(f"""
                    INSERT INTO users ({columns})
                    VALUES ({placeholders})
                """, values)
                print(f"✓ 新用户已插入到ai_medical.db: {user_data['username']}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 同步用户到ai_medical.db失败: {e}")
        import traceback
        traceback.print_exc()


@receiver(post_delete, sender=User)
@receiver(post_delete, sender='auth.User')
def delete_user_from_ai_db(sender, instance, **kwargs):
    """当User模型删除时，从ai_medical.db中删除对应记录"""
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 根据用户名删除记录
        cursor.execute("DELETE FROM users WHERE username = ?", (instance.username,))
        
        if cursor.rowcount > 0:
            print(f"✓ 用户已从ai_medical.db删除: {instance.username}")
        else:
            print(f"⚠️ 在ai_medical.db中未找到用户: {instance.username}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 从ai_medical.db删除用户失败: {e}")


class BookCategory(models.Model):
    """书籍分类模型"""
    name = models.CharField(_('分类名称'), max_length=100)
    description = models.TextField(_('分类描述'), blank=True)
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
        verbose_name = _('书籍分类')
        verbose_name_plural = _('书籍分类')
        ordering = ['sort_order', 'name']
    
    def __str__(self):
        if self.parent:
            return f"{self.parent.name} - {self.name}"
        return self.name


class BookTag(models.Model):
    """书籍标签模型"""
    name = models.CharField(_('标签名称'), max_length=50, unique=True)
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_at = models.DateTimeField(_('更新时间'), auto_now=True)
    
    class Meta:
        verbose_name = _('书籍标签')
        verbose_name_plural = _('书籍标签')
        ordering = ['name']
    
    def __str__(self):
        return self.name


class BookTagRelation(models.Model):
    """书籍和标签的多对多关系模型"""
    book = models.ForeignKey('Book', on_delete=models.CASCADE, verbose_name=_('书籍'), related_name='tag_relations')
    tag = models.ForeignKey('BookTag', on_delete=models.CASCADE, verbose_name=_('标签'), related_name='book_relations')
    created_at = models.DateTimeField(_('创建时间'), auto_now_add=True)
    
    class Meta:
        verbose_name = _('书籍-标签关系')
        verbose_name_plural = _('书籍-标签关系')
        unique_together = ('book', 'tag')
    
    def __str__(self):
        return f"{self.book.name} - {self.tag.name}"


class Book(models.Model):
    """书籍模型"""
    name = models.CharField(_('书籍名称'), max_length=255)
    author = models.CharField(_('作者'), max_length=255)
    category = models.CharField(_('分类'), max_length=100, blank=True, null=True)  # 为了与ai_medical.db兼容，使用CharField
    description = models.TextField(_('书籍描述'), blank=True)
    cover_url = models.CharField(_('封面URL'), max_length=500, blank=True)
    pdf_file_path = models.CharField(_('PDF文件路径'), max_length=500, blank=True)
    file_size = models.IntegerField(_('文件大小(字节)'), blank=True, null=True)
    publish_date = models.DateTimeField(_('出版日期'), blank=True, null=True)
    created_time = models.DateTimeField(_('创建时间'), auto_now_add=True)
    updated_time = models.DateTimeField(_('更新时间'), auto_now=True)
    
    # 标签通过中间表关联
    tags = models.ManyToManyField(BookTag, through='BookTagRelation', verbose_name=_('标签'), blank=True)
    
    class Meta:
        verbose_name = _('书籍')
        verbose_name_plural = _('书籍')
        ordering = ['-created_time']
    
    def __str__(self):
        return self.name


# 信号处理器：同步书籍数据到ai_medical.db
@receiver(post_save, sender=Book)
@receiver(post_save, sender=BookTagRelation)
@receiver(post_delete, sender=BookTagRelation)
def sync_book_to_ai_db(sender, instance, created, **kwargs):
    """当Book模型或BookTagRelation模型保存/删除时，同步数据到ai_medical.db的books表
    
    新表结构字段映射说明:
    - Django Book -> ai_medical.db books 完整字段映射
    - 支持所有Django CMS Book模型字段同步到新的表结构
    - 自动处理标签信息
    """
    try:
        # 如果是BookTagRelation实例，则获取对应的Book实例
        if isinstance(instance, BookTagRelation):
            book = instance.book
            is_tag_change = True
        else:
            book = instance
            is_tag_change = False
        
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 准备数据
        now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        
        # 获取标签信息（用逗号分隔）
        tag_names = [tag.name for tag in book.tags.all()]
        tags_str = ','.join(tag_names)
        
        # 准备所有字段数据
        book_data = {
            'name': book.name[:255] if book.name else '',
            'author': book.author[:255] if book.author else '',
            'category': book.category[:100] if book.category else '',
            'description': book.description or '',
            'cover_url': book.cover_url[:500] if book.cover_url else '',
            'pdf_file_path': book.pdf_file_path[:500] if book.pdf_file_path else '',
            'file_size': int(book.file_size) if book.file_size is not None else 0,
            'publish_date': book.publish_date.strftime('%Y-%m-%d %H:%M:%S') if book.publish_date else None,
            'created_time': book.created_time.strftime('%Y-%m-%d %H:%M:%S') if book.created_time else now,
            'updated_time': now
        }
        
        # 检查是否已存在该书籍
        cursor.execute("SELECT id FROM books WHERE name = ? AND author = ?", (book_data['name'], book_data['author']))
        existing_book = cursor.fetchone()
        
        if existing_book:
            # 更新现有书籍
            set_clause = ', '.join([f'{key} = ?' for key in book_data.keys() if key != 'created_time'])
            update_values = [value for key, value in book_data.items() if key != 'created_time']
            update_values.append(existing_book[0])
            
            cursor.execute(f"""
                UPDATE books SET {set_clause}
                WHERE id = ?
            """, update_values)
            
            if cursor.rowcount > 0:
                print(f"✓ 书籍已更新到ai_medical.db: {book_data['name']}")
        else:
            # 插入新书籍
            columns = ', '.join(book_data.keys())
            placeholders = ', '.join(['?' for _ in book_data])
            values = list(book_data.values())
            
            cursor.execute(f"""
                INSERT INTO books ({columns})
                VALUES ({placeholders})
            """, values)
            print(f"✓ 新书籍已同步到ai_medical.db: {book_data['name']}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 同步书籍到ai_medical.db失败: {e}")
        import traceback
        traceback.print_exc()


@receiver(post_delete, sender=Book)
def delete_book_from_ai_db(sender, instance, **kwargs):
    """当Book模型删除时，从ai_medical.db中删除对应记录"""
    try:
        # 获取ai_medical.db路径
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'ai_medical.db')
        
        if not os.path.exists(db_path):
            print(f"Warning: ai_medical.db not found at {db_path}")
            return
        
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        # 根据名称和作者删除记录
        cursor.execute("DELETE FROM books WHERE name = ? AND author = ?", (instance.name, instance.author))
        
        if cursor.rowcount > 0:
            print(f"✓ 书籍已从ai_medical.db删除: {instance.name}")
        else:
            print(f"⚠️ 在ai_medical.db中未找到书籍: {instance.name}")
        
        conn.commit()
        conn.close()
        
    except Exception as e:
        print(f"⚠️ 从ai_medical.db删除书籍失败: {e}")











