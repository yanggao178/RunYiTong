from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


class MedicalCmsConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'medical_cms'
    verbose_name = _('医疗内容管理系统')
    
    def ready(self):
        # 应用准备就绪时的初始化代码
        pass