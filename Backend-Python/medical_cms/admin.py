from django.contrib import admin
from django.utils.translation import gettext_lazy as _
from django.utils.html import format_html
from .models import (
    MedicalDepartment, Doctor, MedicalNews, MedicalService,
    ProductCategory, Product, ProductImage,
    BookCategory, BookTag, Book, BookTagRelation,
    Hospital, HospitalCategory, HospitalImage
)


@admin.register(MedicalDepartment)
class MedicalDepartmentAdmin(admin.ModelAdmin):
    list_display = ['name', 'is_active', 'created_at', 'updated_at']
    list_filter = ['is_active', 'created_at']
    search_fields = ['name', 'description']
    list_editable = ['is_active']
    readonly_fields = ['created_at', 'updated_at']
    
    fieldsets = (
        (None, {
            'fields': ('name', 'description', 'image', 'is_active')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


@admin.register(Doctor)
class DoctorAdmin(admin.ModelAdmin):
    list_display = ['get_full_name', 'title', 'department', 'is_available', 'created_at']
    list_filter = ['title', 'department', 'is_available', 'created_at']
    search_fields = ['user__first_name', 'user__last_name', 'user__username', 'specialization']
    list_editable = ['is_available']
    readonly_fields = ['created_at', 'updated_at']
    raw_id_fields = ['user']
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('user', 'title', 'department', 'specialization')
        }),
        (_('详细信息'), {
            'fields': ('bio', 'photo', 'phone', 'email', 'is_available')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_full_name(self, obj):
        return obj.user.get_full_name() or obj.user.username
    get_full_name.short_description = _('姓名')
    get_full_name.admin_order_field = 'user__first_name'


@admin.register(MedicalNews)
class MedicalNewsAdmin(admin.ModelAdmin):
    list_display = ['title', 'category', 'author', 'is_published', 'published_at', 'views_count']
    list_filter = ['is_published', 'category', 'published_at', 'created_at']
    search_fields = ['title', 'content', 'tags']
    list_editable = ['is_published']
    readonly_fields = ['views_count', 'created_at', 'updated_at']
    prepopulated_fields = {'slug': ('title',)}
    raw_id_fields = ['author']
    date_hierarchy = 'published_at'
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('title', 'slug', 'category', 'tags', 'author')
        }),
        (_('内容'), {
            'fields': ('excerpt', 'content', 'featured_image')
        }),
        (_('发布设置'), {
            'fields': ('is_published', 'published_at')
        }),
        (_('统计信息'), {
            'fields': ('views_count', 'created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def save_model(self, request, obj, form, change):
        if not change:  # 新建时
            obj.author = request.user
        super().save_model(request, obj, form, change)


@admin.register(MedicalService)
class MedicalServiceAdmin(admin.ModelAdmin):
    list_display = ['name', 'department', 'price', 'duration', 'is_active', 'created_at']
    list_filter = ['department', 'is_active', 'created_at']
    search_fields = ['name', 'description']
    list_editable = ['is_active', 'price']
    readonly_fields = ['created_at', 'updated_at']
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'department', 'description', 'image')
        }),
        (_('服务详情'), {
            'fields': ('price', 'duration', 'is_active')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


# 原CMS插件管理代码已移除，因为Django CMS已被卸载


@admin.register(ProductCategory)
class ProductCategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'parent', 'is_active', 'sort_order', 'created_at']
    list_filter = ['is_active', 'parent', 'created_at']
    search_fields = ['name', 'description']
    list_editable = ['is_active', 'sort_order']
    readonly_fields = ['created_at', 'updated_at']
    prepopulated_fields = {}
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'parent', 'description', 'image')
        }),
        (_('设置'), {
            'fields': ('is_active', 'sort_order')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        return super().get_queryset(request).select_related('parent')


# 商品图片内联模型
class ProductImageInline(admin.TabularInline):
    model = ProductImage
    extra = 1
    fields = ('image', 'order')
    verbose_name = _('商品图库图片')
    verbose_name_plural = _('商品图库图片')


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'category', 'department', 'price', 'stock_quantity', 
        'status', 'is_featured', 'sales_count', 'created_at'
    ]
    list_filter = [
        'status', 'is_featured', 'is_prescription_required', 'category', 
        'department', 'created_at', 'expiry_date'
    ]
    search_fields = ['name', 'description', 'sku', 'barcode', 'tags', 'manufacturer']
    list_editable = ['status', 'is_featured', 'price', 'stock_quantity']
    readonly_fields = ['slug', 'sku', 'views_count', 'sales_count', 'created_at', 'updated_at']
    prepopulated_fields = {}
    raw_id_fields = []
    date_hierarchy = 'created_at'
    inlines = [ProductImageInline]
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'slug', 'category', 'department', 'short_description', 'description')
        }),
        (_('价格和库存'), {
            'fields': ('price', 'original_price', 'stock_quantity', 'min_stock_level', 'sku', 'barcode')
        }),
        (_('商品属性'), {
            'fields': ('weight', 'dimensions', 'manufacturer', 'expiry_date', 'tags')
        }),
        (_('图片'), {
            'fields': ('featured_image',)
        }),
        (_('医疗信息'), {
            'fields': ('is_prescription_required', 'usage_instructions', 'side_effects', 'contraindications'),
            'classes': ('collapse',)
        }),
        (_('状态和设置'), {
            'fields': ('status', 'is_featured')
        }),
        (_('统计信息'), {
            'fields': ('views_count', 'sales_count', 'created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        return super().get_queryset(request).select_related('category', 'department')
    
    def save_model(self, request, obj, form, change):
        # 自动生成slug和sku如果为空
        if not obj.slug:
            from django.utils.text import slugify
            import uuid
            obj.slug = slugify(obj.name) + '-' + str(uuid.uuid4())[:8]
        if not obj.sku:
            import uuid
            obj.sku = 'PRD-' + str(uuid.uuid4())[:8].upper()
        super().save_model(request, obj, form, change)
    
    # 自定义列表显示方法
    def get_stock_status(self, obj):
        if obj.stock_quantity <= 0:
            return format_html('<span style="color: red;">缺货</span>')
        elif obj.is_low_stock:
            return format_html('<span style="color: orange;">库存不足</span>')
        else:
            return format_html('<span style="color: green;">正常</span>')
    get_stock_status.short_description = _('库存状态')
    
    def get_discount_info(self, obj):
        if obj.discount_percentage > 0:
            return format_html('<span style="color: red;">-{}%</span>', obj.discount_percentage)
        return '-'
    get_discount_info.short_description = _('折扣')


# 原CMS插件和插件发布者代码已移除，因为Django CMS已被卸载


@admin.register(BookCategory)
class BookCategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'parent', 'is_active', 'sort_order', 'created_at']
    list_filter = ['is_active', 'parent', 'created_at']
    search_fields = ['name', 'description']
    list_editable = ['is_active', 'sort_order']
    readonly_fields = ['created_at', 'updated_at']
    prepopulated_fields = {}
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'parent', 'description')
        }),
        (_('设置'), {
            'fields': ('is_active', 'sort_order')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        return super().get_queryset(request).select_related('parent')


@admin.register(BookTag)
class BookTagAdmin(admin.ModelAdmin):
    list_display = ['name', 'created_at', 'updated_at']
    list_filter = ['created_at']
    search_fields = ['name']
    readonly_fields = ['created_at', 'updated_at']
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name',)
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


# 书籍标签关系内联模型
class BookTagRelationInline(admin.TabularInline):
    model = BookTagRelation
    extra = 1
    fields = ('tag',)
    verbose_name = _('书籍标签')
    verbose_name_plural = _('书籍标签')


@admin.register(Book)
class BookAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'author', 'category', 'publish_date', 'created_time'
    ]
    list_filter = ['category', 'publish_date', 'created_time']
    search_fields = ['name', 'author', 'description']
    readonly_fields = ['created_time', 'updated_time']
    date_hierarchy = 'created_time'
    inlines = [BookTagRelationInline]
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'author', 'category', 'description')
        }),
        (_('媒体信息'), {
            'fields': ('cover_url', 'pdf_file_path', 'file_size')
        }),
        (_('发布信息'), {
            'fields': ('publish_date',)
        }),
        (_('时间信息'), {
            'fields': ('created_time', 'updated_time'),
            'classes': ('collapse',)
        }),
    )


