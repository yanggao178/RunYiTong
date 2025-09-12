from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import api_views

# 创建路由器
router = DefaultRouter()

# API URL配置
urlpatterns = [
    # REST API路由
    path('', include(router.urls)),
    
    # 自定义API端点
    path('products/', api_views.ProductListAPIView.as_view(), name='product-list'),
    path('products/<slug:slug>/', api_views.ProductDetailAPIView.as_view(), name='product-detail'),
    path('products/featured/', api_views.featured_products, name='featured-products'),
    path('products/search/', api_views.search_products, name='search-products'),
    path('products/stats/', api_views.product_stats, name='product-stats'),
    path('categories/', api_views.ProductCategoryListAPIView.as_view(), name='category-list'),
    path('departments/', api_views.MedicalDepartmentListAPIView.as_view(), name='department-list'),
    
    # 书籍相关API端点
    path('books/', api_views.BookListAPIView.as_view(), name='book-list'),
    path('books/<slug:slug>/', api_views.BookDetailAPIView.as_view(), name='book-detail'),
    path('books/search/', api_views.search_books, name='search-books'),
    path('books/stats/', api_views.book_stats, name='book-stats'),
    path('book-tags/', api_views.BookTagListAPIView.as_view(), name='book-tag-list'),
    path('book-categories/', api_views.BookCategoryListAPIView.as_view(), name='book-category-list'),
    
    # AI医疗数据库商品API
    path('ai-products/', api_views.ai_products_list, name='ai-products-list'),
    path('ai-products/<int:product_id>/', api_views.ai_product_detail, name='ai-product-detail'),
    
    # 医院相关API端点
    path('hospitals/', api_views.HospitalListAPIView.as_view(), name='hospital-list'),
    path('hospitals/<slug:slug>/', api_views.HospitalDetailAPIView.as_view(), name='hospital-detail'),
    path('hospitals/featured/', api_views.featured_hospitals, name='featured-hospitals'),
    path('hospitals/search/', api_views.search_hospitals, name='search-hospitals'),
    path('hospitals/stats/', api_views.hospital_stats, name='hospital-stats'),
    path('hospital-categories/', api_views.HospitalCategoryListAPIView.as_view(), name='hospital-category-list'),
]