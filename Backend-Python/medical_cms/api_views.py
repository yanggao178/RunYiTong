from rest_framework.decorators import api_view, permission_classes
from rest_framework import generics
from rest_framework import status, filters
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q
from .models import Product, ProductCategory, MedicalDepartment, Book, BookTag, BookCategory, Hospital, HospitalCategory
from .serializers import (
    ProductSerializer, 
    ProductListSerializer, 
    ProductCategorySerializer,
    MedicalDepartmentSerializer,
    BookSerializer,
    BookListSerializer,
    BookTagSerializer,
    BookCategorySerializer,
    HospitalSerializer,
    HospitalListSerializer,
    HospitalCategorySerializer
)

# 导入SQLAlchemy相关模块用于连接ai_medical.db
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database import SessionLocal
from models import Product as AIProduct


class ProductListAPIView(generics.ListAPIView):
    """商品列表API"""
    queryset = Product.objects.filter(status='active')
    serializer_class = ProductListSerializer
    permission_classes = [AllowAny]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['category', 'department', 'is_featured', 'is_prescription_required']
    search_fields = ['name', 'description', 'short_description', 'tags']
    ordering_fields = ['created_at', 'price', 'sales_count', 'views_count']
    ordering = ['-created_at']
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # 按分类筛选
        category_id = self.request.query_params.get('category_id')
        if category_id:
            queryset = queryset.filter(category_id=category_id)
            
        # 按科室筛选
        department_id = self.request.query_params.get('department_id')
        if department_id:
            queryset = queryset.filter(department_id=department_id)
            
        # 价格范围筛选
        min_price = self.request.query_params.get('min_price')
        max_price = self.request.query_params.get('max_price')
        if min_price:
            queryset = queryset.filter(price__gte=min_price)
        if max_price:
            queryset = queryset.filter(price__lte=max_price)
            
        # 库存筛选
        in_stock_only = self.request.query_params.get('in_stock_only')
        if in_stock_only and in_stock_only.lower() == 'true':
            queryset = queryset.filter(stock_quantity__gt=0)
            
        return queryset.select_related('category', 'department')


class ProductDetailAPIView(generics.RetrieveAPIView):
    """商品详情API"""
    queryset = Product.objects.filter(status='active')
    serializer_class = ProductSerializer
    permission_classes = [AllowAny]
    lookup_field = 'slug'
    
    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        # 增加浏览次数
        instance.views_count += 1
        instance.save(update_fields=['views_count'])
        
        serializer = self.get_serializer(instance)
        return Response(serializer.data)


class ProductCategoryListAPIView(generics.ListAPIView):
    """商品分类列表API"""
    queryset = ProductCategory.objects.filter(is_active=True)
    serializer_class = ProductCategorySerializer
    permission_classes = [AllowAny]
    ordering = ['sort_order', 'name']


class MedicalDepartmentListAPIView(generics.ListAPIView):
    """医疗科室列表API"""
    queryset = MedicalDepartment.objects.filter(is_active=True)
    serializer_class = MedicalDepartmentSerializer
    permission_classes = [AllowAny]
    ordering = ['name']


class BookListAPIView(generics.ListAPIView):
    """书籍列表API"""
    queryset = Book.objects.all()
    serializer_class = BookListSerializer
    permission_classes = [AllowAny]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['category']
    search_fields = ['name', 'author', 'description']
    ordering_fields = ['created_time', 'publish_date']
    ordering = ['-created_time']
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # 按分类筛选
        category = self.request.query_params.get('category')
        if category:
            queryset = queryset.filter(category=category)
            
        return queryset


class BookDetailAPIView(generics.RetrieveAPIView):
    """书籍详情API"""
    queryset = Book.objects.all()
    serializer_class = BookSerializer
    permission_classes = [AllowAny]
    lookup_field = 'id'


class BookTagListAPIView(generics.ListAPIView):
    """书籍标签列表API"""
    queryset = BookTag.objects.all()
    serializer_class = BookTagSerializer
    permission_classes = [AllowAny]
    ordering = ['name']


class BookCategoryListAPIView(generics.ListAPIView):
    """书籍分类列表API"""
    queryset = BookCategory.objects.filter(is_active=True)
    serializer_class = BookCategorySerializer
    permission_classes = [AllowAny]
    ordering = ['sort_order', 'name']


