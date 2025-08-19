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
    
    # AI医疗数据库商品API
    path('ai-products/', api_views.ai_products_list, name='ai-products-list'),
    path('ai-products/<int:product_id>/', api_views.ai_product_detail, name='ai-product-detail'),
]