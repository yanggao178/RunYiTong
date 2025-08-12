@echo off
chcp 65001 >nul
echo ================================================
echo           AI Medical Backend Server
echo ================================================
echo.
echo 正在启动后端服务...
echo.

:: 检查Python是否可用
where python >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Python，请先安装Python 3.7+
    echo 下载地址: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo Python环境检查通过
echo.

:: 尝试安装基本依赖
echo 正在安装基本依赖包...
python -m pip install fastapi uvicorn --quiet --disable-pip-version-check
if %errorlevel% neq 0 (
    echo 警告: 依赖包安装可能失败，尝试继续运行...
)

echo.
echo 启动服务器...
echo 服务地址: http://localhost:8000
echo API文档: http://localhost:8000/docs
echo 按 Ctrl+C 停止服务器
echo ================================================
echo.

:: 尝试运行简化版应用
python simple_main.py
if %errorlevel% neq 0 (
    echo.
    echo 简化版启动失败，尝试运行完整版...
    python run.py
)

echo.
echo 服务器已停止
pause