from django.contrib import admin
from django.utils.translation import gettext_lazy as _
from django.utils.html import format_html
from .models import (
    MedicalDepartment, Doctor, MedicalNews, MedicalService,
    ProductCategory, Product, ProductImage
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


# 自定义管理后台标题
admin.site.site_header = _('AI医疗管理系统')
admin.site.site_title = _('AI医疗管理系统')
admin.site.index_title = _('管理首页')