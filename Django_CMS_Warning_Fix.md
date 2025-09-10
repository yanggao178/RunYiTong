# Django CMS API 弃用警告解决方案

## 问题描述

在运行Django CMS项目时出现以下警告信息：

```
UserWarning: This API function will be removed in django CMS 4. For publishing functionality use a package that adds publishing, such as: djangocms-versioning.
```

这些警告来自于Django CMS 3.x版本中使用的`get_page_draft`函数，该函数在Django CMS 4.0中将被移除。

## 警告出现的位置

- `cms_toolbars.py:122`
- `cms_toolbars.py:67` 
- `cms_toolbars.py:396`
- `toolbar.py:39`

## 解决方案

### 1. 临时解决方案：警告过滤

在`cms_project/settings.py`中添加警告过滤器：

```python
import warnings

# 禁用Django CMS API弃用警告
warnings.filterwarnings('ignore', 
                       message='This API function will be removed in django CMS 4',
                       category=UserWarning,
                       module='cms')
```

### 2. CMS兼容性配置

在`settings.py`中确保正确的CMS配置：

```python
# CMS版本兼容性设置
CMS_PERMISSION = True
CMS_PLACEHOLDER_CONF = {}

# 禁用版本4的新功能警告
# 这将保持与当前Django CMS 3.x的兼容性
# CMS_CONFIRM_VERSION4 = False
```

### 3. 长期解决方案选项

#### 选项A：继续使用Django CMS 3.x
- 保持当前版本
- 使用警告过滤器
- 等待项目需要时再升级

#### 选项B：准备升级到Django CMS 4.x
1. 安装`djangocms-versioning`包：
   ```bash
   pip install djangocms-versioning
   ```

2. 在`INSTALLED_APPS`中添加：
   ```python
   INSTALLED_APPS = [
       # ... 其他应用
       'djangocms_versioning',
       # ...
   ]
   ```

3. 运行迁移：
   ```bash
   python manage.py migrate
   ```

## 实施状态

✅ 已实施警告过滤器配置
✅ 已更新CMS兼容性设置  
✅ 服务器启动正常，无警告信息
✅ 系统检查通过

## 验证结果

- Django服务器启动无警告
- 系统检查无问题
- CMS功能正常运行

## 注意事项

1. 这些警告不影响当前功能的正常使用
2. 警告过滤器只是临时解决方案
3. 建议在项目发展到一定阶段时考虑升级到Django CMS 4.x
4. 升级前需要充分测试所有CMS功能

## 相关文件

- `cms_project/settings.py` - 主要配置文件
- `django_cms_requirements.txt` - 依赖包配置

---
*解决方案实施日期：2025-09-10*