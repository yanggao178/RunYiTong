# AI Medical Backend

基于 FastAPI 构建的智能医疗后端服务，为 Android 前端应用提供 API 支持。

## 功能特性

- 🏥 **用户管理**: 用户注册、登录、个人资料管理
- 🛒 **商品管理**: 医疗保健品的展示、搜索、购买
- 📚 **健康资源**: 中医西医图书资源管理
- 💊 **处方管理**: 处方创建、图片上传、AI 症状分析
- 📅 **预约挂号**: 医院科室预约、时间管理
- 📊 **健康档案**: 用户健康记录跟踪

## 技术栈

- **框架**: FastAPI
- **数据库**: SQLAlchemy (支持 SQLite/MySQL)
- **认证**: JWT Token
- **文档**: 自动生成 OpenAPI/Swagger 文档
- **部署**: Uvicorn ASGI 服务器

## 项目结构

```
Backend-Python/
├── main.py              # 主应用入口
├── database.py          # 数据库配置
├── models.py            # SQLAlchemy 模型
├── schemas.py           # Pydantic 数据模型
├── init_data.py         # 数据初始化脚本
├── run.py              # 启动脚本
├── requirements.txt     # 依赖包列表
├── .env                # 环境配置
├── routers/            # API 路由模块
│   ├── __init__.py
│   ├── users.py        # 用户管理 API
│   ├── products.py     # 商品管理 API
│   ├── books.py        # 图书资源 API
│   ├── prescriptions.py # 处方管理 API
│   └── appointments.py  # 预约挂号 API
└── static/             # 静态文件目录
```

## 快速开始

### 1. 安装依赖

```bash
# 创建虚拟环境（推荐）
python -m venv .venv

# 激活虚拟环境
# Windows
.venv\Scripts\activate
# Linux/Mac
source .venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 2. 配置环境

编辑 `.env` 文件，修改相关配置：

```env
# 数据库配置
DATABASE_URL=sqlite:///./ai_medical.db

# JWT配置
SECRET_KEY=your-super-secret-key-change-this-in-production

# 服务器配置
HOST=0.0.0.0
PORT=8000
DEBUG=True
```

### 3. 启动服务

```bash
# 使用启动脚本（推荐）
python run.py

# 或直接使用 uvicorn
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 4. 访问服务

- **API 服务**: http://localhost:8000
- **API 文档**: http://localhost:8000/docs
- **ReDoc 文档**: http://localhost:8000/redoc

## API 端点

### 用户管理
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/me` - 更新用户信息

### 商品管理
- `GET /api/products/` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products/` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品

### 图书资源
- `GET /api/books/` - 获取图书列表
- `GET /api/books/chinese` - 获取中医图书
- `GET /api/books/western` - 获取西医图书
- `GET /api/books/{id}` - 获取图书详情

### 处方管理
- `GET /api/prescriptions/` - 获取处方列表
- `POST /api/prescriptions/` - 创建处方
- `POST /api/prescriptions/upload-image` - 上传处方图片
- `POST /api/prescriptions/analyze-symptoms` - AI 症状分析

### 预约挂号
- `GET /api/appointments/` - 获取预约列表
- `POST /api/appointments/` - 创建预约
- `GET /api/appointments/available-slots` - 获取可用时间段
- `GET /api/appointments/departments` - 获取科室列表

## 数据库

### 初始化数据

首次运行时会自动创建数据库表并初始化示例数据，包括：
- 示例商品（养生茶、艾灸贴等）
- 医学图书（黄帝内经、本草纲目等）
- 测试用户账号

### 手动初始化

```bash
python init_data.py
```

## 开发说明

### 添加新的 API 端点

1. 在 `models.py` 中定义数据模型
2. 在 `schemas.py` 中定义请求/响应模型
3. 在 `routers/` 目录下创建路由文件
4. 在 `main.py` 中注册路由

### 数据库迁移

如需使用 Alembic 进行数据库迁移：

```bash
# 安装 alembic
pip install alembic

# 初始化迁移
alembic init alembic

# 生成迁移文件
alembic revision --autogenerate -m "Initial migration"

# 执行迁移
alembic upgrade head
```

## 部署

### Docker 部署

```dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["python", "run.py"]
```

### 生产环境配置

1. 修改 `.env` 中的 `SECRET_KEY`
2. 设置 `DEBUG=False`
3. 配置生产数据库（MySQL/PostgreSQL）
4. 使用反向代理（Nginx）
5. 配置 HTTPS

## 许可证

MIT License