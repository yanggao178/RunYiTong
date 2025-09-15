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
    extra = 0  # 设置为0，避免显示空的图片表单
    fields = ('image', 'order')
    verbose_name = _('商品图库图片')
    verbose_name_plural = _('商品图库图片')
    
    def get_extra(self, request, obj=None, **kwargs):
        # 只有当编辑现有对象时，才显示额外的空表单
        if obj is not None:
            return 1
        return 0


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = [
        'name', 'category', 'department', 'price', 'stock_quantity', 
        'status', 'is_featured', 'sales_count', 'pharmacy_name', 'created_at'
    ]
    
    def response_action(self, request, queryset):
        """覆盖默认的response_action方法，处理选中的主键值，过滤掉无效的'None'字符串和非数字值"""
        # 直接获取请求中的选中项，使用固定的复选框名称
        selected = request.POST.getlist('_selected_action')
        
        # 过滤掉'None'字符串、空字符串和非数字值
        valid_selected = []
        for pk in selected:
            if pk and pk != 'None':
                try:
                    # 尝试将值转换为整数，验证它是一个有效的数字ID
                    int(pk)
                    valid_selected.append(pk)
                except ValueError:
                    # 如果转换失败，跳过这个值
                    continue
        
        # 如果没有有效的选中项，直接返回默认处理
        if not valid_selected:
            return super().response_action(request, queryset)
        
        # 修改请求中的选中项列表
        request.POST = request.POST.copy()
        request.POST.setlist('_selected_action', valid_selected)
        
        # 调用父类方法继续处理
        return super().response_action(request, queryset)
        
    def changelist_view(self, request, extra_context=None):
        """覆盖changelist_view方法，添加额外的请求数据验证"""
        # 同时处理GET和POST请求中的参数验证
        # 处理GET请求参数
        if 'pk__in' in request.GET:
            # 获取pk__in参数的值
            pks = request.GET.get('pk__in', '').split(',')
            # 过滤掉无效的值
            valid_pks = []
            for pk in pks:
                pk = pk.strip()
                if pk and pk != 'None':
                    try:
                        int(pk)
                        valid_pks.append(pk)
                    except ValueError:
                        continue
            # 如果有有效的ID值，重建pk__in参数
            if valid_pks:
                # 创建一个新的GET参数字典
                get_params = request.GET.copy()
                get_params['pk__in'] = ','.join(valid_pks)
                request.GET = get_params
            else:
                # 如果没有有效的ID值，删除pk__in参数
                get_params = request.GET.copy()
                if 'pk__in' in get_params:
                    del get_params['pk__in']
                request.GET = get_params
        
        # 处理POST请求参数
        if request.method == 'POST' and '_selected_action' in request.POST:
            # 获取选中的ID列表
            selected = request.POST.getlist('_selected_action')
            # 过滤掉无效的值
            valid_selected = []
            for pk in selected:
                if pk and pk != 'None':
                    try:
                        int(pk)
                        valid_selected.append(pk)
                    except ValueError:
                        continue
            # 如果有有效的ID值，更新POST参数
            if valid_selected:
                post_params = request.POST.copy()
                post_params.setlist('_selected_action', valid_selected)
                request.POST = post_params
            else:
                # 如果没有有效的ID值，删除该参数
                post_params = request.POST.copy()
                if '_selected_action' in post_params:
                    del post_params['_selected_action']
                request.POST = post_params
        
        return super().changelist_view(request, extra_context)
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
            'fields': ('weight', 'dimensions', 'manufacturer', 'pharmacy_name', 'expiry_date', 'tags')
        }),
        (_('图片'), {
            'fields': ('featured_image_file',)
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
        
    def save_formset(self, request, form, formset, change):
        # 对于内联表单，确保它们都关联到正确的父对象
        for inline_form in formset.forms:
            if hasattr(inline_form, 'instance') and inline_form.instance.pk is None:
                inline_form.instance.product = form.instance
        
        # 保存表单集，让Django的标准保存机制处理所有保存逻辑
        formset.save()
        
        # 处理可能的重复记录问题
        try:
            # 刷新实例以确保数据一致性
            form.instance.refresh_from_db()
        except form.instance.__class__.MultipleObjectsReturned:
            # 如果出现重复记录，记录警告并继续
            import logging
            logger = logging.getLogger(__name__)
            logger.warning(f"发现重复的商品记录: {form.instance.name}")
    
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