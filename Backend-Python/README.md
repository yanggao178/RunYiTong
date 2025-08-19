# AI医疗管理系统 Django CMS

基于Django CMS构建的医疗内容管理系统，提供完整的医疗机构网站管理功能。

## 功能特性

### 核心功能
- 🏥 **医疗科室管理** - 完整的科室信息管理
- 👨‍⚕️ **医生信息管理** - 医生档案、专业信息管理
- 📰 **医疗新闻发布** - 新闻内容管理和发布
- 🛠️ **医疗服务展示** - 服务项目管理
- 📞 **联系表单** - 在线咨询和预约功能

### CMS功能
- 📝 **可视化编辑** - Django CMS提供的所见即所得编辑
- 🎨 **模板管理** - 灵活的页面模板系统
- 🔧 **插件系统** - 丰富的内容插件
- 👥 **用户权限管理** - 多级用户权限控制
- 🌐 **多语言支持** - 国际化内容管理

## 技术栈

- **后端框架**: Django 4.2+
- **CMS系统**: Django CMS 3.11+
- **数据库**: SQLite (开发) / PostgreSQL (生产)
- **前端**: Bootstrap 5, jQuery
- **API**: Django REST Framework
- **文件管理**: Django Filer
- **图片处理**: Easy Thumbnails
- **编辑器**: CKEditor

## 快速开始

### 环境要求

- Python 3.8+
- pip (Python包管理器)

### Windows用户

1. **进入项目目录**
   ```bash
   cd Backend-Python
   ```

2. **运行启动脚本**
   ```bash
   start_cms.bat
   ```

### Linux/macOS用户

1. **进入项目目录**
   ```bash
   cd Backend-Python
   ```

2. **给启动脚本执行权限**
   ```bash
   chmod +x start_cms.sh
   ```

3. **运行启动脚本**
   ```bash
   ./start_cms.sh
   ```

### 访问系统

启动成功后，可以通过以下地址访问系统：

- **前台网站**: http://localhost:8080/
- **管理后台**: http://localhost:8080/admin/
- **CMS编辑**: http://localhost:8080/?edit (登录后)

### 默认账户

- **用户名**: admin
- **密码**: admin123
- **邮箱**: admin@medical.com

## 使用指南

### 内容管理

1. **登录管理后台**
   - 访问 http://localhost:8080/admin/
   - 使用默认账户登录

2. **管理医疗内容**
   - **医疗科室**: 在"Medical cms" → "Medical departments"中管理
   - **医生信息**: 在"Medical cms" → "Doctors"中管理
   - **医疗新闻**: 在"Medical cms" → "Medical newss"中管理
   - **医疗服务**: 在"Medical cms" → "Medical services"中管理

3. **页面编辑**
   - 访问前台页面，登录后点击"编辑"按钮
   - 使用Django CMS的可视化编辑器

## 项目结构

```
Backend-Python/
├── cms_project/                 # Django项目配置
├── medical_cms/                # 医疗CMS应用
├── templates/                  # 模板文件
├── static/                     # 静态文件
├── manage.py                   # Django管理脚本
├── init_cms_data.py           # 数据初始化脚本
├── django_cms_requirements.txt # 依赖包列表
├── start_cms.bat              # Windows启动脚本
├── start_cms.sh               # Linux/macOS启动脚本
└── README.md                  # 项目说明文档
```

## 常见问题

### Q: 启动时出现数据库错误？
A: 确保已经运行了数据库迁移：`python manage.py migrate`

### Q: 静态文件无法加载？
A: 运行`python manage.py collectstatic`收集静态文件

### Q: 忘记管理员密码？
A: 运行`python manage.py createsuperuser`创建新的超级用户

---

**AI医疗管理系统 Django CMS** - 让医疗内容管理更简单！