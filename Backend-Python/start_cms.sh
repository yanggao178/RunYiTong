#!/bin/bash

# AI医疗管理系统 Django CMS 启动脚本
echo "========================================"
echo "   AI医疗管理系统 Django CMS 启动脚本"
echo "========================================"
echo

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到Python3，请先安装Python 3.8+"
    exit 1
fi

# 检查虚拟环境
if [ ! -d ".venv" ]; then
    echo "创建虚拟环境..."
    python3 -m venv .venv
    echo "虚拟环境创建完成"
fi

# 激活虚拟环境
echo "激活虚拟环境..."
source .venv/bin/activate

# 安装依赖
echo "安装Django CMS依赖包..."
pip install -r django_cms_requirements.txt

# 检查是否需要初始化
if [ ! -f "cms_medical.db" ]; then
    echo "初始化数据库和创建示例数据..."
    python init_cms_data.py
else
    echo "执行数据库迁移..."
    python manage.py migrate
fi

# 收集静态文件
echo "收集静态文件..."
python manage.py collectstatic --noinput

# 启动开发服务器
echo
echo "========================================"
echo "启动Django CMS开发服务器..."
echo "访问地址: http://localhost:8080/"
echo "管理后台: http://localhost:8080/admin/"
echo "按 Ctrl+C 停止服务器"
echo "========================================"
echo

python manage.py runserver 0.0.0.0:8080