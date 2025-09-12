from rest_framework import serializers
from .models import Product, ProductCategory, MedicalDepartment, Book, BookTag, BookCategory, Hospital, HospitalCategory


class ProductCategorySerializer(serializers.ModelSerializer):
    """商品分类序列化器"""
    
    class Meta:
        model = ProductCategory
        fields = ['id', 'name', 'description', 'image', 'parent', 'is_active', 'sort_order']
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理图片字段
        if instance.image:
            data['image'] = instance.image.url
        else:
            data['image'] = None
        return data


class MedicalDepartmentSerializer(serializers.ModelSerializer):
    """医疗科室序列化器"""
    
    class Meta:
        model = MedicalDepartment
        fields = ['id', 'name', 'description', 'image', 'is_active']
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理图片字段
        if instance.image:
            data['image'] = instance.image.url
        else:
            data['image'] = None
        return data


class BookTagSerializer(serializers.ModelSerializer):
    """书籍标签序列化器"""
    
    class Meta:
        model = BookTag
        fields = ['id', 'name']


class BookCategorySerializer(serializers.ModelSerializer):
    """书籍分类序列化器"""
    
    class Meta:
        model = BookCategory
        fields = ['id', 'name', 'description', 'parent', 'is_active', 'sort_order']


class ProductSerializer(serializers.ModelSerializer):
    """商品序列化器"""
    category = ProductCategorySerializer(read_only=True)
    department = MedicalDepartmentSerializer(read_only=True)
    
    class Meta:
        model = Product
        fields = [
            'id', 'name', 'slug', 'description', 'short_description',
            'category', 'department', 'price', 'original_price',
            'stock_quantity', 'min_stock_level', 'sku', 'barcode',
            'weight', 'dimensions', 'featured_image', 'gallery_images',
            'tags', 'status', 'is_featured', 'is_prescription_required',
            'manufacturer', 'expiry_date', 'usage_instructions',
            'side_effects', 'contraindications', 'views_count',
            'sales_count', 'created_at', 'updated_at', 'is_in_stock',
            'is_low_stock', 'discount_percentage'
        ]
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理主图片字段
        if instance.featured_image:
            data['featured_image'] = instance.featured_image.url
        else:
            data['featured_image'] = None
            
        # 处理图库图片
        gallery_urls = []
        for image in instance.gallery_images.all():
            gallery_urls.append(image.url)
        data['gallery_images'] = gallery_urls
        
        return data


class ProductListSerializer(serializers.ModelSerializer):
    """商品列表序列化器（简化版）"""
    category_name = serializers.CharField(source='category.name', read_only=True)
    department_name = serializers.CharField(source='department.name', read_only=True)
    
    class Meta:
        model = Product
        fields = [
            'id', 'name', 'slug', 'short_description', 'category_name',
            'department_name', 'price', 'original_price', 'stock_quantity',
            'featured_image', 'status', 'is_featured', 'is_in_stock',
            'discount_percentage', 'created_at'
        ]
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理主图片字段
        if instance.featured_image:
            data['featured_image'] = instance.featured_image.url
        else:
            data['featured_image'] = None
        return data


class BookSerializer(serializers.ModelSerializer):
    """书籍序列化器"""
    tags = BookTagSerializer(many=True, read_only=True)
    
    class Meta:
        model = Book
        fields = [
            'id', 'name', 'author', 'category', 'description',
            'cover_url', 'pdf_file_path', 'file_size', 'publish_date',
            'tags', 'created_time', 'updated_time'
        ]


class BookListSerializer(serializers.ModelSerializer):
    """书籍列表序列化器（简化版）"""
    
    class Meta:
        model = Book
        fields = [
            'id', 'name', 'author', 'category', 'description',
            'cover_url', 'publish_date', 'created_time'
        ]


# 医院相关序列化器
class HospitalCategorySerializer(serializers.ModelSerializer):
    """医院分类序列化器"""
    
    class Meta:
        model = HospitalCategory
        fields = ['id', 'name', 'description', 'image', 'is_active', 'sort_order']
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理图片字段
        if instance.image:
            data['image'] = instance.image.url
        else:
            data['image'] = None
        return data


class HospitalSerializer(serializers.ModelSerializer):
    """医院序列化器"""
    category = HospitalCategorySerializer(read_only=True)
    department = MedicalDepartmentSerializer(read_only=True)
    
    class Meta:
        model = Hospital
        fields = [
            'id', 'name', 'slug', 'description', 'short_description',
            'category', 'department', 'address', 'phone', 'email',
            'website', 'rating', 'featured_image', 'gallery_images',
            'services_offered', 'tags', 'status', 'is_featured', 'is_affiliated',
            'created_at', 'updated_at'
        ]
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理主图片字段
        if instance.featured_image:
            data['featured_image'] = instance.featured_image.url
        else:
            data['featured_image'] = None
            
        # 处理图库图片
        gallery_urls = []
        for image in instance.gallery_images.all():
            gallery_urls.append(image.image.url)
        data['gallery_images'] = gallery_urls
        
        return data


class HospitalListSerializer(serializers.ModelSerializer):
    """医院列表序列化器（简化版）"""
    category_name = serializers.CharField(source='category.name', read_only=True)
    department_name = serializers.CharField(source='department.name', read_only=True)
    
    class Meta:
        model = Hospital
        fields = [
            'id', 'name', 'slug', 'short_description', 'category_name',
            'department_name', 'address', 'phone', 'rating', 'featured_image',
            'status', 'is_featured', 'is_affiliated', 'created_at'
        ]
        
    def to_representation(self, instance):
        data = super().to_representation(instance)
        # 处理主图片字段
        if instance.featured_image:
            data['featured_image'] = instance.featured_image.url
        else:
            data['featured_image'] = None
        return data