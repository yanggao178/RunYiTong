import os
from pathlib import Path
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Build paths inside the project like this: BASE_DIR / 'subdir'.
BASE_DIR = Path(__file__).resolve().parent.parent

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = os.getenv('SECRET_KEY', 'django-insecure-change-this-in-production')

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = os.getenv('DEBUG', 'True').lower() == 'true'

ALLOWED_HOSTS = os.getenv('ALLOWED_HOSTS', 'localhost,127.0.0.1,0.0.0.0,10.0.2.2,192.168.0.6').split(',')

# Application definition
INSTALLED_APPS = [
    # Django CMS admin style
    'djangocms_admin_style',
    
    # Django apps
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'django.contrib.sites',
    'django.contrib.sitemaps',
    'django.contrib.redirects',
    
    # Django CMS core
    'cms',
    'menus',
    'sekizai',
    'treebeard',
    'djangocms_text_ckeditor',
    
    # Django CMS plugins
    'djangocms_picture',
    'djangocms_link',
    'djangocms_file',
    'djangocms_video',
    'djangocms_googlemap',
    'djangocms_style',
    'djangocms_column',
    
    # Media handling
    'filer',
    'easy_thumbnails',
    'easy_thumbnails.optimize',
    
    # Third party apps
    'rest_framework',
    'django_filters',
    'corsheaders',
    'allauth',
    'allauth.account',
    'allauth.socialaccount',
    
    # Custom apps
    'medical_cms',  # 自定义应用
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    'whitenoise.middleware.WhiteNoiseMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'allauth.account.middleware.AccountMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
    'django.middleware.locale.LocaleMiddleware',
    
    # Django CMS middleware
    'cms.middleware.user.CurrentUserMiddleware',
    'cms.middleware.page.CurrentPageMiddleware',
    'cms.middleware.toolbar.ToolbarMiddleware',
    'cms.middleware.language.LanguageCookieMiddleware',
    'django.contrib.redirects.middleware.RedirectFallbackMiddleware',
]

ROOT_URLCONF = 'cms_project.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [BASE_DIR / 'templates'],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
                'django.template.context_processors.media',
                'django.template.context_processors.csrf',
                'django.template.context_processors.tz',
                'django.template.context_processors.static',
                
                # Django CMS context processors
                'cms.context_processors.cms_settings',
                'sekizai.context_processors.sekizai',
            ],
        },
    },
]

WSGI_APPLICATION = 'cms_project.wsgi.application'

# Database
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR / 'cms_medical.db',
    }
}

# If using PostgreSQL (uncomment and configure)
# DATABASES = {
#     'default': {
#         'ENGINE': 'django.db.backends.postgresql',
#         'NAME': os.getenv('DB_NAME', 'cms_medical'),
#         'USER': os.getenv('DB_USER', 'postgres'),
#         'PASSWORD': os.getenv('DB_PASSWORD', ''),
#         'HOST': os.getenv('DB_HOST', 'localhost'),
#         'PORT': os.getenv('DB_PORT', '5432'),
#     }
# }

# Password validation
AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]

# Internationalization
LANGUAGE_CODE = 'zh-hans'
TIME_ZONE = 'Asia/Shanghai'
USE_I18N = True
USE_TZ = True

# Languages for Django CMS
LANGUAGES = [
    ('zh-hans', '简体中文'),
]



# Static files (CSS, JavaScript, Images)
STATIC_URL = '/static/'
STATIC_ROOT = BASE_DIR / 'staticfiles'
STATICFILES_DIRS = [
    BASE_DIR / 'static',
]

# Media files
MEDIA_URL = '/media/'
MEDIA_ROOT = BASE_DIR / 'media'