@api_view(['GET'])
@permission_classes([AllowAny])
def featured_products(request):
    """获取推荐商品"""
    limit = int(request.GET.get('limit', 10))
    products = Product.objects.filter(
        status='active', 
        is_featured=True
    ).select_related('category', 'department')[:limit]
    
    serializer = ProductListSerializer(products, many=True)
    return Response({
        'count': len(products),
        'results': serializer.data
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def search_books(request):
    """书籍搜索API"""
    query = request.GET.get('q', '')
    if not query:
        return Response({'error': '搜索关键词不能为空'}, status=status.HTTP_400_BAD_REQUEST)
    
    books = Book.objects.filter(
        Q(name__icontains=query) |
        Q(author__icontains=query) |
        Q(description__icontains=query)
    )
    
    serializer = BookListSerializer(books, many=True)
    return Response({
        'query': query,
        'count': len(books),
        'results': serializer.data
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def book_stats(request):
    """书籍统计信息API"""
    total_books = Book.objects.count()
    categories_count = Book.objects.values('category').distinct().count()
    tags_count = BookTag.objects.count()
    
    return Response({
        'total_books': total_books,
        'categories_count': categories_count,
        'tags_count': tags_count
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def search_products(request):
    """商品搜索API"""
    query = request.GET.get('q', '')
    if not query:
        return Response({'error': '搜索关键词不能为空'}, status=status.HTTP_400_BAD_REQUEST)
    
    products = Product.objects.filter(
        Q(name__icontains=query) |
        Q(description__icontains=query) |
        Q(short_description__icontains=query) |
        Q(tags__icontains=query),
        status='active'
    ).select_related('category', 'department')
    
    serializer = ProductListSerializer(products, many=True)
    return Response({
        'query': query,
        'count': len(products),
        'results': serializer.data
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def product_stats(request):
    """商品统计信息API"""
    total_products = Product.objects.filter(status='active').count()
    featured_products_count = Product.objects.filter(status='active', is_featured=True).count()
    categories_count = ProductCategory.objects.filter(is_active=True).count()
    departments_count = MedicalDepartment.objects.filter(is_active=True).count()
    
    return Response({
        'total_products': total_products,
        'featured_products': featured_products_count,
        'categories': categories_count,
        'departments': departments_count
    })


# ===== 从ai_medical.db数据库获取商品数据的API =====

@api_view(['GET'])
@permission_classes([AllowAny])
def ai_products_list(request):
    """从ai_medical.db获取商品列表"""
    db = SessionLocal()
    try:
        # 获取查询参数
        category = request.GET.get('category')
        search = request.GET.get('search')
        limit = int(request.GET.get('limit', 20))
        skip = int(request.GET.get('skip', 0))  # Android前端使用skip参数
        offset = skip  # 将skip转换为offset
        
        # 构建查询
        query = db.query(AIProduct)
        
        # 按分类筛选
        if category:
            query = query.filter(AIProduct.category == category)
            
        # 搜索功能
        if search:
            query = query.filter(
                AIProduct.name.contains(search) |
                AIProduct.description.contains(search)
            )
            
        # 分页
        products = query.offset(offset).limit(limit).all()
        total_count = query.count()
        
        # 转换为字典格式，字段映射与ai_medical.db保持一致
        products_data = []
        for product in products:
            products_data.append({
                'id': product.id,
                'name': product.name,
                'price': float(product.price) if product.price else 0.0,
                'description': product.description,
                'image_url': product.featured_image_url,  # 映射到featured_image_url
                'category': product.category_id,  # 暂时返回category_id，后续可关联查询分类名称
                'stock': product.stock_quantity,  # 映射到stock_quantity
                'specification': product.usage_instructions,  # 映射到usage_instructions
                'manufacturer': product.manufacturer,
                'purchase_count': product.sales_count,  # 映射到sales_count
                'created_time': product.created_at.isoformat() if product.created_at else None,  # 映射到created_at
                'updated_time': product.updated_at.isoformat() if product.updated_at else None  # 映射到updated_at
            })
            
        # 返回符合Android前端期望的ApiResponse<ProductListResponse>格式
        return Response({
            'success': True,
            'message': '获取商品列表成功',
            'data': {
                'items': products_data,
                'total': total_count,
                'skip': skip,
                'limit': limit
            }
        })
        
    except Exception as e:
        return Response(
            {'error': f'获取商品列表失败: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
    finally:
        db.close()


# ===== 医院相关API =====

class HospitalListAPIView(generics.ListAPIView):
    """医院列表API"""
    queryset = Hospital.objects.filter(status='active')
    serializer_class = HospitalListSerializer
    permission_classes = [AllowAny]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['category', 'department', 'is_featured', 'is_affiliated']
    search_fields = ['name', 'description', 'short_description', 'tags', 'address', 'services_offered']
    ordering_fields = ['created_at', 'rating', 'name']
    ordering = ['-created_at']
    
    def get_queryset(self):
        queryset = super().get_queryset()
        
        # 按分类筛选
        category_id = self.request.query_params.get('category_id')
        if category_id:
            queryset = queryset.filter(category_id=category_id)
            
        # 按科室筛选
        department_id = self.request.query_params.get('department_id')
        if department_id:
            queryset = queryset.filter(department_id=department_id)
            
        # 按评分范围筛选
        min_rating = self.request.query_params.get('min_rating')
        max_rating = self.request.query_params.get('max_rating')
        if min_rating:
            queryset = queryset.filter(rating__gte=min_rating)
        if max_rating:
            queryset = queryset.filter(rating__lte=max_rating)
            
        # 按状态筛选
        status = self.request.query_params.get('status')
        if status:
            queryset = queryset.filter(status=status)
            
        # 按是否特色医院筛选
        featured = self.request.query_params.get('featured')
        if featured and featured.lower() == 'true':
            queryset = queryset.filter(is_featured=True)
            
        # 按是否合作医院筛选
        affiliated = self.request.query_params.get('affiliated')
        if affiliated and affiliated.lower() == 'true':
            queryset = queryset.filter(is_affiliated=True)
            
        return queryset.select_related('category', 'department')


class HospitalDetailAPIView(generics.RetrieveAPIView):
    """医院详情API"""
    queryset = Hospital.objects.filter(status='active')
    serializer_class = HospitalSerializer
    permission_classes = [AllowAny]
    lookup_field = 'slug'
    
    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        serializer = self.get_serializer(instance)
        return Response(serializer.data)


class HospitalCategoryListAPIView(generics.ListAPIView):
    """医院分类列表API"""
    queryset = HospitalCategory.objects.filter(is_active=True)
    serializer_class = HospitalCategorySerializer
    permission_classes = [AllowAny]
    ordering = ['sort_order', 'name']


@api_view(['GET'])
@permission_classes([AllowAny])
def featured_hospitals(request):
    """获取推荐医院"""
    limit = int(request.GET.get('limit', 10))
    hospitals = Hospital.objects.filter(
        status='active', 
        is_featured=True
    ).select_related('category', 'department')[:limit]
    
    serializer = HospitalListSerializer(hospitals, many=True)
    return Response({
        'count': len(hospitals),
        'results': serializer.data
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def search_hospitals(request):
    """医院搜索API"""
    query = request.GET.get('q', '')
    if not query:
        return Response({'error': '搜索关键词不能为空'}, status=status.HTTP_400_BAD_REQUEST)
    
    hospitals = Hospital.objects.filter(
        Q(name__icontains=query) |
        Q(description__icontains=query) |
        Q(short_description__icontains=query) |
        Q(tags__icontains=query) |
        Q(address__icontains=query) |
        Q(services_offered__icontains=query),
        status='active'
    ).select_related('category', 'department')
    
    serializer = HospitalListSerializer(hospitals, many=True)
    return Response({
        'query': query,
        'count': len(hospitals),
        'results': serializer.data
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def hospital_stats(request):
    """医院统计信息API"""
    total_hospitals = Hospital.objects.filter(status='active').count()
    featured_hospitals_count = Hospital.objects.filter(status='active', is_featured=True).count()
    categories_count = HospitalCategory.objects.filter(is_active=True).count()
    departments_count = MedicalDepartment.objects.filter(is_active=True).count()
    
    return Response({
        'total_hospitals': total_hospitals,
        'featured_hospitals': featured_hospitals_count,
        'categories': categories_count,
        'departments': departments_count
    })


@api_view(['GET'])
@permission_classes([AllowAny])
def ai_product_detail(request, product_id):
    """从ai_medical.db获取商品详情"""
    db = SessionLocal()
    try:
        product = db.query(AIProduct).filter(AIProduct.id == product_id).first()
        
        if not product:
            return Response(
                {'error': '商品不存在'}, 
                status=status.HTTP_404_NOT_FOUND
            )
            
        # 转换为字典格式，字段映射与ai_medical.db保持一致
        product_data = {
            'id': product.id,
            'name': product.name,
            'price': float(product.price) if product.price else 0.0,
            'description': product.description,
            'image_url': product.featured_image_url,  # 映射到featured_image_url
            'category': product.category_id,  # 暂时返回category_id，后续可关联查询分类名称
            'stock': product.stock_quantity,  # 映射到stock_quantity
            'specification': product.usage_instructions,  # 映射到usage_instructions
            'manufacturer': product.manufacturer,
            'purchase_count': product.sales_count,  # 映射到sales_count
            'created_time': product.created_at.isoformat() if product.created_at else None,  # 映射到created_at
            'updated_time': product.updated_at.isoformat() if product.updated_at else None  # 映射到updated_at
        }
        
        # 返回符合Android前端期望的ApiResponse<Product>格式
        return Response({
            'success': True,
            'message': '获取商品详情成功',
            'data': product_data
        })
        
    except Exception as e:
        return Response(
            {'error': f'获取商品详情失败: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )
    finally:
        db.close()