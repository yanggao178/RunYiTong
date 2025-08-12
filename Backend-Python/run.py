import uvicorn
import os
from dotenv import load_dotenv
from init_data import init_database

# 加载环境变量
load_dotenv()

def main():
    """启动应用程序"""
    # 初始化数据库（如果需要）
    if not os.path.exists("ai_medical.db"):
        print("正在初始化数据库...")
        init_database()
    
    # 获取配置
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", 8000))
    debug = os.getenv("DEBUG", "True").lower() == "true"
    
    print(f"启动服务器: http://{host}:{port}")
    print(f"API文档: http://{host}:{port}/docs")
    print(f"调试模式: {debug}")
    
    # 启动服务器
    uvicorn.run(
        "main:app",
        host=host,
        port=port,
        reload=debug,
        log_level="info" if debug else "warning"
    )

if __name__ == "__main__":
    main()