# 自定义管理后台标题
admin.site.site_header = _('AI医疗管理系统')
admin.site.site_title = _('AI医疗管理系统')
admin.site.index_title = _('管理首页')


# 医院图片内联模型
class HospitalImageInline(admin.TabularInline):
    model = HospitalImage
    extra = 3
    fields = ('image', 'order')
    verbose_name = _('医院图库图片')
    verbose_name_plural = _('医院图库图片')


@admin.register(HospitalCategory)
class HospitalCategoryAdmin(admin.ModelAdmin):
    list_display = ['name', 'is_active', 'sort_order', 'created_at', 'updated_at']
    list_filter = ['is_active', 'created_at']
    search_fields = ['name', 'description']
    list_editable = ['is_active', 'sort_order']
    readonly_fields = ['created_at', 'updated_at']
    prepopulated_fields = {}
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'description', 'image')
        }),
        (_('设置'), {
            'fields': ('is_active', 'sort_order')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )


@admin.register(Hospital)
class HospitalAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'category', 'department', 'address', 'phone', 
        'rating', 'status', 'is_featured', 'is_affiliated', 
        'created_at', 'updated_at'
    ]
    list_filter = [
        'category', 'department', 'status', 'is_featured', 
        'is_affiliated', 'created_at'
    ]
    search_fields = ['name', 'description', 'address', 'phone', 'email', 'tags']
    list_editable = ['status', 'is_featured', 'is_affiliated']
    readonly_fields = ['slug', 'created_at', 'updated_at']
    prepopulated_fields = {}
    date_hierarchy = 'created_at'
    inlines = [HospitalImageInline]
    
    fieldsets = (
        (_('基本信息'), {
            'fields': ('name', 'slug', 'category', 'department', 'short_description', 'description')
        }),
        (_('联系信息'), {
            'fields': ('address', 'phone', 'email', 'website')
        }),
        (_('服务信息'), {
            'fields': ('services_offered', 'tags')
        }),
        (_('媒体信息'), {
            'fields': ('featured_image',)
        }),
        (_('评分和设置'), {
            'fields': ('rating', 'status', 'is_featured', 'is_affiliated')
        }),
        (_('时间信息'), {
            'fields': ('created_at', 'updated_at'),
            'classes': ('collapse',)
        }),
    )
    
    def get_queryset(self, request):
        return super().get_queryset(request).select_related('category', 'department')
    
    def save_model(self, request, obj, form, change):
        # 自动生成slug如果为空
        if not obj.slug:
            from django.utils.text import slugify
            import uuid
            obj.slug = slugify(obj.name) + '-' + str(uuid.uuid4())[:8]
        super().save_model(request, obj, form, change)


