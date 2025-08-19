@echo off
chcp 65001 >nul
echo ========================================
echo    AI医疗管理系统 Django CMS 启动脚本
echo ========================================
echo.

:: 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Python，请先安装Python 3.8+
    pause
    exit /b 1
)

:: 检查虚拟环境
if not exist ".venv" (
    echo 创建虚拟环境...
    python -m venv .venv
    echo 虚拟环境创建完成
)

:: 激活虚拟环境
echo 激活虚拟环境...
call .venv\Scripts\activate.bat

:: 安装依赖
echo 安装Django CMS依赖包...
pip install -r django_cms_requirements.txt

:: 检查是否需要初始化
if not exist "cms_medical.db" (
    echo 初始化数据库和创建示例数据...
    python init_cms_data.py
) else (
    echo 执行数据库迁移...
    python manage.py migrate
)

:: 收集静态文件
echo 收集静态文件...
python manage.py collectstatic --noinput

:: 启动开发服务器
echo.
echo ========================================
echo 启动Django CMS开发服务器...
echo 访问地址: http://localhost:8080/
echo 管理后台: http://localhost:8080/admin/
echo 按 Ctrl+C 停止服务器
echo ========================================
echo.

python manage.py runserver 0.0.0.0:8080

pause