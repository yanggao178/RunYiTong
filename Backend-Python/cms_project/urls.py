from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from django.conf.urls.i18n import i18n_patterns
from cms.sitemaps import CMSSitemap
from django.contrib.sitemaps.views import sitemap

# Sitemaps
sitemaps = {
    'cmspages': CMSSitemap,
}

# Non-translatable URLs
urlpatterns = [
    # Admin interface
    path('admin/', admin.site.urls),
    
    # API endpoints
    path('api/', include('medical_cms.urls')),
    path('api-auth/', include('rest_framework.urls')),
    
    # File handling
    path('filer/', include('filer.urls')),
    
    # Sitemap
    path('sitemap.xml', sitemap, {'sitemaps': sitemaps}, name='django.contrib.sitemaps.views.sitemap'),
    
    # Health check endpoint (待实现)
    # path('health/', include('medical_cms.health.urls')),
]

# Translatable URLs with i18n patterns
urlpatterns += i18n_patterns(
    # Django CMS URLs (must be last)
    path('', include('cms.urls')),
)

# Serve media files in development
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
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