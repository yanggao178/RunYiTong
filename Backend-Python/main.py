from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
import uvicorn
from contextlib import asynccontextmanager

# 导入路由
from routers import products, books, prescriptions, appointments, users, payments
from database import engine, Base

# 创建数据库表
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时创建数据库表
    Base.metadata.create_all(bind=engine)
    yield
    # 关闭时的清理工作

# 创建FastAPI应用
app = FastAPI(
    title="AI Medical Backend",
    description="AI医疗应用后端API",
    version="1.0.0",
    lifespan=lifespan
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 在生产环境中应该设置具体的域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 静态文件服务
app.mount("/static", StaticFiles(directory="static"), name="static")

# 注册路由
app.include_router(products.router, prefix="/api/v1/products", tags=["商品管理"])
app.include_router(books.router, prefix="/api/v1/books", tags=["图书管理"])
app.include_router(prescriptions.router, prefix="/api/v1/prescriptions", tags=["处方管理"])
app.include_router(appointments.router, prefix="/api/v1/appointments", tags=["预约挂号"])
app.include_router(users.router, prefix="/api/v1/users", tags=["用户管理"])
app.include_router(payments.router, prefix="/api/v1/payments", tags=["支付管理"])

@app.get("/")
async def root():
    return {"message": "AI Medical Backend API", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)