# 自定义User模型的注册和配置
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.forms import UserChangeForm, UserCreationForm
from .models import User


# 自定义用户创建表单
class CustomUserCreationForm(UserCreationForm):
    """自定义用户创建表单，添加自定义字段"""
    class Meta(UserCreationForm.Meta):
        model = User
        fields = ('username', 'email', 'full_name', 'phone')


# 自定义用户修改表单
class CustomUserChangeForm(UserChangeForm):
    """自定义用户修改表单，添加自定义字段"""
    class Meta(UserChangeForm.Meta):
        model = User
        fields = ('username', 'email', 'full_name', 'phone', 'avatar_url', 
                  'is_active', 'is_staff', 'is_superuser', 'groups', 'user_permissions')


# 自定义用户管理类
@admin.register(User)
class CustomUserAdmin(UserAdmin):
    """自定义用户管理类，在Django CMS中显示用户标签"""
    # 使用自定义表单
    form = CustomUserChangeForm
    add_form = CustomUserCreationForm
    
    # 列表页面显示的字段
    list_display = ('username', 'email', 'full_name', 'phone', 'is_active', 'is_staff', 'created_time')
    list_filter = ('is_active', 'is_staff', 'is_superuser', 'groups', 'created_time')
    search_fields = ('username', 'email', 'full_name', 'phone')
    ordering = ('username',)
    
    # 详情页面的字段分组
    fieldsets = (
        (None, {'fields': ('username', 'password')}),
        (_('个人信息'), {'fields': ('email', 'full_name', 'phone', 'avatar_url')}),
        (_('权限'), {
            'fields': ('is_active', 'is_staff', 'is_superuser', 'groups', 'user_permissions'),
            'classes': ('wide',),
        }),
        (_('重要日期'), {'fields': ('last_login', 'created_time', 'updated_time')}),
    )
    
    # 添加用户页面的字段
    add_fieldsets = (
        (None, {
            'classes': ('wide',),
            'fields': ('username', 'email', 'full_name', 'phone', 'password1', 'password2'),
        }),
    )
    
    # 只读字段
    readonly_fields = ('last_login', 'created_time', 'updated_time')
    
    # 自定义操作按钮
    actions = ['activate_users', 'deactivate_users']
    
    def activate_users(self, request, queryset):
        """激活选中的用户"""
        queryset.update(is_active=True)
    activate_users.short_description = _('激活选中的用户')
    
    def deactivate_users(self, request, queryset):
        """禁用选中的用户"""
        queryset.update(is_active=False)
    deactivate_users.short_description = _('禁用选中的用户')