# Django CMS settings
CMS_CONFIRM_VERSION4 = True
CMS_PERMISSION = True
CMS_PLUGIN_PROCESSORS = [
    'cms.plugin_processors.parent_plugins',
]
CMS_PLACEHOLDER_CONF = {
    'content': {
        'name': '内容区域',
        'plugins': [
            'TextPlugin', 'PicturePlugin', 'LinkPlugin', 
            'FilePlugin', 'VideoPlugin', 'GoogleMapPlugin',
            'StylePlugin', 'ColumnPlugin',
        ],
        'default_plugins': [],
        'limits': {}
    },
    'sidebar': {
        'name': '侧边栏',
        'plugins': [
            'TextPlugin', 'LinkPlugin', 'PicturePlugin',
        ],
        'default_plugins': [],
        'limits': {}
    },
}

# Django CMS templates
CMS_TEMPLATES = (
    ('base.html', '基础模板'),
    ('cms/standard_page.html', '标准页面'),
    ('cms/landing_page.html', '着陆页'),
    ('cms/sidebar_page.html', '侧边栏页面'),
)

# Easy Thumbnails settings
THUMBNAIL_PROCESSORS = (
    'easy_thumbnails.processors.colorspace',
    'easy_thumbnails.processors.autocrop',
    'filer.thumbnail_processors.scale_and_crop_with_subject_location',
    'easy_thumbnails.processors.filters',
    'easy_thumbnails.processors.background',
)

THUMBNAIL_ALIASES = {
    '': {
        'admin_thumb': {'size': (150, 150), 'crop': True},
        'small': {'size': (200, 150), 'crop': True},
        'medium': {'size': (400, 300), 'crop': True},
        'large': {'size': (800, 600), 'crop': True},
    },
}

THUMBNAIL_OPTIMIZE_COMMAND = {
    'gif': ['gifsicle', '-b', '-O3', '%(filename)s'],
    'jpeg': ['jpegoptim', '-f', '--strip-all', '%(filename)s'],
    'png': ['optipng', '-force', '-o7', '%(filename)s'],
    'svg': ['scour', '--no-line-breaks', '--remove-comments', '-i', '%(filename)s', '-o', '%(filename)s']
}

# Filer settings
FILER_CANONICAL_URL = 'sharing/'
FILER_ENABLE_PERMISSIONS = False
FILER_STORAGES = {
    'public': {
        'main': {
            'ENGINE': 'filer.storage.PublicFileSystemStorage',
            'OPTIONS': {
                'location': MEDIA_ROOT,
                'base_url': MEDIA_URL,
            },
            'UPLOAD_TO': 'filer.utils.generate_filename.randomized',
            'UPLOAD_TO_PREFIX': 'filer_public',
        },
        'thumbnails': {
            'ENGINE': 'filer.storage.PublicFileSystemStorage',
            'OPTIONS': {
                'location': MEDIA_ROOT,
                'base_url': MEDIA_URL,
            },
            'THUMBNAIL_OPTIONS': {
                'base_dir': 'filer_public_thumbnails',
            },
            'UPLOAD_TO_PREFIX': 'filer_public_thumbnails',
        },
    },
}

# CKEditor settings
TEXT_CKEDITOR_FILER_IGNORE_EMPTY_SELECT = True

# Default primary key field type
DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# Site ID
SITE_ID = 1

# Django REST Framework
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework.authentication.SessionAuthentication',
        'rest_framework.authentication.TokenAuthentication',
    ],
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAuthenticated',
    ],
    'DEFAULT_PAGINATION_CLASS': 'rest_framework.pagination.PageNumberPagination',
    'PAGE_SIZE': 20
}

# CORS settings
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
    "http://127.0.0.1:3000",
    "http://localhost:8080",
    "http://127.0.0.1:8080",
]

CORS_ALLOW_CREDENTIALS = True

# Logging
LOGGING = {
    'version': 1,
    'disable_existing_loggers': False,
    'handlers': {
        'file': {
            'level': 'INFO',
            'class': 'logging.FileHandler',
            'filename': BASE_DIR / 'logs' / 'django.log',
        },
        'console': {
            'level': 'INFO',
            'class': 'logging.StreamHandler',
        },
    },
    'root': {
        'handlers': ['console', 'file'],
        'level': 'INFO',
    },
}

# Create logs directory if it doesn't exist
os.makedirs(BASE_DIR / 'logs', exist_ok=True)
