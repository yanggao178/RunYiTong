from fastapi import FastAPI, HTTPException
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, JSONResponse, Response
import uvicorn
from contextlib import asynccontextmanager
from dotenv import load_dotenv
import os
import logging
import urllib.parse
# import pydevd_pycharm

# 加载环境变量
load_dotenv()

# 导入路由
from routers import products, books, prescriptions, appointments, users, payments, health_records, addresses, orders
from database import engine, Base


# 创建数据库表
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时创建数据库表
    Base.metadata.create_all(bind=engine)
    yield
    # 关闭时的清理工作

# 创建FastAPI应用
def create_app():
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
        expose_headers=["Content-Type", "Content-Length", "Last-Modified", "ETag"],  # 显式暴露图片相关的响应头
    )

    # 静态文件服务
    import os
    import logging
    from fastapi.staticfiles import StaticFiles
    
    # 配置日志以调试静态文件服务
    logging.basicConfig(level=logging.INFO)
    logger = logging.getLogger("FastAPI Static Files")
    
    # 获取当前文件目录的绝对路径（更简洁的实现）
    base_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 构建静态文件和媒体文件目录的绝对路径
    static_dir = os.path.join(base_dir, "static")
    media_dir = os.path.join(base_dir, "media")
    
    # 验证目录存在性并记录详细日志
    logger.info(f"当前工作目录: {os.getcwd()}")
    logger.info(f"当前文件目录: {base_dir}")
    logger.info(f"静态文件目录: {static_dir}, 存在: {os.path.exists(static_dir)}")
    logger.info(f"媒体文件目录: {media_dir}, 存在: {os.path.exists(media_dir)}")
    
    # 验证媒体目录结构和目标文件
    # if os.path.exists(media_dir):
    #     product_images_dir = os.path.join(media_dir, "product_images")
    #     logger.info(f"product_images目录: {product_images_dir}, 存在: {os.path.exists(product_images_dir)}")
        
        # # 检查目标图片文件
        # target_file = "药品1_Qkuuolu.jpg"
        # target_file_path = os.path.join(product_images_dir, target_file)
        # logger.info(f"目标图片路径: {target_file_path}, 存在: {os.path.exists(target_file_path)}")
        
        # if os.path.exists(target_file_path):
        #     logger.info(f"目标图片大小: {os.path.getsize(target_file_path)} 字节")
        #     logger.info(f"测试访问URL: http://192.168.0.7:8000/media/product_images/{target_file}")
    
    # 自定义静态文件服务类，增强对中文和编码URL的支持
    class EnhancedStaticFiles(StaticFiles):
        # 简单的媒体类型检测方法
        def get_media_type(self, path: str) -> str:
            """根据文件扩展名返回媒体类型"""
            import mimetypes
            mime_type, _ = mimetypes.guess_type(path)
            return mime_type or "application/octet-stream"
        
        async def get_response(self, path: str, scope):
            # 记录请求信息用于调试
            logger.info(f"静态文件请求路径: {path}")
            logger.info(f"原始请求URL: {scope['path']}")
            
            # 尝试解码路径，处理URL编码的中文
            try:
                # FastAPI的StaticFiles在调用get_response前可能已经对path进行了解码
                # 但我们需要确保路径中的中文能被正确处理
                # 首先对传入的path进行解码（针对客户端编码的URL）
                decoded_path = urllib.parse.unquote(path)
                logger.info(f"第一次解码后的路径: {decoded_path}")
                
                # 构建完整的文件系统路径
                full_path = os.path.join(self.directory, decoded_path)
                logger.info(f"文件系统路径: {full_path}")
                
                # 检查文件是否存在
                if os.path.isfile(full_path):
                    logger.info(f"文件存在: {full_path}")
                    # 使用FileResponse直接返回文件，确保正确处理中文
                    return FileResponse(full_path, media_type=self.get_media_type(full_path))
                else:
                    logger.warning(f"文件不存在: {full_path}")
                    
                    # 尝试直接使用原始path调用父类方法（不进行额外解码）
                    logger.info(f"尝试使用原始路径调用父类方法: {path}")
                    try:
                        return await super().get_response(path, scope)
                    except Exception as e:
                        logger.error(f"父类方法处理异常: {e}")
                        
                        # 尝试使用os.path.normpath规范化路径
                        normalized_path = os.path.normpath(decoded_path)
                        logger.info(f"尝试使用规范化路径: {normalized_path}")
                        try:
                            return await super().get_response(normalized_path, scope)
                        except Exception as e2:
                            logger.error(f"规范化路径处理异常: {e2}")
                            
                            # 返回详细的404错误信息
                            error_info = {
                                "error": "文件未找到",
                                "requested_path": path,
                                "decoded_path": decoded_path,
                                "full_path": full_path,
                                "directory": self.directory,
                                "exception": str(e2)
                            }
                            return JSONResponse(content=error_info, status_code=404)
            except Exception as e:
                logger.error(f"静态文件处理异常: {e}")
                # 返回500响应
                return JSONResponse(content={"error": str(e)}, status_code=500)
    
    # 挂载静态文件服务，使用增强版的StaticFiles类
    app.mount(
        "/static", 
        EnhancedStaticFiles(directory=static_dir, check_dir=True),
        name="static"
    )
    app.mount(
        "/media", 
        EnhancedStaticFiles(directory=media_dir, check_dir=True),
        name="media"
    )

    # 添加一个调试路由，用于检查媒体目录和文件状态
    # @app.get("/debug/static_files")
    # async def debug_static_files():
    #     target_file = "药品1_Qkuuolu.jpg"
    #     product_images_dir = os.path.join(media_dir, "product_images")
    #     target_file_path = os.path.join(product_images_dir, target_file)
        
    #     # 获取product_images目录中的文件列表
    #     files_in_dir = []
    #     if os.path.exists(product_images_dir):
    #         files_in_dir = os.listdir(product_images_dir)
        
    #     return {
    #         "current_dir": base_dir,
    #         "working_dir": os.getcwd(),
    #         "static_dir": static_dir,
    #         "media_dir": media_dir,
    #         "product_images_dir": product_images_dir,
    #         "media_dir_exists": os.path.exists(media_dir),
    #         "product_images_dir_exists": os.path.exists(product_images_dir),
    #         "target_file": target_file,
    #         "target_file_path": target_file_path,
    #         "target_file_exists": os.path.exists(target_file_path),
    #         "files_in_product_images": files_in_dir,
    #         "test_url": f"http://192.168.0.7:8000/media/product_images/{target_file}",
    #         "url_encoded": f"http://192.168.0.7:8000/media/product_images/%E8%8D%AF%E5%93%811_Qkuuolu.jpg"
    #     }
    
    # 添加一个直接访问图片的测试路由
    # @app.get("/test_image")
    # async def test_image():
    #     target_file = "药品1_Qkuuolu.jpg"
    #     target_url = f"http://192.168.0.7:8000/media/product_images/{target_file}"
    #     return {
    #         "message": "请点击下方链接测试图片访问",
    #         "direct_url": target_url,
    #         "encoded_url": f"http://192.168.0.7:8000/media/product_images/%E8%8D%AF%E5%93%811_Qkuuolu.jpg"
    #     }
    
    # 注册路由
    app.include_router(products.router, prefix="/api/v1/products", tags=["商品管理"])
    app.include_router(books.router, prefix="/api/v1/books", tags=["图书管理"])
    app.include_router(prescriptions.router, prefix="/api/v1/prescriptions", tags=["处方管理"])
    app.include_router(appointments.router, prefix="/api/v1/appointments", tags=["预约挂号"])
    app.include_router(users.router, prefix="/api/v1/users", tags=["用户管理"])
    app.include_router(payments.router, prefix="/api/v1/payments", tags=["支付管理"])
    app.include_router(health_records.router, prefix="/api/v1/health-records", tags=["健康档案"])
    app.include_router(addresses.router, prefix="/api/v1/addresses", tags=["收货地址管理"])
    app.include_router(orders.router, prefix="/api/v1/orders", tags=["订单管理"])
    
    @app.get("/")
    async def root():
        return {"message": "AI Medical Backend API", "version": "1.0.0"}
    
    @app.get("/health")
    async def health_check():
        return {"status": "healthy"}

    # import pdb; pdb.set_trace()
    # pydevd_pycharm.settrace('192.168.0.5', port=8000, stdout_to_server=True, stderr_to_server=True)
    
    return app

# 创建应用实例
app = create_app()

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True, log_level="info")