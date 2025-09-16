from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
# Import for django CMS
sitemap_urlpatterns = []
if 'django.contrib.sitemaps' in settings.INSTALLED_APPS and 'cms' in settings.INSTALLED_APPS:
    from cms.sitemaps import CMSSitemap
    from django.contrib.sitemaps.views import sitemap
    sitemap_urlpatterns = [
        path('sitemap.xml', sitemap, {'sitemaps': {'cmspages': CMSSitemap}}, name='django.contrib.sitemaps.views.sitemap'),
    ]

# Non-translatable URLs
urlpatterns = [
    # Admin interface
    path('admin/', admin.site.urls),
    # API endpoints
    path('api/', include('medical_cms.urls')),
    path('api-auth/', include('rest_framework.urls')),
    # django CMS URLs
    path('', include('cms.urls')),

    # Health check endpoint (待实现)
    # path('health/', include('medical_cms.health.urls')),
]

# Serve media files - always serve in this project to ensure media access
urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

# Serve static files in development
if settings.DEBUG:
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)
    
    # Django Debug Toolbar
    if 'debug_toolbar' in settings.INSTALLED_APPS:
        import debug_toolbar
        urlpatterns = [
            path('__debug__/', include(debug_toolbar.urls)),
        ] + urlpatterns

# Custom error handlers (待实现)
# handler404 = 'medical_cms.views.custom_404'
# handler500 = 'medical_cms.views.custom_500'